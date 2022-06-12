package driver.data;

import mytools.Config;
import mytools.SQL_batch;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler;
import org.apache.jena.rdfxml.xmloutput.impl.Abbreviated;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

/**
 * Read DBpedia into database
 * Here we need extra tables, maybe it will be combined in DBbuilder
 *      types:
 *          format: (id - name)
 *          content: map the typename to its id.
 *
 *      nodetype:
 *          format: (node - type)
 *          content: store the type of nodes
 *
 *      labels:
 *          format: (id - label)
 *          content: store the label of nodes
 *
 *      queries:
 *          format: (query - keyword)
 *          content: store the keywords of a query
 *
 * @Author qkoqhh
 * @Date 2020-11-4
 */
public class ReadDBpedia extends ReadMondial{
    /**
     * Create the experiment database, and initialize the tables: nodes, edges, keyword, keymap
     * The connection to database is hold on, remember to close it
     * @param dbname the name of database
     */
    SQL_batch types,nodetype,labels,queries;
    public ReadDBpedia(String dbname) {
        super(dbname);
        java.sql.Statement stmt=null;
        try{
            stmt=conn.createStatement();
            stmt.executeUpdate(
                    "create table types(" +
                    "    id int," +
                    "    name varchar(1024)," +
                    "    primary key (id)" +
                    ")");
            stmt.executeUpdate(
                    "create table nodetype(" +
                            "    node int," +
                            "    type int" +
                            ")"
            );
            stmt.executeUpdate(
                    "create table labels(" +
                    "    id int," +
                    "    label varchar(1024)" +
                    ")"
            );
            stmt.executeUpdate(
                    "create table queries(" +
                    "    query int," +
                    "    keyword int" +
                    ")"
            );


            types=new SQL_batch(conn,"insert into types values (?,?)");
            nodetype=new SQL_batch(conn,"insert into nodetype values (?,?)");
            labels=new SQL_batch(conn,"insert into labels values (?,?)");
            queries=new SQL_batch(conn,"insert into queries values (?,?)");


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally{
            try{
                if(stmt!=null) {
                    stmt.close();
                }
            }catch(SQLException se2){
            }
        }
    }

    Map<String,Integer> typelist=new HashMap<>();


    int gettype(String type) throws Exception {
        if(!typelist.containsKey(type)){
            types.add(typelist.size(),type);
            typelist.put(type,typelist.size());
        }
        return typelist.get(type);
    }




    @Override
    public void read_graph(String filename) throws Exception {
        System.out.println("Reading graph from file...");
        Model model= ModelFactory.createDefaultModel();
//        model.getReader().setErrorHandler(new Abbreviated());
        RDFParser.create()
                .source(new File(filename).toURI().toString())
                .lang(Lang.TTL)
                .parse(model);
//        Model model= RDFDataMgr.loadModel(filename, Lang.TTL);
//        model.read(new File(filename).toURI().toString(),"TTL");
        StmtIterator iter = model.listStatements();
        while(iter.hasNext()){
            Statement stmt=iter.next();
            int x=getnode(stmt.getSubject().toString());
            int y=getnode(stmt.getObject().toString());
            edges.add(x,y);
            jgraph.addEdge(x,y);
        }
        edges.close();
        model.close();
    }

    public void read_type(String filename) throws Exception {
        System.out.println("Reading type from file...");
        Model model= ModelFactory.createDefaultModel();
        model.read(filename);
        StmtIterator iter = model.listStatements();
        while(iter.hasNext()){
            Statement stmt=iter.next();
            if(!nodemap.containsKey(stmt.getSubject().toString())){
                continue;
            }
            int x=nodemap.get(stmt.getSubject().toString());
            int y=gettype(stmt.getObject().toString());
            nodetype.add(x,y);
        }
        typelist = null;
        types.close();
        model.close();
    }



    public void read_label(String filename) throws Exception {
        System.out.println("Reading label from file...");
        Model model= ModelFactory.createDefaultModel();
        model.read(filename);
        StmtIterator iter = model.listStatements();
        int cnt=0;
        while(iter.hasNext()){
            Statement stmt=iter.next();
            cnt++;
            System.out.println("label "+cnt);
            if(!nodemap.containsKey(stmt.getSubject().toString())){
                continue;
            }
            int x=nodemap.get(stmt.getSubject().toString());
            String y=stmt.getObject().asLiteral().getString();
            labels.add(x,y);
        }
        labels.close();
        model.close();
    }



    public void read_query(String filename) throws Exception {
        System.out.println("Reading query from file...");
        Scanner in=new Scanner(filename);

        for(int query_num=0,keyword_num=0;in.hasNext();query_num++){
            String line=in.nextLine();
            line=line.substring(line.indexOf('\t')+1).trim().replace(',',' ');
            String[]keywords=line.split(" ");
            for(int i=0;i<keywords.length;i++) {
                queries.add(query_num, keyword_num++);
            }
            read_keyword(keywords);
        }
        queries.close();
        keymap.close();
        keyword.close();
    }


    /*
    public static void main(String[]args){
        ReadDBpedia here = new ReadDBpedia("dbpedia");
        try {
            here.read_graph("mappingbased_objects_en.ttl");
            here.set_vertex_weight();
            here.read_type("instance_types_transitive_en.ttl");
            here.read_label("labels_en.ttl");
            here.build_lucene();
            here.read_query("dbpedia.queries-v2_stopped.txt");
            here.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

        try{
            here.read_query(pwd + "dbpedia.queries-v2_stopped.txt");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    */
}

/* ------------------------------------------------------------------------------ */
/*
    void add_type(int id,String typename) throws SQLException {
        types.setInt(1,id);
        types.setString(2,typename);
        types.addBatch();
        types_cnt++;
        if(types_cnt%batch_size==0){
            System.out.println("Add type "+types_cnt);
            types.executeBatch();
            conn.commit();
            types.clearBatch();
        }
    }

    void add_nodetype(int nodeid,int typeid) throws SQLException {
        nodetype.setInt(1,nodeid);
        nodetype.setInt(2,typeid);
        nodetype.addBatch();
        nodetype_cnt++;
        if(nodetype_cnt%batch_size==0){
            System.out.println("Add nodetype "+nodetype_cnt);
            nodetype.executeBatch();
            conn.commit();;
            nodetype.clearBatch();
        }
    }

    void add_query(int query,int keyword) throws SQLException {
        queries.setInt(1,query);
        queries.setInt(2,keyword);
        queries.addBatch();
        queries_cnt++;
        if(queries_cnt%batch_size==0){
            System.out.println("Add nodetype "+nodetype_cnt);
            queries.executeBatch();
            conn.commit();
            queries.clearBatch();
        }
    }

    void add_nodelabel(int nodeid,String label) throws SQLException {
        labels.setInt(1,nodeid);
        labels.setString(2,label);
        labels.addBatch();
        labels_cnt++;
        if(labels_cnt%batch_size==0){
            System.out.println("Add label "+labels_cnt);
            labels.executeBatch();
            conn.commit();
            labels.clearBatch();
        }
    }

 */

