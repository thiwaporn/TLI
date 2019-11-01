package infoasset.replication;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;

class SlaveDB implements Slave {

	private Connection connection;
	private String name;
	private String detail;
	private PreparedStatement pstTime = null;
	@Override
	public synchronized boolean executeSQL(String targetFileName, ArrayList<String> sqlList) {
	  
		try {			
			for (String sql : sqlList) {					   
				try (Statement stm = connection.createStatement()) {
				   
				   if (sql.startsWith("SELECT")) {
				      return true;
				      /*
				      ResultSet rs =  stm.executeQuery(sql);
				      while (rs.next()) {
				         count++;
				      }*/
				   } 
				   int count = stm.executeUpdate(sql);
				   
					if (StringUtils.startsWithIgnoreCase(sql, "DROP")
							|| StringUtils.startsWithIgnoreCase(sql, "CREATE")) {

					} else {
						if (count != 1) {
	                        Log.getInstance().error("count=%d  SQL = %s", count, sql);	                        
							return false;
						} else {
							//Log.getInstance().trace(sql);
						}
					}
				}
			}
		connection.commit();
			
		

		} catch (SQLException e) {
		//	e.printStackTrace();
			Log.getInstance().error("error|%s|%s",e.getClass().getName(), e.getMessage());
			return false;
		} finally {
		  
		}

		return true;
	}

	@Override
	public void config(SlaveProperty prop) throws ClassNotFoundException,
			SQLException {
		name = prop.getSlaveName();
		String driver = prop.getDriver();
		String url = prop.getUrl();
		String user = prop.getUserName();
		String password = prop.getPassword();
		detail = prop.getUrl();
		BasicDataSource bsd = new BasicDataSource();
		bsd.setUrl(url);
		bsd.setDriverClassName(driver);
		bsd.setUsername(user);
		bsd.setPassword(password);

		connection = bsd.getConnection();
		connection.setAutoCommit(false);
		
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}

	@Override
	public String getSlaveName() {
	return name;
	}

	@Override
	public String getSlaveType() {
return "RDBMS";
	}
	@Override
	public String getDetail() {		
		return detail;
	}


   @Override
   public void insertTimeLoad(String sourceTable, String sourceDatabase, String fullTargetTable, Boolean success, Timestamp startTime, Timestamp endTime) throws SQLException {
      try {
      if (fullTargetTable == null) {
         Log.getInstance().debug("Full target is null " + sourceTable + " " + sourceDatabase );
         return;
      }
      
     if (pstTime == null) {
        String sql = "INSERT INTO operator.TimeLoadMysql (filename, time, status, recordCount,startTime) VALUES (?, NOW(), ?, ?,?)";
        pstTime = connection.prepareStatement(sql);
     }
     String tableName = StringUtils.substringBefore(fullTargetTable, "@");
     String databaseName = StringUtils.substringAfter(fullTargetTable, "@");
     String countSQL = "SELECT COUNT(*) FROM " + databaseName + "." + tableName;
     int numberOfRecord = -1;
     ResultSet rs = connection.createStatement().executeQuery(countSQL);
     if (rs.next()) {
        numberOfRecord = rs.getInt(1);
     }
     pstTime.clearParameters();
     pstTime.setString(1, fullTargetTable);     
     pstTime.setBoolean(2,  !success); 
     pstTime.setInt(3, numberOfRecord);
    pstTime.setTimestamp(4, startTime);
     pstTime.executeUpdate();
      connection.commit();
      } catch (Exception e) {
         
      }
   }

	
}
