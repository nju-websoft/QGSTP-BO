package graphtheory.SemKSG;

import graphtheory.Query;
import graphtheory.Structure;
import mytools.Config;
import mytools.Debug;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static graphtheory.Structure.alpha;

/**
 * Implement EO, search the minimize size key node set
 * @Author qkoqhh
 * @Date 2020-10-29
 */
public class EO extends SemKSG {
    public EO(Structure.Graph G, Query Q) {
        super(G, Q);
    }

    @Override
    public Structure.Tree solve() {
        List<Integer> enumeration = new ArrayList<>();
        for (int i = 0; i < G.n; i++) {
            enumeration.add(i);
        }
        get_rank();
        if(Config.IRR) {
            enumeration.sort(comparator);
        }


        int kmin=0;
        for(int i=1;i<Q.g;i++) {
            if (Q.keywords.get(i).size() < Q.keywords.get(kmin).size()) {
                kmin = i;
            }
        }

        if(Config.IRR) {
            Q.keywords.get(kmin).sort(comparator);
        }

        if(Config.IMH){
            min_hop_RPS(enumeration.subList(0,10));
            min_hop_RPS(Q.keywords.get(kmin));
        }

        if(Config.IRR) {
            for (int i = 0; i < 10; i++) {
                if (System.currentTimeMillis() - start > timeout) {
                    return ansTree;
                }
                FindRPS(enumeration.get(i));
            }

        }


        for(Integer r:Q.keywords.get(kmin)){
            if(System.currentTimeMillis()-start>timeout){
                return ansTree;
            }
            FindRPS(r);
        }

        if(!Config.IRR) {
            for (int i = 0; i < 10; i++) {
                if (System.currentTimeMillis() - start > timeout) {
                    return ansTree;
                }
                FindRPS(enumeration.get(i));
            }

        }

        System.out.println("cost(RPS)="+ansRPS.cost);
        return ansTree;
    }
}
