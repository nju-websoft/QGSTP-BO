package graphtheory.SemKSG;

import graphtheory.Query;
import graphtheory.SemKSG.SemKSG;
import graphtheory.Structure.*;
import mytools.Config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static graphtheory.Structure.alpha;
import static java.lang.Math.*;

/**
 * Implement BO, search sqrt{n}/e nodes randomly chosen
 * @Author qkoqhh
 * @Date 2020-12-11
 */
public class BO extends SemKSG {
    public BO(Graph G, Query Q) {
        super(G, Q);
    }

    @Override
    public Tree solve() {
        List<Integer> enumeration = new ArrayList<>();
        for (int i = 0; i < G.n; i++) {
            enumeration.add(i);
        }
        get_rank();
        if (Config.IRR) {
            enumeration.sort(comparator);
        }

        int kmin = 0;
        for (int i = 1; i < Q.g; i++) {
            if (Q.keywords.get(i).size() < Q.keywords.get(kmin).size()) {
                kmin = i;
            }
        }
        if (Config.IRR) {
            Q.keywords.get(kmin).sort(comparator);
        }

        List<Integer> rand_enumeration = new ArrayList<>();
        for (int i = 1; i <= sqrt(G.n) / Math.E; i++) {
            rand_enumeration.add(rand.nextInt(G.n));
        }
        if (Config.IRR) {
            rand_enumeration.sort(comparator);
        }

        if (Config.IMH) {
            if (Config.IRR) {
                min_hop_RPS(enumeration.subList(0, 10));
            }
            min_hop_RPS(Q.keywords.get(kmin));
            min_hop_RPS(rand_enumeration);
        }

        if (Config.IRR) {
            for (int i = 0; i < 10; i++) {
                if (System.currentTimeMillis() - start > timeout) {
                    return ansTree;
                }
                FindRPS(enumeration.get(i));
            }
        }

        for (Integer r : Q.keywords.get(kmin)) {
            if (System.currentTimeMillis() - start > timeout) {
                return ansTree;
            }
            FindRPS(r);
        }


        for (Integer r : rand_enumeration) {
            if (System.currentTimeMillis() - start > timeout) {
                return ansTree;
            }
            FindRPS(r);
        }

        if (!Config.IRR) {
            for (int i = 0; i < 10; i++) {
                if (System.currentTimeMillis() - start > timeout) {
                    return ansTree;
                }
                FindRPS(enumeration.get(i));
            }

        }

        System.out.println("cost(RPS)=" + ansRPS.cost);
        return ansTree;
    }
}
