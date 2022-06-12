package driver.work;

import java.sql.SQLException;

/**
 * Run algorithm on mondial
 * @Author qkoqhh
 * @Date 2020-11-03
 */
public class Worker extends WorkerBase{
    Worker(String graphname){
        super(graphname);
    }
    Worker(String graphname, Algorithm_type algorithmType, double alpha) {
        super(graphname, algorithmType, alpha);
    }

    @Override
    protected void gen_keyword(int key_id) throws Exception {
        throw new Exception("Not implemented");
    }

    @Override
    protected void read_data(){
        try{
            connect();
            read_graph();
            read_query();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public static void main(String[] args){
        Worker here=new Worker("mondial", Algorithm_type.EO,0.5);
        here.solve();
    }
}
