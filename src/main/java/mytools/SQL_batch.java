package mytools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Date 2020-12-22
 * @Author qkoqhh
 */
public class SQL_batch{
    static final int batch_size=100000;
    private int cnt;
    private PreparedStatement bat;
    private int m;
    private Connection conn;
    public SQL_batch(Connection conn,String sql)  {
        try {
            this.conn=conn;
            bat=conn.prepareStatement(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        for(Character i:sql.toCharArray()) {
            if (i == '?') {
                m++;
            }
        }
    }
    private void set(int x,Object y) throws Exception {
        Class<?> t = y.getClass();
        if(t==Integer.class){
            bat.setInt(x, (Integer) y);
        }else if(t==Double.class){
            bat.setDouble(x, (Double) y);
        }else if(t==String.class){
            bat.setString(x, (String) y);
        }else{
            throw new Exception("not implemented");
        }
    }
    private void commit() throws SQLException {
        bat.executeBatch();
        conn.commit();
        bat.clearBatch();
    }
    public void add(Object...args) throws Exception {
        for(int i=0;i<m;i++){
            set(i+1,args[i]);
        }
        bat.addBatch();
        cnt++;
        if(cnt%batch_size==0){
            commit();
        }
    }
    public void close() throws SQLException {
        commit();
        bat.close();
    }

}
