package graphtheory.B3F;

import graphtheory.Algorithm;
import graphtheory.Query;
import graphtheory.Structure;
import graphtheory.Structure.*;
import mytools.Config;
import mytools.Debug;

import java.util.*;

import static graphtheory.Structure.alpha;
import static java.lang.Math.*;
import static java.util.Collections.sort;


/**
 * The implementation of B3F which can exactly compute the answer of QGST
 * @Author qkoqhh
 * @Date 2020-11-10
 */
public class B3F extends Algorithm {

    final int all;

    public B3F(Graph G, Query Q) {
        super(G, Q);
        enumeration = new List[Q.g];
        index = new int[Q.g];
        all = (1 << (Q.g)) - 1;
    }

    List<Set<Integer>> edgeSet = new ArrayList<>(G.n);


    double[] wt;
    int[] sd;
    int[] node_mask;

    void pre_compute(Set<Integer> enumeration) {
        node_mask = new int[G.n];
        for (int i = 0; i < G.n; i++) {
            for (Integer j : G.key.get(i)) {
                node_mask[i] |= 1 << j;
            }
            Set<Integer> s = new HashSet<>();
            for (Hop j : G.edges.get(i)) {
                s.add(j.t);
            }
            edgeSet.add(i, s);
        }

        wt = new double[1 << Q.g];
        sd = new int[1 << Q.g];
        double[] w = new double[1 << Q.g];
        int[] d = new int[1 << Q.g];
        Arrays.fill(w, inf);
        w[0] = 0;
        Arrays.fill(d, Q.g);
        d[0] = 0;
        for (Integer node : enumeration) {
            for (int j = 0; j <= all; j++) {
                w[j | node_mask[node]] = min(w[j | node_mask[node]], w[j] + G.a[node]);
                d[j | node_mask[node]] = min(d[j | node_mask[node]], d[j] + 1);
            }
        }
        for (int mask = 0; mask <= all; mask++) {
            wt[mask] = w[all];
            sd[mask] = d[all];
            for (int submask = mask; submask > 0; submask = (submask - 1) & mask) {
                wt[mask] = min(wt[mask], w[submask ^ all]);
                sd[mask] = min(sd[mask], d[submask ^ all]);
            }
        }
    }


    static int ddd = 3;


    List<Path>[] enumeration;
    int[] index;

    void dfs(final int k, final B3F_Tree tree) {
        if (k == Q.g) {
            double cost = tree.weight * alpha + (1 - alpha) * tree.cost_sd;

            if (cost < ans.cost) {
                ans = tree.toTree();
                ans.cost = cost;
                if (Config.debug) {
                    ans.cal_cost(G);
                    if (abs(ans.cost - cost) > eps) {
                        System.err.println(cost + " " + ans.cost);
                    }
                }
                while (!set.isEmpty() && set.last().cost >= cost) {
                    set.pollLast();
                }
            }
            return;
        }

        final int x = index[k];
        if ((tree.mask >> x & 1) > 0) {
            dfs(k + 1, tree);
            return;
        }

        for (Path p : enumeration[x]) {
            final B3F_Tree next = new B3F_Tree(tree, p);
            if (next.cost < ans.cost) {
                dfs(k + 1, next);
            }
        }
    }

    Tree ans = new Tree();

    Comparator<Path>com=new Comparator<Path>() {
        @Override
        public int compare(Path o1, Path o2) {
            if(abs(o1.cost-o2.cost)<eps){
                return Integer.compare(o1.hashCode(),o2.hashCode());
            }
            return Double.compare(o1.cost,o2.cost);
        }
    };
    TreeSet<Path> set = new TreeSet<>(com);

    @Override
    public Tree solve() {
        Set<Integer> keyset = new HashSet<>();
        for (List<Integer> keyword : Q.keywords) {
            for (Integer j : keyword) {
                keyset.add(j);
            }
        }
        pre_compute(keyset);

        for (Integer key : keyset) {
            Path path = new Path(key);
            set.add(path);
        }

        Map<Integer, List<Path>>[] pathset = new Map[Q.g];
        for (int i = 0; i < Q.g; i++) {
            pathset[i] = new HashMap<>();
        }


        int cnt = 0;
        while (!set.isEmpty()) {
            Path path = set.pollFirst();
            cnt++;
            Debug.print(set.size() + " " + path.cost + " " + ans.cost + " " + cnt);
            int endpoint = path.nodes[path.nodes.length - 1];


            int mask = 0;
            for (int i : path.nodes) {
                mask |= node_mask[i];
            }
            for (int i = 0; i < Q.g; i++) {
                if ((mask >> i & 1) > 0) {
                    if (!pathset[i].containsKey(endpoint)) {
                        pathset[i].put(endpoint, new ArrayList<>());
                    }
                    pathset[i].get(endpoint).add(path);
                }
            }


            boolean _f = true;
            for (int i = 0; i < Q.g; i++) {
                if (pathset[i].containsKey(endpoint)) {
                    enumeration[i] = pathset[i].get(endpoint);
                    index[i] = i;
                } else {
                    _f = false;
                    break;
                }
            }


            if (_f) {
                for (int i = 0; i < Q.g; i++) {
                    for (int j = i + 1; j < Q.g; j++) {
                        if (enumeration[index[i]].size() > enumeration[index[j]].size()) {
                            int t = index[i];
                            index[i] = index[j];
                            index[j] = t;
                        }
                    }
                }

                dfs(0, new B3F_Tree(path));
            }


            if (path.nodes.length <= ddd) {
                for (Integer j : edgeSet.get(endpoint)) {
                    Path next = new Path(path, j);
                    if (next.cost < ans.cost) {
                        set.add(next);
                    }
                }
            }
        }
        return ans;
    }


