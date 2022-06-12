package driver.data;

import driver.work.Run;
import mytools.Config;
import mytools.SQL_batch;
import driver.ProcessBase;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Generate fix query in database randomly
 * Refer to the document of method gen_query()
 * @Author qkoqhh
 * @Date 2020-12-30
 */
public class GenFixQueryByRandom extends ProcessBase {
    public GenFixQueryByRandom(String graphname) {
        super(graphname);
        try {
            read_graph();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connect();
    }
    Random rand=new Random();

    int cnt;
    Statement stmt;
    SQL_batch keymap,queries;

    /**
     * Generate a query contains g keywords, which maps to kwf keywords
     * @param q the id of query
     * @param g the number of keywords
     * @param kwf the (average) number of nodes the keywords maps to
     */
    void gen_query(int q, int g, int kwf) throws Exception {
        for (int i = 0; i < g; i++) {
            Set<Integer> keyword = new HashSet<>();
            while(keyword.size()<kwf) {
                int key_node = rand.nextInt(G.n);
                keyword.add(key_node);
            }
            cnt++;
            queries.add(q,cnt);
            for(Integer node:keyword) {
                keymap.add(cnt, node);
            }
        }
    }

    public void gen() throws Exception {
        stmt=conn.createStatement();
        stmt.executeUpdate(
                "create table queries(" +
                        "    query int," +
                        "    keyword int" +
                        ")"
        );
        stmt.executeUpdate(
                "create table keymap(" +
                        "   `key` int not null," +
                        "    `node` int not null," +
                        "    primary key(`key`,`node`)" +
                        ")"
        );
        conn.setAutoCommit(false);
        keymap=new SQL_batch(conn,"insert into keymap values (?,?)");
        queries=new SQL_batch(conn,"insert into queries values (?,?)");

        List<Integer>g=Arrays.asList(2,4,4,4,6);
        List<Integer>kwf=Arrays.asList(100,10,100,1000,100);
        for (int i=0;i<5;i++){
            for(int j=0;j<50;j++){
                gen_query(i*50+j,g.get(i),kwf.get(i));
            }
        }

        keymap.close();
        queries.close();
        stmt.close();
    }

}
