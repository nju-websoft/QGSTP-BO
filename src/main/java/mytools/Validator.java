package mytools;

import graphtheory.Query;
import graphtheory.Structure.*;
import graphtheory.Structure.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Validator {
    public static int find(int[]f,int x){
        if(f[x]==x){
            return x;
        }
        f[x]=find(f,f[x]);
        return f[x];
    }
    public static void comp(Graph G){
        int[]f=new int[G.n];
        for(int i=0;i<G.n;i++){
            f[i]=i;
        }
        for(int i=0;i<G.n;i++) {
            for (Hop j : G.edges.get(i)) {
                int x = find(f, i), y = find(f, j.t);
                if (x != y) {
                    f[y] = x;
                }
            }
        }
        int[]size=new int[G.n];
        int s=0;
        for(int i=0;i<G.n;i++){
            if(find(f,i)==i){
                s++;
            }
            size[find(f,i)]++;
        }
        System.out.println(s);
        for(int i=0;i<G.n;i++){
            if(find(f,i)==i){
                System.out.print(size[i]+" ");
            }
        }
        System.out.println("");
    }
}
