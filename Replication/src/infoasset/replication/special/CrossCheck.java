package infoasset.replication.special;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CrossCheck {
   private Connection connDat;
   private Connection connRep;
   private String dataURL = "jdbc:mysql://206.1.1.15/information_schema";
   private String dataUser = null;
   private String dataPass = null;
   private String repoURL = "jdbc:mysql://206.1.1.137/Replication";
   private String repoUser = "infoasset";
   private String repoPass = "infoasset";
   private String[] databases = {"common", "direct","tlcustomer", "paybank", "master", "claim", "payroll", "service", "tlapp", "newcase", "sales", "license", "rsale", "complain", "receipt", "campaign"};
   public CrossCheck()  {
      
      
   }

   private void close() {
      if (connDat != null) {
         try {
            connDat.close();
         } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      if (connRep != null) {
         try {
            connRep.close();
         } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }
   private void start() throws ClassNotFoundException, SQLException {
      Class.forName("com.mysql.jdbc.Driver");
      connDat = DriverManager.getConnection(dataURL, dataUser, dataPass);
      connRep = DriverManager.getConnection(repoURL, repoUser, repoPass);
      String mapSQL = "SELECT * FROM Mapping WHERE slaveId = 1 AND targetDatabase = ? AND targetTable = ?";
      String updSQL = "UPDATE Mapping SET found15 = TRUE WHERE targetDatabase = ? AND targetTable = ?";
      String insSQL = "INSERT IGNORE INTO NO_MAPPING (targetDatabase, targetTable) VALUES ( ?,  ?)";
      PreparedStatement mapPst = connRep.prepareStatement(mapSQL);
      PreparedStatement updPst = connRep.prepareStatement(updSQL);
      PreparedStatement insPst = connRep.prepareStatement(insSQL);
      for (String database : databases) {
         if (!database.equals("direct")) {
            continue;
         }
         System.out.printf("DB %s\n", database);
         connDat.createStatement().executeQuery("USE " + database);
         ResultSet rsList =connDat.createStatement().executeQuery("SHOW TABLES;");
         while (rsList.next()) {
            String tbName = rsList.getString(1);
            if (tbName.matches(".*\\d+")) {
               continue;
            }
            tbName = tbName.replaceAll("\\d","#");
            
          //  System.out.printf("\tTable %s\n", tbName);
            mapPst.clearParameters();
            mapPst.setString(1, database);
            mapPst.setString(2, tbName);
            ResultSet rsMap = mapPst.executeQuery();
            if (rsMap.next()) {
               updPst.clearParameters();
               updPst.setString(1, database);
               updPst.setString(2, tbName);
         //      updPst.executeUpdate();
            } else {
               System.out.printf("Not found %-30s : %s\n", database, tbName);
               insPst.clearParameters();
               insPst.setString(1, database);
               insPst.setString(2, tbName);
         //      insPst.executeUpdate();
            }
            
         }
        
      }
   }
   public static void main(String[] args) throws ClassNotFoundException {
      CrossCheck cross = new CrossCheck();
      try {
     cross.start();    
      } catch (Exception e){
         e.printStackTrace();
      } finally {
         cross.close();
      }
   }
}
