package graphtheory.semantic_distance;



import graphtheory.Structure;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.exp;

/**
 * The rough method to implement the sd function, just let the weight difference as sd
 * @Author qkoqhh
 * @Date 2020-10-29
 */
public class Rough extends SD {
    double[] a;
    Rough(Structure.Graph G){
        a=G.a;
    }

    @Override
    public double cal(int i, int j) {
        return 1-exp(-abs(a[i]-a[j]));
    }
}
