package graphtheory.SemKSG;


import java.util.*;


import static graphtheory.Structure.alpha;
import static java.lang.Math.*;

import graphtheory.Algorithm;
import graphtheory.Query;
import graphtheory.Structure.*;
import mytools.Config;
import mytools.Debug;
import org.jgrapht.alg.util.Pair;


/**
 * @Author qkoqhh
 * @Date 2020-10-12
 */

public abstract class SemKSG extends Algorithm {

    public SemKSG(Graph G, Query Q) {
        super(G, Q);
        init(G.n, Q.g);
        init_PLB();
        long end = System.currentTimeMillis();
        System.out.println("PLB init time: " + (end - start) + " ms");
    }

    Random rand = new Random(2333);

    double[][] hub_sal, hub_sd;

    /**
     * Cluster the hub for each keyword, and store it on the array
     */
    void init_PLB() {
        hub_sal = new double[G.n][Q.g];
        hub_sd = new double[G.n][Q.g];
        for (int i = 0; i < G.n; i++) {
            Arrays.fill(hub_sal[i], inf);
            Arrays.fill(hub_sd[i], inf);
        }
        for (int i = 0; i < Q.g; i++) {
            for (Integer j : Q.keywords.get(i)) {
                for (int t = G.hub_sal_consor[j], num = 0; num < G.hub_sal_size[j]; num++, t++) {
                    Hop k = new Hop(G.hub_node_pool[t], G.hub_value_pool[t]);
                    hub_sal[k.t][i] = min(hub_sal[k.t][i], k.w - G.a[k.t]);
                }
                if ("Jaccard".equals(Config.SD)) {
                    for (int t = G.hub_sd_consor[G.f[j]], num = 0; num < G.hub_sd_size[G.f[j]]; num++, t++) {
                        Hop k = new Hop(G.hub_node_pool[t], G.hub_value_pool[t]);
                        hub_sd[k.t][i] = min(hub_sd[k.t][i], k.w);
                    }
                } else {
                    for (int t = G.hub_sd_consor[j], num = 0; num < G.hub_sd_size[j]; num++, t++) {
                        Hop k = new Hop(G.hub_node_pool[t], G.hub_value_pool[t]);
                        hub_sd[k.t][i] = min(hub_sd[k.t][i], k.w);
                    }
                }
            }
        }
    }

    double lower_sal, lower_sd;
    double[] lower_bound_sal, lower_bound_sd;
    double[] lower_path;

    double query_sal(int t) {
        lower_bound_sal = new double[Q.g];
        Arrays.fill(lower_bound_sal, inf);
        for (int k = G.hub_sal_consor[t], num = 0; num < G.hub_sal_size[t]; num++, k++) {
            Hop j = new Hop(G.hub_node_pool[k], G.hub_value_pool[k]);
            for (int i = 0; i < Q.g; i++) {
                lower_bound_sal[i] = min(lower_bound_sal[i], j.w + hub_sal[j.t][i] - G.a[t]);
            }
        }
        return Arrays.stream(lower_bound_sal).sum() + G.a[t];
    }

    double query_sd(int t) {
        lower_bound_sd = new double[Q.g];
        Arrays.fill(lower_bound_sd, inf);
        if ("Jaccard".equals(Config.SD)) {
            for (int k = G.hub_sd_consor[G.f[t]], num = 0; num < G.hub_sd_size[G.f[t]]; num++, k++) {
                Hop j = new Hop(G.hub_node_pool[k], G.hub_value_pool[k]);
                for (int i = 0; i < Q.g; i++) {
                    lower_bound_sd[i] = min(lower_bound_sd[i], j.w + hub_sd[j.t][i]);
                }
            }
        } else {
            for (int k = G.hub_sd_consor[t], num = 0; num < G.hub_sd_size[t]; num++, k++) {
                Hop j = new Hop(G.hub_node_pool[k], G.hub_value_pool[k]);
                for (int i = 0; i < Q.g; i++) {
                    lower_bound_sd[i] = min(lower_bound_sd[i], j.w + hub_sd[j.t][i]);
                }
            }
        }
        return Arrays.stream(lower_bound_sd).sum();
    }


