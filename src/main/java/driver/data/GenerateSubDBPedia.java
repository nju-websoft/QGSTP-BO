package driver.data;

import mytools.SQL_batch;
import driver.ProcessBase;
import mytools.Validator;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static java.lang.Integer.min;

/**
 * Generate the subgraph from the raw graph(dbpedia)
 * First pick the node with max degree and find the component from it.(See @bfs)
 * Then extract all the queries still valid in the subgraph
 * We map the node id and query id, but we reserve the keyword id.
 * To store the subgraph and queries, we store the graph in the database, described in follow
 *
 *
 * Database name:  [name of KG]_[size]
 * eg. dbpedia_50k
 *
 * Tables:
 *      nodes:
 *          format: (id - name - weight)
 *          content: the map from resource to node id, and store the weight of each node
 *
 *      edges:
 *          format: (x - y)
 *          content: store the edges of graph, purely some undirected edges
 *
 *      keymap:
 *          format: (key - node)
 *          content: store the map from keyword to node
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
 *      subnodemap:
 *          format: (id - origin)
 *          content: store the map from subgraph node id(id) to origin graph node id(origin)
 *
 *      subqueriesmap:
 *          format: (query - origin)
 *          content: store the map from subgraph query id(id) to origin graph query id(origin)
 *
 *
 * @Author qkoqhh
 * @Date 2020-12-9
 */
public class GenerateSubDBPedia extends ProcessBase {

    public GenerateSubDBPedia(String graphname) {
        super(graphname);
        connect();
        try {
            read_graph();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    String subdbname;
    void init() throws SQLException {
        Statement stmt=conn.createStatement();
        if(size>=1000000){
            subdbname=dbname+"_"+(size/1000000)+"M";
        }else{
            subdbname=dbname+"_"+(size/1000)+"K";
        }
        stmt.executeUpdate("create database "+subdbname);
        stmt.executeUpdate("use "+subdbname);
        stmt.executeUpdate(
                "create table subnodemap(" +
                "    id int unique," +
                "    origin int unique," +
                "    primary key (id)" +
                ")"
        );
        subnodemap=new SQL_batch(conn,"insert into subnodemap values (?,?)");
    }


    SQL_batch subnodemap;

    int loc;
    void find_loc(){
        for(int i=0;i<G.n;i++){
            if(G.edges.get(i).size()>G.edges.get(loc).size()){
                loc=i;
            }
        }
    }
    int[]v;
    int cnt,size;
    void bfs() throws Exception {
        Queue<Integer> q=new LinkedList<>();
        int[]d=new int[G.n];
        q.add(loc);v[loc]=cnt=1;
        subnodemap.add(0,loc);
        final int MAX_OUT=size/20;
        final int MAX_HOP=5;
        while(!q.isEmpty()){
            int t=q.poll();
            if(d[t]>=MAX_HOP){
                continue;
            }
            for(int k=0;k<min(G.edges.get(t).size(),MAX_OUT);k++){
                int j=G.edges.get(t).get(k).t;
                if(v[j]==0){
                    cnt++;
                    v[j]=cnt;
                    d[j]=d[t]+1;
                    subnodemap.add(v[j]-1,j);
                    if(cnt==size){
                        break;
                    }
                    q.add(j);
                }
            }
            if(cnt==size){
                break;
            }
        }
    }
    public void generate(int size) throws Exception {
        this.size=size;
        System.out.println("Create database...");
        init();
        System.out.println("Selecting nodes");
        cnt=0;
        v=new int[G.n];
        conn.setAutoCommit(false);
        find_loc();
        bfs();
        subnodemap.close();
        conn.setAutoCommit(true);
        System.out.println(cnt);


        System.out.println("Generate data...");
        Statement stmt=conn.createStatement();
        //stmt.executeUpdate("create table queries " +
        //        "select * from "+dbname+".queries");
        //stmt.executeUpdate("create table keyword " +
        //        "select * from "+dbname+".keyword");
        stmt.executeUpdate("create table nodes " +
                "select A.id id,B.name name,B.weight weight from subnodemap A,"+dbname+".nodes B where A.origin=B.id");
        stmt.executeUpdate("create table edges " +
                "select A.id x,C.id y from subnodemap A,"+dbname+".edges B,subnodemap C where A.origin=B.x and B.y=C.origin");
        stmt.executeUpdate("create table labels " +
                "select A.id id,B.label label from subnodemap A,"+dbname+".labels B where A.origin=B.id");
        stmt.executeUpdate("create table nodetype "+
                "select A.id node,B.type type from subnodemap A,"+dbname+".nodetype B where A.origin=B.node");
        //stmt.executeUpdate("create table keymap " +
        //        "select B.key `key`,A.id node from subnodemap A,"+dbname+".keymap B where A.origin=B.node");

        /*
        From now we deprecate the table above: queries,keyword,keymap. And to store all the valid queries, we create new table by following sql statement:

        create table subqueriesmap(
            select row_number() over(order by `query`)-1   `query`, `query`  `origin` from dbpedia.queries
                where `query` in(
		            select `query` from dbpedia.queries
                		where `query` not in(
                			select distinct(`query`) from dbpedia.queries
                    			where keyword not in(
                    				select distinct(dbpedia.keymap.`key`) from dbpedia.keymap join subnodemap on subnodemap.origin=dbpedia.keymap.node
			                    )
		                )
           		group by `query`
		        having count(`keyword`)>=2 and count(`keyword`)<=6
                )
                group by `query`
        );
create table queries(select subqueriesmap.`query` `query`,dbpedia.queries.keyword keyword from dbpedia.queries join subqueriesmap on dbpedia.queries.`query`=subqueriesmap.origin);
create table keymap(select queries.keyword `key`,subnodemap.id node from (dbpedia.keymap join queries on dbpedia.keymap.`key`=queries.keyword)  join  subnodemap on dbpedia.keymap.node=subnodemap.origin);
         */
        stmt.executeUpdate("create table subqueriesmap( " +
                "select row_number() over(order by `query`)-1   `query`, `query`  `origin` from "+dbname+".queries" +
                "   where `query` in(" +
                "       select `query` from "+dbname+".queries" +
                "           where `query` not in(" +
                "               select distinct(`query`) from "+dbname+".queries" +
                "                   where keyword not in(" +
                "                       select distinct(dbpedia.keymap.`key`) from "+dbname+".keymap join subnodemap on subnodemap.origin="+dbname+".keymap.node" +
                "                   )" +
                "           )" +
                "           group by `query`" +
                "           having count(`keyword`)>=2 and count(`keyword`)<=6" +
                "       )" +
                "   group by `query`" +
                ")");
        stmt.executeUpdate("create table queries(" +
                "select subqueriesmap.`query` `query`,"+dbname+".queries.keyword keyword from "+dbname+".queries join subqueriesmap on "+dbname+".queries.`query`=subqueriesmap.origin )");
        stmt.executeUpdate("create table keymap(" +
                "select queries.keyword `key`,subnodemap.id node from ("+dbname+".keymap join queries on "+dbname+".keymap.`key`=queries.keyword)  join  subnodemap on "+dbname+".keymap.node=subnodemap.origin )");
    }

    public static void main(String[]args){
        try{
            GenerateSubDBPedia here=new GenerateSubDBPedia("dbpedia");
            here.connect();
            here.read_graph();
//            Validator.comp(here.G);
            here.generate(1000);
            here.generate(50000);
            here.generate(6000000);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
