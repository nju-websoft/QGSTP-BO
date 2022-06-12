package mytools;

public class Dcmp {
    static final double eps=1e-8;
    public static int cmp(final double a,final double b){
        if(a>b+eps){
            return 1;
        }
        if(a+eps<b){
            return -1;
        }
        return 0;
    }
}
