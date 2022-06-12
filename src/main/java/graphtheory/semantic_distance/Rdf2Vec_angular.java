package graphtheory.semantic_distance;

import java.sql.*;

import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

/**
 * Map the node to vector and let the angle between the vector as sd
 * @Author qkoqhh
 * @Date 2020-10-29
 */
public class Rdf2Vec_angular extends SD {
    static final int vec_length=10;
    final double[][] nodevecs;
    public Rdf2Vec_angular(Connection conn,int n) throws SQLException {
        System.out.println("Reading nodevec..");
        PreparedStatement pstmt=conn.prepareStatement("select * from nodevec",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        pstmt.setFetchSize(100000);
        pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        ResultSet ret=pstmt.executeQuery();
        nodevecs=new double[n][vec_length];
        while(ret.next()){
            int i=ret.getInt("id");
            int j=ret.getInt("dimension");
            nodevecs[i][j]=ret.getDouble("value");
        }
        conn.commit();
        ret.close();
        pstmt.close();
    }
    @Deprecated
    public Rdf2Vec_angular(double[][] nodevecs){
        this.nodevecs=nodevecs;
    }
    double vec_point_mul(double[] a, double[] b){
        double s=0;
        for(int i=0;i<vec_length;i++){
            s+=a[i]*b[i];
        }
        return s;
    }
    double vec_mod(double[] a){
        double s=0;
        for(double i:a){
            s+=i*i;
        }
        return sqrt(s);
    }

    @Override
    public double cal(int i, int j){
        return acos(vec_point_mul(nodevecs[i],nodevecs[j])/vec_mod(nodevecs[i])/vec_mod(nodevecs[j]))/Math.PI;
    }
}
