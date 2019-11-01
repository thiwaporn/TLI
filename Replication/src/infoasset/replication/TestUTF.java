package infoasset.replication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import manit.M;

public class TestUTF {

   public static void main(String[] args) {
      Connection conn = null;
      try {
         Class.forName("com.mysql.jdbc.Driver");
         conn = DriverManager.getConnection("jdbc:mysql://206.1.1.149/manisa?characterEncoding=utf8", "manisa",null);
         System.out.println("Connect OK");
         String select = "SELECT * FROM rider;";
         String insert = "INSERT INTO riderutf (policyNo,riderType,riderSum,riderPremium,riderStatus,riderStatusDate,effectiveDate,marker)";
         insert += " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
         PreparedStatement pst = conn.prepareStatement(insert);
         Statement stm = conn.createStatement();
         ResultSet rs = stm.executeQuery(select);
         while (rs.next()) {
            String riderType = rs.getString("riderType");
            System.out.println(M.stou(riderType));
            pst.clearParameters();
            pst.setString(1, rs.getString("policyNo"));
            pst.setString(2, M.stou(rs.getString("riderType")));
            pst.setDouble(3, rs.getDouble("riderSum"));
            pst.setDouble(4, rs.getDouble("riderPremium"));

            pst.setString(5, rs.getString("riderStatus"));
            pst.setString(6, rs.getString("riderStatusDate"));
            pst.setString(7, rs.getString("effectiveDate"));
            pst.setString(8, rs.getString("marker"));
            pst.executeUpdate();
            
            
         }
               /*
               -----------------+--------------+------+-----+---------+-------+
               | Field           | Type         | Null | Key | Default | Extra |
               +-----------------+--------------+------+-----+---------+-------+
               | policyNo        | char(8)      | NO   | PRI |         |       |
               | riderType       | char(3)      | NO   | PRI |         |       |
               | riderSum        | decimal(9,0) | YES  |     | NULL    |       |
               | riderPremium    | decimal(9,0) | YES  |     | NULL    |       |
               | riderStatus     | char(1)      | YES  |     | NULL    |       |
               | riderStatusDate | char(8)      | YES  |     | NULL    |       |
               | effectiveDate   | char(8)      | YES  |     | NULL    |       |
               | marker          | char(1)      | YES  |     | NULL    |       |
               +-----------------+--------------+------+-----+---------+-------+
*/
      } catch (ClassNotFoundException | SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } finally {
         if (conn != null) {
            try {
               conn.close();
            } catch (SQLException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }
    
   }

}
