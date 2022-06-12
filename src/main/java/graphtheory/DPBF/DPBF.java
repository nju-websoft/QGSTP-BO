package graphtheory.DPBF;

import graphtheory.Algorithm;
import graphtheory.Query;
import graphtheory.Structure.*;
import org.jgrapht.alg.util.Pair;

import java.lang.reflect.Parameter;
import java.util.*;

public class DPBF extends Algorithm {
    public DPBF(Graph G, Query Q) {
        super(G, Q);
        long start=System.currentTimeMillis();
        d=new double[1<<Q.g][G.n];
        p=new Pair[1<<Q.g][G.n];
        v=new boolean[1<<Q.g][G.n];
        for(int i=0;i<(1<<Q.g);i++){
            Arrays.fill(d[i],inf);
        }
        m=(1<<Q.g)-1;
        long end=System.currentTimeMillis();
        System.out.println("init time: "+(end-start)/1000D+" s");
    }

    double[][]d;
    boolean[][]v;
    Pair<Integer,Integer>[][]p;
    int m;
    PriorityQueue<Pair<Integer,Integer>>q=new PriorityQueue<>(Comparator.comparing(o->d[o.getFirst()][o.getSecond()]));

    Tree ans;

    void construct(Pair<Integer,Integer>x){
        Pair<Integer,Integer>pre=p[x.getFirst()][x.getSecond()];
        if(pre==null){
            return;
        }
        if(!pre.getSecond().equals(x.getSecond())){
            ans.edges.add(new Edge(pre.getSecond(),x.getSecond()));
            ans.map.put(pre.getSecond(),ans.map.size());
            construct(pre);
        }else{
            construct(pre);
            construct(Pair.of(x.getFirst()^pre.getFirst(),x.getSecond()));
        }
    }

    @Override
    public Tree solve() {
        for(int i=0;i<G.n;i++){
            int mask=0;
            for(Integer j:G.key.get(i)){
                mask|=1<<j;
            }
            if(mask>0){
                d[mask][i]=G.a[i];
                q.add(Pair.of(mask,i));
            }
        }
        while(!q.isEmpty()){
            Pair<Integer,Integer> t=q.poll();
            if(t.getFirst()==m){
                ans=new Tree();
                ans.cost=d[t.getFirst()][t.getSecond()];
                ans.map.put(t.getSecond(),0);
                construct(t);
                ans.n=ans.map.size();
                return ans;
            }
            if(v[t.getFirst()][t.getSecond()]){
                continue;
            }
            if(System.currentTimeMillis()-start>timeout){
                continue;
            }
            v[t.getFirst()][t.getSecond()]=true;
            for(Hop j:G.edges.get(t.getSecond())){
                if(d[t.getFirst()][j.t]>d[t.getFirst()][t.getSecond()]+G.a[j.t]){
                    d[t.getFirst()][j.t]=d[t.getFirst()][t.getSecond()]+G.a[j.t];
                    p[t.getFirst()][j.t]=t;
                    q.add(Pair.of(t.getFirst(),j.t));
                }
            }
            for(int j=m^t.getFirst();j>0;j=(j-1)&m){
                if(d[j|t.getFirst()][t.getSecond()]>d[j][t.getSecond()]+d[t.getFirst()][t.getSecond()]-G.a[t.getSecond()]){
                    d[j|t.getFirst()][t.getSecond()]=d[j][t.getSecond()]+d[t.getFirst()][t.getSecond()]-G.a[t.getSecond()];
                    p[j|t.getFirst()][t.getSecond()]=t;
                    q.add(Pair.of(j|t.getFirst(),t.getSecond()));
                }
            }
        }
        return ans;
    }
}
