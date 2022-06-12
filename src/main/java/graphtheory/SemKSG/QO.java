package graphtheory.SemKSG;

import graphtheory.Query;
import graphtheory.SemKSG.SemKSG;
import graphtheory.Structure;
import mytools.Config;

import java.util.ArrayList;
import java.util.Comparator;

import static graphtheory.Structure.alpha;

/**
 * Implement QO, search all the node
 * @Author qkoqhh
 * @Date 2020-10-29
 */
public class QO extends SemKSG {

    public QO(Structure.Graph G, Query Q) {
        super(G, Q);
    }

    @Override
    public Structure.Tree solve() {
        ArrayList<Integer>enumeration=new ArrayList<>();
        for(int i=0;i<G.n;i++){
            enumeration.add(i);
        }
        get_rank();
        if(Config.IRR) {
            enumeration.sort(comparator);
        }

        if(Config.IMH){
            min_hop_RPS(enumeration);
        }
        for(int i = 0; i<G.n; i++){
            if(System.currentTimeMillis()-start>timeout){
                return ansTree;
            }
            FindRPS(enumeration.get(i));
        }
        System.out.println("cost(RPS)="+ansRPS.cost);
        return ansTree;
    }
}
