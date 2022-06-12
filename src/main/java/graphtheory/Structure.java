package graphtheory;

import graphtheory.SemKSG.SemKSG;
import graphtheory.semantic_distance.SD;
import mytools.Debug;

import java.security.PublicKey;
import java.util.*;

import static graphtheory.Algorithm.eps;


/**
 * Various graph structure
 * @Author qkoqhh
 * @Date 2020-10-12
 */
public class Structure{
    public static double alpha;
    final static double inf=Double.MAX_VALUE;
    public static class Edge {
        public int s, t;
        public Edge(int x,int y){
            s=x; t=y;
        }
    }

    public static class Graph {
        public int n;
        public int m;
        // node weight
        public double[]a;
        public SD sd;
        public ArrayList<LinkedList<Hop>> edges= new ArrayList<>();

        // The key of each node
        public List<LinkedList<Integer>>key;

        // Hub label
        public int[] hub_sd_consor,hub_sal_consor,hub_sal_size,hub_sd_size;
        public int[] hub_node_pool;
        public double[] hub_value_pool;

        // Consider some of edge weight is zero, we compress the graph here
        public int[]f;
    }

    public static class Hop{
        public double w;
        public int t;
        public Hop(int x){t=x;}
        public Hop(int t,double w){
            this.t=t;
            this.w=w;
        }
    }

    public static class Path{
        public double cost;
        public List<Integer> nodes=new ArrayList<>();
    }

    public static class RPS{
        public double cost;
        public ArrayList<Path> paths=new ArrayList<Path>();
    }

    static int find(List<Integer> f,int x){
        if(f.get(x)==x){
            return x;
        }
        f.set(x,find(f,f.get(x)));
        return f.get(x);
    }

    public static class Tree{
        public int n;
        public double cost;
        public List<Edge> edges=new ArrayList<Edge>();
        public HashMap<Integer,Integer> map=new HashMap<>();

        public Tree(Graph G,RPS rps){
            int tot=0;
            List<Integer> f=new ArrayList<Integer>();

            //edges=new ArrayList<Edge>();
            for(Path path :rps.paths){
                int m= path.nodes.size();
                for(int i=0;i<m;i++){
                    if(!map.containsKey(path.nodes.get(i))){
                        map.put(path.nodes.get(i),tot);
                        f.add(tot);
                        tot++;
                    }
                }
                m--;
                for(int i=0;i<m;i++){
                    int x=find(f,map.get(path.nodes.get(i))),y=find(f,map.get(path.nodes.get(i+1)));
                    if(x==y){
                        continue;
                    }
                    f.set(x,y);
                    edges.add(new Edge(path.nodes.get(i),path.nodes.get(i+1)));
                }
            }
            n=map.size();
            if(n==1){
                return;
            }

            // erase additional nodes
            List<Integer>count=new ArrayList<>();
            Map<Integer,Set<Integer>>edges_map=new HashMap<>();
            Queue<Integer>leaf=new LinkedList<>();
            for (Edge e:edges){
                if(!edges_map.containsKey(e.s)){
                    edges_map.put(e.s,new HashSet<>());
                }
                if(!edges_map.containsKey(e.t)){
                    edges_map.put(e.t,new HashSet<>());
                }
                edges_map.get(e.s).add(e.t);
                edges_map.get(e.t).add(e.s);
            }
            for(Integer node:map.keySet()){
                for(Integer key:G.key.get(node)){
                    while(count.size()<=key){
                        count.add(0);
                    }
                    count.set(key,count.get(key)+1);
                }
                if(edges_map.get(node).size()==1){
                    leaf.add(node);
                }
            }

            while(!leaf.isEmpty()){
                int t=leaf.poll();
                boolean flag=false;
                for (Integer key:G.key.get(t)){
                    if(count.get(key)==1){
                        flag=true;
                    }
                }
                if (!flag){
                    map.remove(t);
                    for (Integer j:edges_map.get(t)){
                        edges_map.get(j).remove(t);
                        if(edges_map.get(j).size()==1){
                            leaf.add(j);
                        }
                    }
                    edges_map.remove(t);
                    for (Integer key:G.key.get(t)){
                        count.set(key,count.get(key)-1);
                    }
                }
            }
            edges=new LinkedList<>();
            for(Map.Entry<Integer,Set<Integer>>entry:edges_map.entrySet()){
                for(Integer j:entry.getValue()){
                    if(entry.getKey()<j){
                        edges.add(new Edge(entry.getKey(),j));
                    }
                }
            }
            if(map.size()==1){
                map.put((Integer) map.keySet().toArray()[0],0);
                n=1;
            }else {
                map = new HashMap<>();
                for (Edge e : edges) {
                    if (!map.containsKey(e.t)) {
                        map.put(e.t, map.size());
                    }
                    if (!map.containsKey(e.s)) {
                        map.put(e.s, map.size());
                    }
                }
                n = map.size();
            }
        }

        public Tree() {
            cost= inf;
        }

        public void cal_cost(Graph G){
            cost=0;
            List<Integer>nodes=new ArrayList<>();
            nodes.addAll(map.keySet());
            for(Integer i: nodes){
                cost+=alpha*G.a[i];
            }
            for(int i=0;i<n;i++) {
                for (int j = i + 1; j < n; j++) {
                    cost += (1 - alpha) * G.sd.cal(nodes.get(i), nodes.get(j));
                }
            }
        }

        public void validate(Query Q) throws Exception {
            List<Integer>f=new ArrayList<>();
            for(int i=0;i<n;i++){
                f.add(i);
            }
            for(Edge e:edges) {
                if (!map.containsKey(e.s)) {
                    throw new Exception("The tree doesn't contain this edge");
                }
                if (!map.containsKey(e.t)) {
                    throw new Exception("The tree doesn't contain this edge");
                }
                int x = find(f, map.get(e.s)), y = find(f, map.get(e.t));
                if (x == y) {
                    throw new Exception("Find Loop");
                }
                f.set(x, y);
            }
            if(n>1) {
                int root = find(f, 0);
                for (int i = 0; i < n; i++) {
                    if (find(f, i) != root) {
                        throw new Exception("Not connected");
                    }
                }
            }
            for(List<Integer> keyword: Q.keywords){
                boolean v=false;
                for(Integer j:keyword) {
                    if (map.containsKey(j)) {
                        v = true;
                        break;
                    }
                }
                if(!v){
                    throw new Exception("Not contain all the keyword");
                }
            }
        }
    }
}




