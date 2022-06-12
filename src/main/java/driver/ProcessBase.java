package driver;

import graphtheory.Query;
import graphtheory.Structure.*;
import graphtheory.semantic_distance.Jaccard;
import graphtheory.semantic_distance.Rdf2Vec_angular;
import mytools.Config;

import java.sql.*;
import java.util.LinkedList;

/**
 * Extract the common part of all the process class
 * Before initialize the class, please initialize class @Config
 * It is not allowed to implement the main function in this class and its subclass
 * @Date 2020-12-22
 * @Author qkoqhh
 */
abstract public class ProcessBase {
    // JDBC Driver and Database URL
    protected static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    protected static final String IP=Config.IP;
    public static final String PORT=Config.PORT;
    protected static final String DB_URL = "jdbc:mysql://"+IP+":"+PORT+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true&tcpKeepAlive=true";
    // username and password for database login
    protected static final String USER = Config.USER;
    protected static final String PASS = Config.PASS;

    public static final double inf=Double.MAX_VALUE;
    public static final double eps=1e-8;

    // connection to database
    static protected Connection conn;

    protected final String dbname;

    protected ProcessBase(String graphname) {
        dbname=graphname;
    }

    protected void connect(){
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connect to database "+dbname+"...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            Statement stmt=conn.createStatement();
            stmt.executeUpdate("use "+dbname);
            stmt.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }


    public void close() throws SQLException {
        conn.close();
    }


    public Graph G;
    public Query Q;

    /**
     * Read the graph from database to @G
     * Please don't change @G after reading the graph
     * Before reading graph, please call @connect()
     * @throws SQLException
     */
    protected void read_graph() throws SQLException {
        System.out.println("Reading graph...");
        conn.setAutoCommit(false);
        conn.setReadOnly(true);
        G=new Graph();
        Statement stmt=conn.createStatement();
        ResultSet ret;
        ret=stmt.executeQuery("select count(*) from nodes");
        if(ret.next()){
            G.n=ret.getInt("count(*)");
        }
        conn.commit();
        ret.close();
        stmt.close();

        PreparedStatement pstmt=conn.prepareStatement("select id,weight from nodes",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        pstmt.setFetchSize(100000);
        pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        ret=pstmt.executeQuery();
        G.a=new double[G.n];
        for(int i=0;i<G.n;i++){
            G.edges.add(new LinkedList<>());
        }

        while(ret.next()){
            int id=ret.getInt("id");
            G.a[id]=ret.getDouble("weight");
        }
        conn.commit();
        ret.close();
        pstmt.close();

        pstmt=conn.prepareStatement("select * from edges",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        pstmt.setFetchSize(100000);
        pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        ret=pstmt.executeQuery();
        while(ret.next()){
            G.m++;
            int x=ret.getInt("x");
            int y=ret.getInt("y");
            G.edges.get(x).add(new Hop(y));G.edges.get(y).add(new Hop(x));
        }
        conn.commit();
        ret.close();
        pstmt.close();

        try {
            read_hub_label();
        }catch (Exception ignored){
        }

        switch (Config.SD){
            case "Jaccard": G.sd=new Jaccard(conn,G.n);break;
            case "Rdf2Vec": G.sd=new Rdf2Vec_angular(conn,G.n);break;
            default: System.err.println("Notice: No SD function found");
        }
        try {
            set_edge_weight();
        }catch (Exception ignored){
        }

        conn.setReadOnly(false);
        conn.setAutoCommit(true);
    }

    private int subgraph_find(int x){
        if(G.f[x]==x){
            return x;
        }
        return G.f[x]=subgraph_find(G.f[x]);
    }

    void read_hub_label() throws SQLException {
        ResultSet ret;
        int tot=0;
        Statement stmt=conn.createStatement();
        ret=stmt.executeQuery("select count(*) from hub_sal");
        if(ret.next()){
            tot=ret.getInt("count(*)");
        }
        conn.commit();
        ret.close();
        ret=stmt.executeQuery("select count(*) from hub_sd");
        if(ret.next()){
            tot+=ret.getInt("count(*)");
        }
        conn.commit();
        ret.close();
        stmt.close();
        G.hub_node_pool=new int[tot];
        G.hub_value_pool=new double[tot];
        G.hub_sd_consor=new int[G.n];
        G.hub_sal_consor=new int[G.n];
        G.hub_sd_size=new int[G.n];
        G.hub_sal_size=new int[G.n];


        tot=0;

        PreparedStatement pstmt;

        System.out.println("Reading hub label sd...");
        pstmt=conn.prepareStatement("select * from hub_sd",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        pstmt.setFetchSize(Integer.MIN_VALUE);
        pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        ret=pstmt.executeQuery();
        while(ret.next()){
            int u=ret.getInt("u");
            int v=ret.getInt("v");
            double w=ret.getDouble("w");
            G.hub_node_pool[tot]=v;
            G.hub_value_pool[tot]=w;
            if(G.hub_sd_size[u]==0){
                G.hub_sd_consor[u]=tot;
            }
            G.hub_sd_size[u]++;
            tot++;
        }
        conn.commit();
        ret.close();
        pstmt.close();

        System.out.println("Reading hub label sal...");
        pstmt=conn.prepareStatement("select * from hub_sal",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        pstmt.setFetchSize(Integer.MIN_VALUE);
        pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        ret=pstmt.executeQuery();
        while(ret.next()){
            int u=ret.getInt("u");
            int v=ret.getInt("v");
            double w=ret.getDouble("w");
            G.hub_node_pool[tot]=v;
            G.hub_value_pool[tot]=w;
            if(G.hub_sal_size[u]==0){
                G.hub_sal_consor[u]=tot;
            }
            G.hub_sal_size[u]++;
            tot++;
        }
        conn.commit();
        ret.close();
        pstmt.close();
    }

    void set_edge_weight(){
        System.out.println("Setting edge weight...");
        for(int i=0;i<G.n;i++){
            for(Hop j:G.edges.get(i)){
                j.w=G.sd.cal(j.t,i);
            }
        }
        if("Jaccard".equals(Config.SD)) {
            G.f = new int[G.n];
            for (int i = 0; i < G.n; i++) {
                G.f[i] = i;
            }
            for (int i = 0; i < G.n; i++) {
                for (Hop j : G.edges.get(i)) {
                    if (j.w < eps) {
                        int x = subgraph_find(i), y = subgraph_find(j.t);
                        if (x != y) {
                            if (G.hub_sd_size[y] > 0) {
                                G.f[x] = y;
                            } else {
                                G.f[y] = x;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < G.n; i++) {
                G.f[i] = subgraph_find(i);
            }
        }
    }
}
