package mytools;

import driver.ProcessBase;

import java.lang.reflect.Field;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Enabled by config option "record each query"
 * When the option enabled, we first create a database ahead. The class @Info will be parsed and the database will generate the column according to the variable.
 * And then after each experiment, we fill the class and store in the database.
 * @Date 2020-12-28
 * @Author qkoqhh
 */
public class Info {
    /**
     * Something for connecting to database
     */
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String IP=Config.IP;
    static final String PORT=Config.PORT;
    static final String DB_URL = "jdbc:mysql://"+IP+":"+ PORT +"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    /**
     * username and password for database login
     */
    static final String USER = Config.USER;
    static final String PASS = Config.PASS;
    static final String dbname="info";
    static String tablename;
    static Connection conn;


    /**
     * The information to record
     */
    public String graphname;
    public double alpha;
    public boolean PLB;
    public boolean PUB;
    public boolean IMH;
    public boolean IRR;

    public String algorithm;
    public int data_id;
    public int g;
    public double runtime;
    public double cost;
    public List<Integer> anstree;
    public String date;
    /**
     * record other information, for example the way to generate query(default fixed query)
     */
    public String comment;

    String type(String s){
        if(s.contains(".")){
            return "varchar(1024)";
        }
        return s;
    }

    public void init(){
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connect to database "+dbname+"...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            Statement stmt=conn.createStatement();
            stmt.executeUpdate("use "+dbname);
            if(Config.expr==-1) {
                ResultSet ret = stmt.executeQuery("select count(*) from information_schema.tables where `table_schema`='info'");
                if (ret.next()) {
                    tablename = "info_" + ret.getInt("count(*)");
                }
                Class c=this.getClass();
                Field[] fields=c.getFields();
                String sql="create table "+ tablename + "(";
                for(Field field:fields){
                    sql=sql+field.getName()+" "+type(field.getType().getTypeName())+",";
                }
                sql=sql.substring(0,sql.length()-1)+")";
                stmt.executeUpdate(sql);
            }else{
                tablename="info_"+Config.expr;
            }
            stmt.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        graphname=Config.database;
        PLB=Config.PLB;
        PUB=Config.PUB;
        IMH=Config.IMH;
        IRR=Config.IRR;

        comment="";
        if(Config.special_query){
            comment+="Special query: "+Config.query_file_name+";";
        }
        if(Config.special_fixed_query){
            comment+="Special fixed query: "+Config.query_id+";";
        }
        if(Config.random_query){
            comment+="Random query;";
        }
        if(Config.fixed_file_query){
            comment+="Fixed file query;";
        }
    }
    public void add(){
        date=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        Class c=this.getClass();
        Field[] fields=c.getFields();
        try {
            String sql="insert into "+tablename+" values (";
            for(Field field:fields){
                if(field.getType()==boolean.class) {
                    sql = sql + "\"" + ((Boolean)field.get(this)?1:0) + "\",";
                }else {
                    sql = sql + "\"" + field.get(this).toString() + "\",";
                }
            }
            sql=sql.substring(0,sql.length()-1)+")";
            Statement stmt=conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (IllegalAccessException | SQLException e) {
            e.printStackTrace();
        }
    }
}
