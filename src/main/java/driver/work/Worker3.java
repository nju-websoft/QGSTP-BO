package driver.work;

import mytools.Config;
import driver.ProcessBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.Math.abs;

/**
 * Run algorithm on dbpedia
 * @Author qkoqhh
 * @Date 2020-11-03
 */
public class Worker3 extends WorkerBase{


    public Worker3(String graphname) {
        super(graphname);
    }


    @Override
    protected void read_data() {
        try{
            connect();
            read_graph();
            System.out.println("Read Data Finished...");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    protected void gen_keyword(int key_id) throws Exception {
        System.out.println("Generating keyword...");
        if (Config.special_query) {
            read_raw_query("data/"+Config.query_file_name);
        }else if (Config.random_query) {
            select_query();
            put_raw_query("data/" + graphname + "-" + System.currentTimeMillis() + "-" + key_id + ".txt");
        } else if (Config.fixed_file_query) {
            read_raw_query("data/" + graphname + "-fixed-" + key_id +".txt");
        }else if(Config.fixed_query){
            pick_query(key_id);
        }else if(Config.special_fixed_query){
            pick_query(Config.query_id+key_id);
        }
    }



    void select_query() throws Exception {
        System.out.println("Selecting query...");
        Statement stmt = ProcessBase.conn.createStatement();
        ResultSet ret = stmt.executeQuery("select count(distinct query) from queries");
        int queries_num = 0;
        if (ret.next()) {
            queries_num = ret.getInt("count(distinct query)");
        }
        ret.close();
        pick_query(rand.nextInt(queries_num));
    }


}
