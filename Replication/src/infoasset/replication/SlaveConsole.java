package infoasset.replication;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

class SlaveConsole implements Slave {
	private String name;
	
	@Override
	public	boolean executeSQL(String targetFileName,ArrayList<String> sqlList) {
		for (String sql : sqlList) {
			System.out.println(sql);
		}
		return true;
		
	}

	@Override
	public void config(SlaveProperty prop) {
		name = prop.getSlaveName();
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSlaveName() {
		return name;
	}

	@Override
	public String getSlaveType() {
		return "CONSOLE";
	}

	@Override
	public String getDetail() {		
		return null;
	}

  
   @Override
   public void insertTimeLoad(String sourceTable, String sourceDatabase,String fullTargetTable, Boolean success, Timestamp startTime, Timestamp endTime)
         throws SQLException {
      Log.getInstance().debug("Full target " + sourceTable + " " + sourceDatabase + " " + fullTargetTable);
      
   }




}
