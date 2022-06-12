package driver.data;

import mytools.Config;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

/**
 * @Author qkoqhh
 * @Date 2020-11-01
 */
public class ReadLUBM extends ReadMondial{
    static final String label_tag="advisor";
    Map<String,List<Integer>> keywords=new HashMap<>();

    /**
     * Create the experiment database, and initialize the tables: nodes, edges, keyword, keymap
     * The connection to database is hold on, remember to close it
     * @param dbname the name of database
     */
    public ReadLUBM(String dbname) {
        super(dbname);
    }

    @Override
    public void read_graph(String filename) throws Exception {
        String in=ReadLUBM.class.getClassLoader().getResource(filename).toString();
        Model model= ModelFactory.createDefaultModel();
        model.read(in,"N-TRIPLE");

        StmtIterator iter = model.listStatements();
        while(iter.hasNext()){
            org.apache.jena.rdf.model.Statement stmt=iter.next();
            String pred = stmt.getPredicate().getLocalName();

            if(!stmt.getSubject().isURIResource()){
                continue;
            }
            int x=getnode(stmt.getSubject().toString());

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

    public static void main(String[]args){
    }
}
