package mytools;

public class Debug {
    public static void print(String str){
        if(Config.debug){
            System.out.println(str);
        }
    }

    static long timestamp;
    public static void start(){
        if(Config.debug){
            timestamp=System.currentTimeMillis();
        }
    }

    public static void end(String str){
        if(Config.debug){
            System.out.println(str+" : "+(System.currentTimeMillis()-timestamp)+" ms");
        }
    }



    public static void istart(){
        timestamp=System.currentTimeMillis();
    }

    public static void iend(String str){
        System.out.println(str+" : "+(System.currentTimeMillis()-timestamp)+" ms");
    }
}
