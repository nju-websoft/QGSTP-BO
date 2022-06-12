package graphtheory.semantic_distance;

import graphtheory.Structure;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Jaccard extends SD{
    final List<List<Integer>>typelist;

    /**
     * @param typelist the type of each node. Notice that the list should have been sorted.
     */
    @Deprecated
    public Jaccard(List<List<Integer>>typelist){
        this.typelist=typelist;
    }
    public Jaccard(Connection conn,int n) throws SQLException {
        System.out.println("Reading type...");
        PreparedStatement pstmt=conn.prepareStatement("select * from nodetype",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        pstmt.setFetchSize(100000);
        pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        ResultSet ret=pstmt.executeQuery();
        typelist=new ArrayList<>();
        for(int i=0;i<n;i++){
            typelist.add(new ArrayList<>());
        }
        while(ret.next()){
            int node=ret.getInt("node");
            int type=ret.getInt("type");
            typelist.get(node).add(type);
        }
        conn.commit();
        ret.close();
        pstmt.close();

        for(int i=0;i<n;i++){
            Collections.sort(typelist.get(i));
        }
    }
    @Override
    public double cal(int i, int j) {
        int n=typelist.get(i).size();
        int m=typelist.get(j).size();
        if(n==0 && m==0){
            return 1D;
        }
        int k=0;
        for(int x=0,y=0;x<n&&y<m;){
            if(typelist.get(i).get(x).equals(typelist.get(j).get(y))){
                k++;
                x++;
                y++;
            }else if(typelist.get(i).get(x)<typelist.get(j).get(y)){
                x++;
            }else{
                y++;
            }
        }
        return 1-1D*k/(n+m-k);
    }
}
