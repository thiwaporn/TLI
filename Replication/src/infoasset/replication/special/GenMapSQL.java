package infoasset.replication.special;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;



public class GenMapSQL implements AutoCloseable {
   private String repoURL = "jdbc:mysql://206.1.1.137/Replication";
   private String repoUser = "infoasset";
   private String repoPass = "infoasset";
   private Connection connRep;
   private File sqlFile;
   public GenMapSQL() throws ClassNotFoundException, SQLException, IOException {
      Class.forName("com.mysql.jdbc.Driver");      
      connRep = DriverManager.getConnection(repoURL, repoUser, repoPass);
      sqlFile = new File ("/tmp/MAP_SCRIPT.sql"); 
      
      FileUtils.forceDelete(sqlFile);
   }
   @Override
   public void close() {
      try {
         connRep.close();
         
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
   }
   private void start() throws IOException, SQLException {
      ArrayList<String> sqlScript;
      PreparedStatement pst;
      String sqlFmt;
      
      pst = connRep.prepareStatement("SELECT sourceTable,targetTable FROM Mapping WHERE serverName = ? AND sourceDatabase = ? AND targetDatabase = ? ORDER BY sourceTable");
      sqlFmt = "CALL InsertMap (@SLAVE_NAME, @SERVER_NAME, @SOURCE_DB, %s, @TARGET_DB, '%s');";

      sqlScript = new ArrayList<>();
      sqlScript.add("USE Replication;");
      sqlScript.add(String.format("SET @%-15s = '%s';", "SLAVE_NAME", "LOCUS_DB"));
      ArrayList<String[]> dbList = getDBList();
      for (String[] db : dbList) {
         String server = db[0];
         String source = db[1];
         String target = db[2];
         sqlScript.add(String.format("-- %-15s : %-15s : %-15s", server,source, target));
         sqlScript.add(String.format("SET @%-15s = '%s';", "SERVER_NAME", server));
         sqlScript.add(String.format("SET @%-15s = '%s';", "SOURCE_DB", source));
         sqlScript.add(String.format("SET @%-15s = '%s';", "TARGET_DB", target));
         pst.clearParameters();
         pst.setString(1, server);
         pst.setString(2, source);
         pst.setString(3, target);
         ResultSet rs = pst.executeQuery();
         while (rs.next()) {
            String sourceTb = rs.getString(1);
            String targetTb = rs.getString(2);
            String fmtSourceTb = String.format("%-30s", "'" + sourceTb + "'");           
            sqlScript.add(String.format(sqlFmt, fmtSourceTb, targetTb));
         }
         
      }
      
      FileUtils.writeLines(sqlFile, sqlScript);
   }
   private ArrayList<String[]> getDBList() throws SQLException {
      String sql = "SELECT serverName,sourceDatabase,targetDatabase FROM Mapping GROUP BY serverName, sourceDatabase, targetDatabase ORDER BY serverName,sourceDatabase,targetDatabase";
      ArrayList<String[]> srvList = new ArrayList<>();
      ResultSet rs = connRep.createStatement().executeQuery(sql);
      while (rs.next()) {
         srvList.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3)});
      }
      return srvList;
      
   }
   public static void main(String[] args) {
      try (GenMapSQL gen = new GenMapSQL()) {
         gen.start();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