    void init(int n, int m) {
        final int bound = 100;

        a = new double[n];

        d = new double[bound][n];
        for (int i = 0; i < bound; i++) {
            Arrays.fill(d[i], inf);
        }
        pd = new int[bound][n];


        pack = new double[m + 1][m * bound];
        pre = new int[m + 1][m * bound];

        g = new double[m][bound];
        for (int i = 0; i < m; i++) {
            Arrays.fill(g[i], inf);
        }
        pg = new int[m][bound];

        len = new int[m];
        vis = new boolean[n];

        dis = new double[n];
        Arrays.fill(dis, inf);
        v = new boolean[n];
        length = new int[n];
        cal = new boolean[n];
        sd = new double[n];


        ansRPS.cost = inf;
        ansTree.cost = inf;

        lower_path = new double[Q.g];

        past_dis = new double[Q.g];

        if (Config.IMH) {
            pre_hop = new List[Q.g][G.n];
            for (int i = 0; i < Q.g; i++) {
                for (int j = 0; j < G.n; j++) {
                    pre_hop[i][j] = new LinkedList<>();
                }
            }
        }
        upper_d = new double[G.n];
        Arrays.fill(upper_d, inf);
        key_hop = new int[Q.g][G.n];
    }


    boolean[] cal;
    double[] sd;
    List<Integer> vis_cal;

    void cal_clear() {
        for (Integer i : vis_cal) {
            cal[i] = false;
        }
        vis_cal.clear();
    }


    double[] past_dis;

    boolean[] vis;
    int[] len;
    double[] dis;
    int[] length;
    boolean[] v;
    List<Integer> visitor;

    boolean upper_dij(int r, int n) {
        int cnt = 0;
        Arrays.fill(len, -1);
        PriorityQueue<Pair<Integer, Double>> q = new PriorityQueue<>(Comparator.comparing((Pair::getSecond)));
        a[r] = alpha * G.a[r];
        dis[r] = 0;
        length[r] = 0;
        for (int i = 0; i < Q.g; i++) {
            lower_path[i] = alpha * lower_bound_sal[i] + (1 - alpha) * n / 4 * lower_bound_sd[i];
        }
        double s = a[r];
        double bound = inf;
        bound = ansRPS.cost - s - Arrays.stream(lower_path).sum() + Arrays.stream(lower_path).max().getAsDouble();
        q.add(new Pair<>(r, dis[r]));
        vis[r] = true;
        visitor.add(r);
        while (!q.isEmpty()) {
            int t = q.poll().getFirst();
            if (v[t]) {
                continue;
            }
            if (dis[t] >= bound) {
                break;
            }
            v[t] = true;
            for (Integer k : G.key.get(t)) {
                if (len[k] == -1) {
                    len[k] = length[t];
                    s += dis[t];
                    past_dis[k] = dis[t];
                    cnt++;
                    lower_path[k] = 0;
                    bound = ansRPS.cost - s - Arrays.stream(lower_path).sum() + Arrays.stream(lower_path).max().getAsDouble();
                    if (ansRPS.cost <= s) {
                        return true;
                    }
                    if (cnt == Q.g) {
                        return false;
                    }
                }
            }

            for (Hop j : G.edges.get(t)) {
                if (!vis[j.t]) {
                    if (!cal[j.t]) {
                        cal[j.t] = true;
                        sd[j.t] = G.sd.cal(r, j.t);
                        vis_cal.add(j.t);
                    }
                    a[j.t] = alpha * G.a[j.t] + (1 - alpha) * n / 2 * sd[j.t];
                    vis[j.t] = true;
                    visitor.add(j.t);
                }
                if (!v[j.t] && dis[j.t] > dis[t] + a[j.t]) {
                    dis[j.t] = dis[t] + a[j.t];
                    length[j.t] = length[t] + 1;
                    if (dis[j.t] >= bound) {
                        continue;
                    }
                    q.add(new Pair<>(j.t, dis[j.t]));
                }
            }
        }
        return true;
    }


    double[][] d, pack, g;
    double[] upper_d;
    int[][] pd, pre, pg;
    double[] a;
    Set<Integer>[] q = new Set[]{new HashSet<>(), new HashSet<>()};

