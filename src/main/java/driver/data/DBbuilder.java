package driver.data;

import mytools.SQL_batch;
import driver.ProcessBase;

import java.sql.*;

/**
 * Transform the KG into database
 *
 * Database design follow:
 *
 * Database name:  [name of KG]
 * Tables:
 *      nodes:
 *          format: (id - name - weight)
 *          content: the map from resource to node id, and store the weight of each node
 *
 *      edges:
 *          format: (x - y)
 *          content: store the edges of graph, purely some undirected edges
 *
 *      keyword:
 *          format: (id - keyword)
 *          content: store the keyword and its id
 *
 *      keymap:
 *          format: (key - node)
 *          content: store the map from keyword to node
 *
 *      nodevec:
 *          format: (id - dimension - value)
 *          content: Store the map from node to vector.
 *                   This table exists when using RDF2Vec method because library RDF2Vec is written in Python.(Fuck You)
 *                   And the table is created in genvec.py.
 *
 *
 * @Author qkoqhh
 * @Date 2020-10-29
 */
public class DBbuilder extends ProcessBase {
    // connection to database
    Connection conn;
    SQL_batch nodes,edges,keyword,keymap;

    /**
     * Create the experiment database, and initialize the tables: nodes, edges, keyword, keymap
     * The connection to database is hold on, remember to close it
     * @param dbname the name of database
     */
    DBbuilder(String dbname){
        super(dbname);
        Statement stmt = null;
        try{
            // regiser JDBC driver
            Class.forName(JDBC_DRIVER);
            System.out.println("Connect to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            System.out.println("Create database "+dbname+"...");
            stmt = conn.createStatement();

            // create database expr-n
            stmt.executeUpdate("create database "+dbname);
            stmt.executeUpdate("use "+dbname);
            // create tables: nodes, edges, keyword, keymap, labels
            stmt.executeUpdate(
                            "create table nodes(" +
                            "    `id` int not null," +
                            "    `name` varchar(1024)," +
                            "    `weight` double," +
                            "    primary key (`id`)" +
                            ")"
            );
            stmt.executeUpdate(
                            "create table edges(" +
                            "    `x` int not null," +
                            "    `y` int not null" +
                            ")"
            );
            stmt.executeUpdate(
                            "create table keyword(" +
                            "   `id` int not null," +
                            "    `keyword` varchar(1024) not null," +
                            "    primary key (`id`)" +
                            ")"
            );
            stmt.executeUpdate(
                            "create table keymap(" +
                            "   `key` int not null," +
                            "    `node` int not null," +
                            "    primary key(`key`,`node`)" +
                            ")"
            );

            nodes=new SQL_batch(conn,"insert into nodes values (?,?,?)");
            edges=new SQL_batch(conn,"insert into edges values (?,?)");
            keyword=new SQL_batch(conn,"insert into keyword values (?,?)");
            keymap=new SQL_batch(conn,"insert into keymap values (?,?)");
            conn.setAutoCommit(false);
            stmt.close();

        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
    }

    /*
    protected void add_node(int x,String name,double w) throws SQLException {
        nodes.setInt(1,x);
        nodes.setString(2,name);
        nodes.setDouble(3,w);
        nodes.addBatch();
        nodes_cnt++;
        if(nodes_cnt%batch_size==0){
            System.out.println("Add node "+nodes_cnt);
            nodes.executeBatch();
            conn.commit();
            nodes.clearBatch();
        }
    }
    @Deprecated
    protected void add_edge(int x,int y) throws SQLException {
        edges.setInt(1,x);
        edges.setInt(2,y);
        edges.addBatch();
        edges_cnt++;
        if(edges_cnt%batch_size==0){
            System.out.println("Add edge "+edges_cnt);
            edges.executeBatch();
            conn.commit();
            edges.clearBatch();
        }
    }
    @Deprecated
    protected void add_keyword(int x, String name) throws SQLException {
        keyword.setInt(1,x);
        keyword.setString(2,name);
        keyword.addBatch();
        keyword_cnt++;
        if(keyword_cnt%batch_size==0){
            System.out.println("Add keyword "+keyword_cnt);
            keyword.executeBatch();
            conn.commit();
            keyword.clearBatch();
        }
    }
    @Deprecated
    protected void add_keymap(int x,int y) throws SQLException {
        keymap.setInt(1,x);
        keymap.setInt(2,y);
        keymap.addBatch();
        keymap_cnt++;
        if(keymap_cnt%batch_size==0){
            System.out.println("Add keymap "+keymap_cnt);
            keymap.executeBatch();
            conn.commit();
            keymap.clearBatch();
        }
    }
     */

    /*
    public void close() throws SQLException {
        nodes.executeBatch();
        conn.commit();
        nodes.clearBatch();
        edges.executeBatch();
        conn.commit();
        edges.clearBatch();;
        keyword.executeBatch();
        conn.commit();
        keyword.clearBatch();
        keymap.executeBatch();
        conn.commit();
        keymap.clearBatch();

        conn.close();
    }
    */




}