    class B3F_Tree {
        double cost, weight, cost_sd;
        List<Integer> nodes = new LinkedList<>();
        List<Edge> edges = new LinkedList<>();
        int mask;

        B3F_Tree(Path path) {
            cost = path.cost;
            cost_sd = path.cost_sd;
            mask = node_mask[path.nodes[0]];
            nodes.add(path.nodes[0]);
            weight = G.a[path.nodes[0]];
            for (int i = 1; i < path.nodes.length; i++) {
                weight += G.a[path.nodes[i]];
                mask |= node_mask[path.nodes[i]];
                nodes.add(path.nodes[i]);
                edges.add(new Edge(path.nodes[i - 1], path.nodes[i]));
            }
        }

        B3F_Tree(B3F_Tree tree, Path path) {
            weight = tree.weight;
            cost_sd = tree.cost_sd;
            mask = tree.mask;
            nodes.addAll(tree.nodes);
            edges.addAll(tree.edges);
            for (int i = 0; i < path.nodes.length; i++) {
                boolean f = true;
                for (Integer j : nodes) {
                    if (path.nodes[i] == j) {
                        f = false;
                        break;
                    }
                }
                if (f) {
                    mask |= node_mask[path.nodes[i]];
                    weight += G.a[path.nodes[i]];
                    for (Integer j : nodes) {
                        cost_sd += G.sd.cal(path.nodes[i], j);
                    }
                    nodes.add(path.nodes[i]);
                    edges.add(new Edge(path.nodes[i], path.nodes[i + 1]));
                }
            }
            double ans_wt = wt[mask] + weight;
            int num = sd[mask];
            double ans_sd = (1 + num / (1D + ((nodes.size() - 1) >> 1) * 2)) * cost_sd;
            cost = alpha * ans_wt + (1 - alpha) * ans_sd;
        }

        Tree toTree() {
            Tree ret = new Tree();
            ret.n = nodes.size();
            ret.edges = new LinkedList<>();
            int pre = -1;
            for (Integer i : nodes) {
                if (pre >= 0) {
                    ret.edges.add(new Edge(pre, i));
                }
                ret.map.put(i, ret.map.size());
                pre = i;
            }
            return ret;
        }
    }

    class Path {
        double cost, cost_sd;
        int[] nodes;

        Path(Path p, int k) {
            cost_sd = p.cost_sd;
            double weight = G.a[k];
            int mask = node_mask[k];
            nodes = new int[p.nodes.length + 1];
            System.arraycopy(p.nodes, 0, nodes, 0, p.nodes.length);

            for (int i : p.nodes) {
                mask |= node_mask[i];
                weight += G.a[i];
                cost_sd += G.sd.cal(i, k);
                if (i == k) {
                    cost = inf;
                    return;
                }
            }
            nodes[nodes.length - 1] = k;

            double ans_wt = wt[mask] + weight;
            int num = sd[mask];
            double ans_sd = (1 + num / (1D + ((nodes.length - 1) >> 1) * 2)) * cost_sd;

            cost = alpha * ans_wt + (1 - alpha) * ans_sd;
        }

        Path(int k) {
            cost_sd = 0;
            double weight = G.a[k];
            int mask = node_mask[k];
            nodes = new int[]{k};
            double ans_wt = wt[mask] + weight;
            cost = alpha * ans_wt;
        }
        /*
        Structure.Path toStructurePath() {
            Structure.Path path = new Structure.Path();
            path.cost = cost;
            path.nodes = nodes;
            return path;
        }
         */



        /*
        void cal() {
            mask = 0;
            for (Integer node : nodes) {
                mask |= node_mask[node];
            }

            double ans_wt = wt[mask];
            int num = B3F.this.cost_sd[mask];

            cost_sd = 0;
            int m = nodes.size();
            for (int i = 0; i < m; i++) {
                for (int j = i + 1; j < m; j++) {
                    cost_sd += G.cost_sd.cal(nodes.get(i), nodes.get(j));
                }
            }
            double ans_sd = (1 + num / (1D + ((nodes.size() - 1) >> 1) * 2)) * cost_sd;

            cost = alpha * ans_wt + (1 - alpha) * ans_sd;
            //System.out.println(1 + 1D * num / (1 + ((nodes.size() - 1) >> 1) * 2)+" "+alpha * ans_wt+" "+(1 - alpha) * ans_sd);

        }
         */

    }
}