    void stratified_shortest_path(int r, int n, int max_hop) {
        int _t = 0;
        double upper_bound = ansRPS.cost - a[r] - Arrays.stream(past_dis).sum() + Arrays.stream(past_dis).max().getAsDouble();
        int max_length = min(Arrays.stream(len).max().getAsInt(), max_hop);
        q[_t].add(r);
        d[0][r] = 0;
        for (int i = 0; i < max_length & !q[_t].isEmpty(); i++) {
            for (Integer j : q[_t]) {
                for (Hop k : G.edges.get(j)) {
                    if (!vis[k.t]) {
                        vis[k.t] = true;
                        visitor.add(k.t);
                        if (!cal[k.t]) {
                            cal[k.t] = true;
                            sd[k.t] = G.sd.cal(r, k.t);
                            vis_cal.add(k.t);
                        }
                        a[k.t] = alpha * G.a[k.t] + (1 - alpha) * n / 2 * sd[k.t];
                    }
                    if (upper_d[k.t] <= d[i][j] + a[k.t]) {
                        continue;
                    }
                    d[i + 1][k.t] = d[i][j] + a[k.t];
                    upper_d[k.t] = d[i + 1][k.t];
                    if (d[i + 1][k.t] > upper_bound) {
                        continue;
                    }
                    pd[i + 1][k.t] = j;
                    q[_t ^ 1].add(k.t);
                }
                for (Integer k : G.key.get(j)) {
                    if (i <= len[k] && d[i][j] < g[k][i]) {
                        g[k][i] = d[i][j];
                        pg[k][i] = j;
                    }
                }
                d[i][j] = inf;
            }
            q[_t].clear();
            _t ^= 1;
        }

        for (Integer j : q[_t]) {
            for (Integer k : G.key.get(j)) {
                if (max_length <= len[k] && d[max_length][j] < g[k][max_length]) {
                    g[k][max_length] = d[max_length][j];
                    pg[k][max_length] = j;
                }
            }
            d[max_length][j] = inf;
        }
        q[_t].clear();
    }

    void FindRPS(int r) {
        lower_sal = query_sal(r);
        lower_sd = query_sd(r);
        vis_cal = new LinkedList<>();

        for (int n = !Config.IMH ? 1 : (R[r] + 1 + (Config.IMH ? 1 : 0)), max_hop = Rm[r] + (Config.IMH ? 1 : 0), end = Q.g * (G.n - 1); n <= end; n++, max_hop++) {
            if (System.currentTimeMillis() - start > timeout) {
                break;
            }

            if (Config.PLB && alpha * lower_sal + (1 - alpha) * n / 4 * lower_sd >= ansRPS.cost) {
                break;
            }


            Debug.print(r + " " + n);
            Debug.print(ansRPS.cost / (alpha * lower_sal + (1 - alpha) * n / 4 * lower_sd) + "");


            visitor = new LinkedList<>();

            Debug.start();
            if (upper_dij(r, n) || Arrays.stream(len).sum() + 1 < n) {
                for (Integer i : visitor) {
                    dis[i] = inf;
                    v[i] = false;
                    vis[i] = false;
                }
                Debug.end("Dij Time");
                Debug.print("Dij size: " + visitor.size());
                Debug.print("Dij Cut");
                if (Config.PLB) {
                    break;
                }
            }
            end = Arrays.stream(len).sum() + 1;
            Debug.end("Dij Time");
            Debug.print("Dij size: " + visitor.size());
            Debug.print(Arrays.toString(len));


            Debug.print("Execute");
            Debug.start();

            stratified_shortest_path(r, n, max_hop);


            for (int i = 0; i <= Q.g; i++) {
                for (int j = 0; j < n; j++) {
                    pack[i][j] = inf;
                }
            }
            pack[0][0] = a[r];
            for (int i = 0; i < Q.g; i++) {
                for (int k = 0; k <= len[i]; k++) {
                    if (g[i][k] < inf) {
                        for (int j = k; j < n; j++) {
                            if (pack[i][j - k] + g[i][k] < pack[i + 1][j]) {
                                pack[i + 1][j] = pack[i][j - k] + g[i][k];
                                pre[i + 1][j] = k;
                            }
                        }
                        g[i][k] = inf;
                    }
                }
            }

            Debug.end("Execute Time");
            Debug.print(ansRPS.cost + " " + pack[Q.g][n - 1]);

            if (ansRPS.cost > pack[Q.g][n - 1]) {
                RPS rps = new RPS();
                Debug.print("RPS Used!");
                rps.cost = pack[Q.g][n - 1];
                for (int i = Q.g, j = n - 1; i > 0; j -= pre[i][j], i--) {
                    Path path = new Path();
                    path.cost = g[i - 1][pre[i][j]];
                    for (int k = pre[i][j], x = pg[i - 1][k]; k >= 0; x = pd[k][x], k--) {
                        path.nodes.add(x);
                    }
                    rps.paths.add(path);
                }
                ansRPS = rps;

                Tree tree = new Tree(G, rps);
                tree.cal_cost(G);
                if (tree.cost < ansTree.cost) {
                    Debug.print("Tree Used!");
                    ansTree = tree;
                }
            }
            for (Integer i : visitor) {
                upper_d[i] = inf;
                vis[i] = false;
                dis[i] = inf;
                v[i] = false;
            }
            Debug.print("");
        }
        cal_clear();
    }

