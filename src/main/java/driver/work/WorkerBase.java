package driver.work;


import graphtheory.*;
import graphtheory.B3F.B3F;
import graphtheory.DPBF.DPBF;
import graphtheory.SemKSG.EO;
import graphtheory.SemKSG.QO;
import graphtheory.SemKSG.BO;
import mytools.Config;
import mytools.Filereader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.*;
import java.util.*;
import graphtheory.Structure.Tree;
import driver.ProcessBase;

/**
 * The collection of function to read the data and run the algorithm
 * Need to implement the progress manually
 * @Author qkoqhh
 * @Date 2020-11-3
 */
public abstract class WorkerBase extends ProcessBase {
    protected Random rand=new Random();

    static final int vec_length=10;


    protected Algorithm algorithm;
    protected Algorithm_type algorithm_type;
    final String graphname;





    WorkerBase(String graphname){
        super(graphname);
        System.out.println("Work on "+dbname+"...");
        this.graphname=graphname;
    }

    @Deprecated
    WorkerBase(String graphname, Algorithm_type algorithmType, double alpha){
        super(graphname);
        algorithm_type = algorithmType;
        Structure.alpha=alpha;
        System.out.println("Work on "+dbname+"...");
        this.graphname = null;
    }

    protected void reset_algo(Algorithm_type algorithmType){
        algorithm_type = algorithmType;
    }
    protected void reset_algo(String algo) throws Exception {
        if(algo.equals("EO")){
            algorithm_type=Algorithm_type.EO;
        }
        else
        if(algo.equals("QO")){
            algorithm_type=Algorithm_type.QO;
        }
        else
        if(algo.equals("BO")){
            algorithm_type=Algorithm_type.BO;
        }
        else
        if(algo.equals("B3F")){
            algorithm_type=Algorithm_type.B3F;
        }
        else
        if(algo.equals("DPBF")){
            algorithm_type=Algorithm_type.DPBF;
        }
        else{
            throw new Exception("Not implemented");
        }
    }

    protected void reset_alpha(double alpha){
        Structure.alpha=alpha;
    }




    protected void read_query() throws SQLException {
        System.out.println("Reading query...");
        Q=new Query();
        Statement stmt=conn.createStatement();
        ResultSet ret;

        ret=stmt.executeQuery("select count(*) from keyword");
        if(ret.next()){
            Q.g=ret.getInt("count(*)");
        }
        for(int i=0;i<Q.g;i++){
            Q.keywords.add(new ArrayList<>());
        }
        ret.close();

        ret=stmt.executeQuery("select * from keymap");
        while(ret.next()){
            int node=ret.getInt("node");
            int key=ret.getInt("key");
            G.key.get(node).add(key);
            Q.keywords.get(key).add(node);
        }
        ret.close();

        stmt.close();
    }


