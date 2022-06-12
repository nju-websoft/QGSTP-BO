package graphtheory;

import graphtheory.Structure.RPS;
import graphtheory.Structure.Graph;
import mytools.Config;

/**
 * The algorithm to solve the QGST problem.
 * @Author qkoqhh
 * @Date 2020-11-9
 */
abstract public class Algorithm {
    final static protected double inf=Double.MAX_VALUE;
    final static protected double eps=1e-8;

    /**
     * Timeout parameter
     */
    public long start;
    static protected final long timeout= Config.timeout>0?Config.timeout*1000:Long.MAX_VALUE;

    protected Graph G;
    protected Query Q;

    public Algorithm(Graph G,Query Q){
        start=System.currentTimeMillis();
        this.G=G;
        this.Q=Q;
    }

    public abstract Structure.Tree solve();
}
