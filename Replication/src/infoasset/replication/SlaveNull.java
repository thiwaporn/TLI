package infoasset.replication;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class SlaveNull implements Slave {
   private String name;
   public SlaveNull() {
      
   }

   @Override
   public void config(SlaveProperty prop) throws IOException, SQLException,
         ClassNotFoundException {
      name = prop.getSlaveName();
      
   }

   @Override
   public boolean executeSQL(String targetFileName,ArrayList<String> sqlList) {
      return true;
   }

   @Override
   public String getSlaveName() {
      return name;
   }

   @Override
   public String getSlaveType() {
    return "NULL";
   }

   @Override
   public String getDetail() {
      return null;
   }


  

   @Override
   public void insertTimeLoad(String sourceTable, String sourceDatabase, String fullTargetTable, Boolean success, Timestamp startTime, Timestamp endTime)
         throws SQLException {
      Log.getInstance().debug("Full target " + sourceTable + " " + sourceDatabase + " " + fullTargetTable);
      
   }

}
