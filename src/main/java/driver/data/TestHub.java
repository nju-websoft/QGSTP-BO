package driver.data;

import driver.work.Run;
import graphtheory.Structure.*;
import mytools.Config;
import org.jgrapht.alg.util.Pair;
import driver.ProcessBase;

import java.sql.SQLException;
import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class TestHub extends ProcessBase {
    public TestHub(String graphname) throws SQLException {
        super(graphname);
        connect();
        read_graph();
    }
    Random rand=new Random();


    double[]d;
    boolean[]v;
    void dij_sal(int s){
        PriorityQueue<Pair<Integer,Double>>q=new PriorityQueue<>(Comparator.comparing(Pair::getSecond));
        d=new double[G.n];
        v=new boolean[G.n];
        Arrays.fill(d,inf);
        d[s]=G.a[s];q.add(new Pair<>(s,d[s]));
        while(!q.isEmpty()){
            int t=q.poll().getFirst();
            if(v[t]){
                continue;
            }
            v[t]=true;
            for(Hop j:G.edges.get(t)){
                if(d[j.t]>d[t]+G.a[j.t]){
                    d[j.t]=d[t]+G.a[j.t];
                    q.add(new Pair<>(j.t,d[j.t]));
                }
            }
        }
    }

    double query_sal(int s, int t){
        double ans=inf;
        for(int x=G.hub_sal_consor[s],y=G.hub_sal_consor[t],n=x+G.hub_sal_size[s],m=y+G.hub_sal_size[t];x<n&&y<m;){
            if(G.hub_node_pool[x]==G.hub_node_pool[y]) {
                ans = min(ans, G.hub_value_pool[x] + G.hub_value_pool[y] - G.a[G.hub_node_pool[x]]);
                x++;
                y++;
            }else if(G.hub_node_pool[x]<G.hub_node_pool[y]){
                x++;
            }else{
                y++;
            }
        }
        return ans;
    }

    public void source_sample_sal(int n, int m) throws Exception {
        for(int i=0;i<n;i++){
            int s=abs(rand.nextInt())%G.n;
            dij_sal(s);
            for(int j=0;j<m;j++){
                int t= abs(rand.nextInt())%G.n;
                if(Math.abs(d[t]-query_sal(s,t))>eps){
                    System.err.println(s+" "+t);
                    System.err.println("Hub Label sal Wrong!!!! "+"The real distance is "+d[t]+" while HL calculate "+query_sal(s,t));
                }
            }
        }
    }

    void dij_sd(int s){
        PriorityQueue<Pair<Integer,Double>>q=new PriorityQueue<>(Comparator.comparing(Pair::getSecond));
        d=new double[G.n];
        v=new boolean[G.n];
        Arrays.fill(d,inf);
        d[s]=0;
        q.add(new Pair<>(s,d[s]));
        while(!q.isEmpty()){
            int t=q.poll().getFirst();
            if(v[t]){
                continue;
            }
            v[t]=true;
            for(Hop j:G.edges.get(t)){
                if(d[j.t]>d[t]+j.w){
                    d[j.t]=d[t]+j.w;
                    q.add(new Pair<>(j.t,d[j.t]));
                }
            }
        }
    }

    double query_sd(int s, int t){
        double ans=inf;
        for(int x=G.hub_sd_consor[s],y=G.hub_sd_consor[t],n=x+G.hub_sd_size[s],m=y+G.hub_sd_size[t];x<n&&y<m;){
            if(G.hub_node_pool[x]==G.hub_node_pool[y]) {
                ans = min(ans, G.hub_value_pool[x] + G.hub_value_pool[y]);
                x++;
                y++;
            }else if(G.hub_node_pool[x]<G.hub_node_pool[y]){
                x++;
            }else{
                y++;
            }
        }
        return ans;
    }

    public void source_sample_sd(int n, int m) throws Exception {
        if(Config.database.startsWith("lubm")){
            G.f=new int[G.n];
            for (int i=0;i<G.n;i++){
                G.f[i]=i;
            }
        }
        for(int i=0;i<n;i++){
            int s=abs(rand.nextInt())%G.n;
            dij_sd(s);
            for(int j=0;j<m;j++){
                int t= abs(rand.nextInt())%G.n;
                if(Math.abs(d[t]-query_sd(G.f[s],G.f[t]))>eps){
                    System.err.println(s+" "+t);
                    System.err.println("Hub Label sd Wrong!!!! "+"The real distance is "+d[t]+" while HL calculate "+query_sd(G.f[s],G.f[t]));
                }
            }
        }
    }

}
