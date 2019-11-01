package infoasset.replication.special;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListTable15 {
   private String dataURL = "jdbc:mysql://206.1.1.15/information_schema";
   private String dataUser = null;
   private String dataPass = null;
   private String repoURL = "jdbc:mysql://206.1.1.137/Replication";
   private String repoUser = "infoasset";
   private String repoPass = "infoasset";
   private Connection connDat;
   private Connection connRep;
   private String[] DB_LIST = new String[] { "accountbranch", "acpay",
         "bbtest", "bbtlcus", "bracontest", "branch", "ca", "ca_log",
         "campaign", "cbranch", "ccn", "chain", "claim", "common", "complain",
         "contactpoint", "direct", "iservice", "kpisales", "license",
         "lovecyber2", "manisa", "master", "mca", "mca_tmp", "mis", "newcase",
         "operator", "paybank", "payroll", "prospect", "receipt", "rsale",
         "sales", "service", "straining", "tcommon", "test", "testclaim",
         "tlapp", "tlc", "tlcard", "tlcardimplement", "tlcustomer", "tlsales",
         "tmp", "tmpcontactpoint", "tpayroll", "underwrite", "wansodsai_id" };

   public ListTable15() throws ClassNotFoundException, SQLException {
      Class.forName("com.mysql.jdbc.Driver");
      connDat = DriverManager.getConnection(dataURL, dataUser, dataPass);
      connRep = DriverManager.getConnection(repoURL, repoUser, repoPass);
      String sqlIns = "INSERT IGNORE INTO SERVER_15 (databaseName, tableName,wildcard) VALUES (?, ?, ?)";
      PreparedStatement pstIns = connRep.prepareStatement(sqlIns);
      for (String database : DB_LIST) {
         connDat.createStatement().execute("USE " + database + ";");
         int count = 0;
         ResultSet rs = connDat.createStatement().executeQuery("SHOW TABLES");
         while (rs.next()) {
            count++;
            String tableName = rs.getString(1);
            String wildcard = "";
            if (tableName.matches(".*\\d{8}.*")) {
               tableName = tableName.replaceAll("\\d{8}", "########");
               wildcard = "yyyymmdd";
            } else if (tableName.matches(".*\\d{6}.*")) {
               tableName = tableName.replaceAll("\\d{6}", "######");
               wildcard = "yyyymm";
            } else if (tableName.matches(".*\\d{4}.*")) {
               tableName = tableName.replaceAll("\\d{4}", "####");
               if (tableName.indexOf("25") >= 0) {
                  wildcard = "yyyyyy";
               } else {
                  wildcard = "yyyymm";
               }
            }
            pstIns.setString(1, database);
            pstIns.setString(2, tableName);
            pstIns.setString(3, wildcard);
            pstIns.executeUpdate();
         }
         System.out.printf("%-30s   : %d\n", database,count);
      }
   }

   public static void main(String[] args) {
      try {
         new ListTable15();
      } catch (ClassNotFoundException | SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }
}