    public RPS ansRPS = new RPS();
    Tree ansTree = new Tree();

    @Override
    public abstract Tree solve();


    // IMH
    List<Integer>[][] pre_hop;

    Path get_path(int r, List<Integer>[] pre_hop, int[] pre, double[] d, int n) {
        int m = 0;
        List<List<Integer>> layer = new ArrayList<>();
        layer.add(new LinkedList<>());
        layer.get(m).add(r);
        d[r] = 0;
        pre[r] = -1;
        while (pre_hop[layer.get(m).get(0)].size() > 0) {
            layer.add(new LinkedList<>());
            for (Integer i : layer.get(m)) {
                for (Integer j : pre_hop[i]) {
                    if (Double.compare(inf, d[j]) == 0) {
                        layer.get(m + 1).add(j);
                    }
                    double new_dis = d[i] + alpha * G.a[j] + (1 - alpha) * n / 2 * G.sd.cal(r, j);
                    if (d[j] > new_dis) {
                        d[j] = new_dis;
                        pre[j] = i;
                    }
                }
            }
            m++;
        }
        int t = layer.get(m).get(0);
        for (Integer i : layer.get(m)) {
            if (d[i] < d[t]) {
                t = i;
            }
        }
        Path path = new Path();
        path.cost = d[t];
        for (int x = t; x >= 0; x = pre[x]) {
            path.nodes.add(x);
        }
        for (List<Integer> nodes : layer) {
            for (Integer j : nodes) {
                d[j] = inf;
            }
        }
        return path;
    }

    void min_hop_RPS(List<Integer> enumeration) {
        Debug.istart();
        System.out.println("Precompute min hop RPS...");
        int[] pre = new int[G.n];
        double[] d = new double[G.n];
        Arrays.fill(d, inf);
        for (Integer r : enumeration) {
            if (Config.PLB) {
                if (alpha * query_sal(r) + (1 - alpha) * (R[r] + 1) / 4 * query_sd(r) >= ansRPS.cost) {
                    continue;
                }
            } else if (R[r] >= Integer.MAX_VALUE >> 1) {
                continue;
            }
            //System.out.println("min hop: "+r+" "+R[r]);
            RPS rps = new RPS();
            rps.cost = alpha * G.a[r];
            for (int i = 0; i < Q.g; i++) {
                Path path = get_path(r, pre_hop[i], pre, d, R[r] + 1);
                rps.paths.add(path);
                rps.cost += path.cost;
            }
            if (rps.cost < ansRPS.cost) {
                //System.out.println("RPS Used");
                ansRPS = rps;
                Tree tree = new Tree(G, rps);
                tree.cal_cost(G);
                if (tree.cost < ansTree.cost) {
                    ansTree = tree;
                }
            }
        }
        Debug.iend("Find Min Hop RPS Time");
    }

    int[] R, Rm;
    int[][] key_hop;
    Comparator<Integer> comparator = null;

    void get_rank() {
        Debug.istart();
        R = new int[G.n];
        Rm = new int[G.n];
        int cnt = 0;
        for (List<Integer> query : Q.keywords) {
            Queue<Integer> q = new LinkedList<>();
            for (Integer i : query) {
                key_hop[cnt][i] = 1;
                q.add(i);
            }
            while (!q.isEmpty()) {
                int t = q.poll();
                for (Hop j : G.edges.get(t)) {
                    if (key_hop[cnt][j.t] == 0) {
                        key_hop[cnt][j.t] = key_hop[cnt][t] + 1;
                        q.add(j.t);
                    }
                    if (Config.IMH && key_hop[cnt][j.t] == key_hop[cnt][t] + 1) {
                        pre_hop[cnt][j.t].add(t);
                    }
                }
            }
            for (int i = 0; i < G.n; i++) {
                if (key_hop[cnt][i] > 0) {
                    R[i] += key_hop[cnt][i] - 1;
                    Rm[i] = max(Rm[i], key_hop[cnt][i] - 1);
                } else {
                    R[i] = Integer.MAX_VALUE >> 1;
                }
            }
            cnt++;
        }
        comparator = Comparator.comparing(o -> R[o]);
        Debug.iend("Get Rank Time");
    }
}

