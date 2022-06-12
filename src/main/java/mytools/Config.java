package mytools;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Config {
    public static boolean debug;

    public static int worker;
    public static String IP;
    public static String PORT;
    public static String USER;
    public static String PASS;
    public static String database;
    public static String SD;
    public static String result_file;

    public static List<Double>alpha_array=new ArrayList<>();

    public static int expr;

    public static int timeout;
    public static boolean com_more;

    public static boolean random_query;
    public static int query_num;

    public static boolean fixed_query;
    public static boolean fixed_file_query;
    public static boolean record_each_query;

    public static boolean special_query;
    public static String query_file_name;

    public static boolean special_fixed_query;
    public static int query_id;

    public static boolean PLB;
    public static boolean PUB;
    public static boolean IMH;
    public static boolean IRR;

    public static boolean algorithm_EO;
    public static boolean algorithm_QO;
    public static boolean algorithm_B3F;
    public static boolean algorithm_BO;
    public static boolean algorithm_DPBF;


    static boolean isTrue(String bool) throws Exception {
        if(bool.equals("TRUE"))return true;
        else if(bool.equals("FALSE"))return false;
        else throw new Exception("Bool Value Error");
    }

    public static String config_filename="config.properties";

    public static void init(String[]args){
    }

    public static void init() throws Exception {
        Properties properties=new Properties();
        try{
            InputStream in=Config.class.getClassLoader().getResourceAsStream(config_filename);
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        debug=isTrue(properties.getProperty("DEBUG"));
        worker=Integer.parseInt(properties.getProperty("WORKER"));
        IP=properties.getProperty("IP");
        PORT=properties.getProperty("PORT");
        USER=properties.getProperty("USER");
        PASS=properties.getProperty("PASS");
        database=properties.getProperty("DATABASE");
        SD=properties.getProperty("SD");
        result_file=properties.getProperty("RESULT_FILE");
        PLB=isTrue(properties.getProperty("PLB"));
        PUB=isTrue(properties.getProperty("PUB"));
        IMH=isTrue(properties.getProperty("IMH"));
        IRR=isTrue(properties.getProperty("IRR"));
        String[]alpha_list=properties.getProperty("ALPHA").split(",");
        expr=Integer.parseInt(properties.getProperty("EXPR"));
        timeout=Integer.parseInt(properties.getProperty("TIMEOUT"));
        com_more=isTrue(properties.getProperty("COM_MORE"));
        for(String str:alpha_list){
            alpha_array.add(Double.parseDouble(str));
        }
        random_query=isTrue(properties.getProperty("RANDOM_QUERY"));
        query_num=Integer.parseInt(properties.getProperty("QUERY_NUM"));
        fixed_query =isTrue(properties.getProperty("FIXED_QUERY"));
        fixed_file_query =isTrue(properties.getProperty("FIXED_FILE_QUERY"));
        record_each_query=isTrue(properties.getProperty("RECORD_EACH_QUERY"));
        special_query=isTrue(properties.getProperty("SPECIAL_QUERY"));
        query_file_name=properties.getProperty("QUERY_FILE_NAME");
        special_fixed_query=isTrue(properties.getProperty("SPECIAL_FIXED_QUERY"));
        query_id=Integer.parseInt(properties.getProperty("QUERY_ID"));
        algorithm_EO=isTrue(properties.getProperty("ALGORITHM_EO"));
        algorithm_QO=isTrue(properties.getProperty("ALGORITHM_QO"));
        algorithm_B3F=isTrue(properties.getProperty("ALGORITHM_B3F"));
        algorithm_BO =isTrue(properties.getProperty("ALGORITHM_BO"));
        algorithm_DPBF=isTrue(properties.getProperty("ALGORITHM_DPBF"));
    }



}
