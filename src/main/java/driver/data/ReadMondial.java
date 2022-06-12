package driver.data;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

/**
 * Used to read mondial, probably some part of the code can be used to read other form of KG.
 * The query file is written in file already.
 * @Author qkoqhh
 * @Date 2020-10-31
 */
public class ReadMondial extends DBbuilder{
    static final String label_tag="teacherOf";
    static final String graph_name="lubm_2u";

    /**
     * Create the experiment database, and initialize the tables: nodes, edges, keyword, keymap
     * The connection to database is hold on, remember to close it
     *
     * @param dbname the name of database
     */
    ReadMondial(String dbname) {
        super(dbname);
    }
    Map<String,Integer>nodemap=new HashMap<>();
    List<String>nodename=new ArrayList<>();
    List<List<String>> labels =new ArrayList<>();
    org.jgrapht.Graph<Integer, DefaultEdge>jgraph=new DefaultDirectedGraph<>(DefaultEdge.class);

    int getnode(String s){
        if(!nodemap.containsKey(s)){
            jgraph.addVertex(nodemap.size());
            nodemap.put(s,nodemap.size());
            nodename.add(s);
            labels.add(new ArrayList<>());
        }
        return nodemap.get(s);
    }

    /**
     * Read the graph into database
     * Fulfill the table node, edges
     * @param filename filename of KG
     */
    void read_graph(String filename) throws Exception {
        Model model= ModelFactory.createDefaultModel();
        model.read(filename);

        StmtIterator iter = model.listStatements();
        while(iter.hasNext()){
            org.apache.jena.rdf.model.Statement stmt=iter.next();
            String pred = stmt.getPredicate().getLocalName();

            if(!stmt.getSubject().isURIResource()){
                continue;
            }
            int x=getnode(stmt.getSubject().toString());

            if(pred.equals(label_tag)){
                labels.get(x).add(stmt.getObject().toString());
                continue;
            }

            if(!stmt.getObject().isURIResource()){
                continue;
            }
            int y=getnode(stmt.getObject().toString());

            if(x!=y){
                edges.add(x,y);
            }
            jgraph.addEdge(x,y);
        }
        edges.close();
        model.close();

    }

    public void set_vertex_weight() throws Exception {
        System.out.println("Setting vertex weight...");
        int n=nodemap.size();
        PageRank<Integer,DefaultEdge> pageRank=new PageRank<>(jgraph);
        double min_pagerank=Double.MAX_VALUE;
        for(int i=0;i<n;i++){
            min_pagerank=min(min_pagerank,pageRank.getVertexScore(i));
        }
        for(int i=0;i<n;i++){
            double t=log10(pageRank.getVertexScore(i))-log10(min_pagerank);
            t=1-1/(1+exp(-t));
            nodes.add(i,nodename.get(i),t);
        }
        jgraph=null;
        nodes.close();
    }


    Path path;
    Directory dir;
    Analyzer analyzer;
    public void build_lucene() throws IOException {
        int n=nodemap.size();
        assert n>0;

        path= Files.createTempDirectory("indexfile");
        dir= FSDirectory.open(path);
        analyzer=new StandardAnalyzer();

        IndexWriterConfig config=new IndexWriterConfig(analyzer);
        IndexWriter iwriter=new IndexWriter(dir,config);
        for(int i=0;i<n;i++){
            for(String label: labels.get(i)){
                Document doc=new Document();
                doc.add(new Field("id",Integer.toString(i), TextField.TYPE_STORED));
                doc.add(new Field("label",label, TextField.TYPE_STORED));
                iwriter.addDocument(doc);
            }
        }
        iwriter.close();
    }


    /**
     * Read keywords from a query
     * Attention to close the SQL_batch keymap
     * @param keywords the keywords of queries
     */
    int keyword_num;
    protected void read_keyword(String[]keywords) throws Exception {
        int g=keywords.length;
        IndexReader ireader= DirectoryReader.open(dir);
        IndexSearcher isearcher=new IndexSearcher(ireader);
        QueryParser parser=new QueryParser("label",analyzer);
        for(int i=0;i<g;i++)if(!keywords[i].isEmpty()){
            keyword.add(keyword_num, keywords[i]);
            Query query = parser.parse(keywords[i]);
            ScoreDoc[] hits=isearcher.search(query,10).scoreDocs;
            for(int j=0;j< hits.length;j++){
                Document doc=isearcher.doc(hits[j].doc);
                keymap.add(keyword_num,Integer.parseInt(doc.get("id")));
            }
            keyword_num++;
        }
        ireader.close();
        keyword.close();
    }



    public static void main(String[]args){

        ReadMondial here=new ReadMondial(graph_name);
        try {
            System.out.println("Reading graph...");
            here.read_graph(graph_name+".nt");

            System.out.println("Setting vertex weight...");
            here.set_vertex_weight();

            List<String>keywords=new ArrayList<>();
            System.out.println("Reading keywords...");
            BufferedReader in = new BufferedReader(new FileReader("lubm_2u_query.in"));
            while(true) {
                String str=in.readLine();
                if(str==null){
                    break;
                }
                keywords.add(str);
            }
            in.close();
            here.read_keyword(keywords.toArray(new String[0]));
            here.keymap.close();
            System.out.println("Write in...");
            here.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}