package driver.work;

import graphtheory.Query;
import mytools.Config;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Run algorithm on lubm.
 * @Author qkoqhh
 * @Date 2020-11-3
 */
public class Worker2 extends WorkerBase {
    Worker2(String graphname, Algorithm_type algorithmType, double alpha) {
        super(graphname, algorithmType, alpha);
    }
    Worker2(String graphname){
        super(graphname);
    }

    @Override
    protected void read_data(){
        try{
            connect();
            read_graph();
        }catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    protected void gen_keyword(int key_id) throws Exception {
        System.out.println("Generating keyword...");
        if(Config.random_query) {
            gen_random_query();
            put_raw_query("data/" + graphname + "-" + System.currentTimeMillis() + "-" + key_id + ".txt");
        }else if(Config.fixed_file_query){
            read_raw_query("data/" + graphname + "-fixed-" + key_id +".txt");
        }else if(Config.special_query){
            read_raw_query(Config.query_file_name);
        }else if(Config.fixed_query){
            pick_query(key_id);
        }else if(Config.special_fixed_query){
            pick_query(Config.query_id+key_id);
        }
    }

    /**
     * keyword number
     */
    private final int knum=6;
    /**
     * the average number of vertices matched with each keyword
     */
    private final int kwf=10;
    /**
     * Generate a query contains knum keywords, which maps to kwf keywords
     */
    void gen_random_query(){
        Q = new Query();
        Q.g = knum;
        G.key = new ArrayList<>();
        for (int i = 0; i < G.n; i++) {
            G.key.add(new LinkedList<>());
        }
        for (int i = 0; i < Q.g; i++) {
            ArrayList<Integer> keyword = new ArrayList<>();
            for (int j = 0; j < kwf; j++) {
                int key_node = Math.abs(rand.nextInt()) % G.n;
                keyword.add(key_node);
                G.key.get(key_node).add(i);
            }
            Q.keywords.add(keyword);
        }
    }





}