    void pick_query(int query_id) throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet ret = stmt.executeQuery("select keymap.* from keymap,queries where queries.keyword=keymap.key and queries.query=" + query_id);
        Map<Integer, Integer> wordmap = new HashMap<>();
        G.key=new ArrayList<>();
        for(int i=0;i<G.n;i++){
            G.key.add(new LinkedList<>());
        }
        Q = new Query();
        while (ret.next()) {
            int key = ret.getInt("key");
            int node = ret.getInt("node");
            if (!wordmap.containsKey(key)) {
                wordmap.put(key, wordmap.size());
                Q.keywords.add(new ArrayList<>());
            }
            key = wordmap.get(key);
            Q.keywords.get(key).add(node);
            G.key.get(node).add(key);
        }
        Q.g=wordmap.size();
        ret.close();
    }

    protected RetInfo run_algorithm(){
        Tree ans;
        System.out.println("Running algorithm...");

        RetInfo ret=new RetInfo();

        double[]case_time=new double[9];
        switch (algorithm_type){
            case EO: algorithm=new EO(G,Q);break;
            case QO: algorithm=new QO(G,Q);break;
            case B3F: algorithm=new B3F(G,Q);break;
            case BO: algorithm=new BO(G,Q);break;
            case DPBF: algorithm=new DPBF(G,Q);break;
            default: System.err.println("Not implemented");
        }
        ans=algorithm.solve();
        case_time[0]=(System.currentTimeMillis()-algorithm.start)/1000D;
        try{
            ans.validate(Q);
        } catch (Exception e) {
            System.err.println("Validate failed!");
            e.printStackTrace();
        }
        ret.ans=ans;
        algorithm=null;
        if(Config.com_more&&(algorithm_type!=Algorithm_type.B3F&&case_time[0]<25)) {
            for (int i = 1; i < 9; i++) {
                switch (algorithm_type){
                    case EO: algorithm=new EO(G,Q);break;
                    case QO: algorithm=new QO(G,Q);break;
                    case B3F: algorithm=new B3F(G,Q);break;
                    case BO: algorithm=new BO(G,Q);break;
                    case DPBF: algorithm=new DPBF(G,Q);break;
                    default: System.err.println("Not implemented");
                }
                ans = algorithm.solve();
                case_time[i] = (System.currentTimeMillis() - algorithm.start) / 1000D;
                try{
                    ans.validate(Q);
                } catch (Exception e) {
                    System.err.println("Validate failed!");
                    e.printStackTrace();
                }
                algorithm=null;
            }
            double[]avg=new double[3];
            for(int i=0;i<3;i++){
                for(int j=0;j<3;j++){
                    avg[i]+=case_time[i*3+j];
                }
                avg[i]/=3;
            }
            Arrays.sort(avg);
            System.out.println(Arrays.toString(case_time));
            ret.time=avg[1];
        }else{
            ret.time=case_time[0];
        }




        System.out.println("Finish!");
        System.out.println("The number of graph nodes is "+G.n+", the number of graph edges is "+G.m+".");
        System.out.println("The number of keyword is "+Q.g+".");
        System.out.println("Algorithm: "+ algorithm_type.name());
        System.out.println("alpha= "+Structure.alpha);
        System.out.println("Runtime: "+ret.time+" s.");

        if(algorithm_type!=Algorithm_type.DPBF) {
            ret.ans.cal_cost(G);
        }
        System.out.println("Complete! cost(T)=" + ret.ans.cost+".");
        System.out.println("");



        /*
        Output the anstree

        for (Structure.Edge edge: ans.edges){
            System.out.println(edge.s+" "+ edge.t);
        }
        System.out.println("");
         */

        return ret;
    }
    protected abstract void gen_keyword(int key_id) throws Exception;


    protected void put_raw_query(String filename) throws FileNotFoundException {
        PrintStream out=new PrintStream(filename);
        out.println(Q.g);
        for(int i=0;i<Q.g;i++){
            out.print(Q.keywords.get(i).size());
            out.print(" ");
            for(Integer j:Q.keywords.get(i)){
                out.print(j);
                out.print(" ");
            }
            out.println("");
        }
    }

    protected void read_raw_query(String filename) throws IOException {
        Filereader in=new Filereader(filename);
        Q=new Query();
        Q.g=in.read();
        G.key=new ArrayList<>();
        for(int i=0;i<G.n;i++){
            G.key.add(new LinkedList<>());
        }
        for(int i=0;i<Q.g;i++){
            int m=in.read();
            ArrayList<Integer> keyword =new ArrayList<>();
            for(int j=0;j<m;j++){
                int t=in.read();
                keyword.add(t);
                G.key.get(t).add(i);
            }
            Q.keywords.add(keyword);

        }
    }

    /**
     * Do all things
     */
    void solve(){
        read_data();
        run_algorithm();
        try {
            close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected abstract void read_data();
}

enum Algorithm_type {
    EO,
    QO,
    B3F,
    BO,
    DPBF
}

/*
 * Database name: info
 *
 * Tables:
 *      commit:
 *          format: (id - date - alpha - graph - other)
 *          content: document the information of each experiment
 *                   id: the (id)-th experiment
 *                   date: the date of experiment
 *                   alpha: the value of alpha in this experiment
 *                   graph: the KG used in the experiment
 *                   method1: how to calculate the node weight
 *                   method2: how to calculate the sd
 *                   other: other information
 *
 *      result:
 *          format: (id - runtime - T - RPS - accuracy - n - m - other)
 *          content: document the result of the experiment, including experiment id, the runtime(ms) of the algorithm,
 *                   cost(T), cost(RPS), accuracy,the number of nodes n,the number of edges m, and so on.
 */