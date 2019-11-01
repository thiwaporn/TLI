package infoasset.replication;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;

public class ReplicationLog {
	private static ReplicationLog INSTANCE;

	public static ReplicationLog getInstance() throws IOException, SQLException {
		if (INSTANCE == null) {
			INSTANCE = new ReplicationLog();
		}
		return INSTANCE;
	}

	private Connection connection;
	private Integer logId;
	private PreparedStatement insertPst;
	private PreparedStatement updatePst;
	private PreparedStatement slavePst;
	private PreparedStatement invalidPst;
	private PreparedStatement increaseCall;
	private PreparedStatement startServer;
	private PreparedStatement shutdownServer;
	private PreparedStatement transResult;
	private String journalFile;
	private boolean haveFail = false;

	private ReplicationLog() throws IOException, SQLException {

		Properties prop = new Properties();
		FileInputStream inStream = new FileInputStream(
				System.getProperty("connection_file"));
		prop.load(inStream);
		String driver = prop.getProperty("DB_DRIVER");
		String url = prop.getProperty("DB_URL");
		String user = prop.getProperty("DB_USER");
		String password = prop.getProperty("DB_PASSWORD");

		BasicDataSource bsd = new BasicDataSource();
		bsd.setUrl(url);
		bsd.setDriverClassName(driver);
		bsd.setUsername(user);
		bsd.setPassword(password);
		connection = bsd.getConnection();
		connection.setAutoCommit(false);
		String insertSQL = "INSERT INTO `Replication`.`ReplicationLog` (`journalFile`) VALUES (?)";
		String updateSQL = "UPDATE `Replication`.`ReplicationLog` SET `endTime` = NOW(), `result`=? WHERE `logId` = ?";
		String insertSlave = "INSERT INTO `Replication`.`ReplicationLogSlave` (`logId`, `name`,`type`,`detail`) VALUES (?, ?, ?, ?)";
		String invalidSQL = "REPLACE INTO `Replication`.`InvalidCharacter` (`tableName`,`lastFoundJournal`, `transactionTime`, `transactionType`, `solveDate`) VALUES (?, ?, ?, ?, NULL)";
		String increaseTrans = "CALL `Replication`.`recordTransaction`(?, ?, ?, ?, ?, ?)";
		String startSQL = "UPDATE `Replication`.`ReplicationLog` SET `dataStartTime` = ? WHERE `logId` =?";
		String shutdownQL = "UPDATE `Replication`.`ReplicationLog` SET `dataEndTime` = ? WHERE `logId` =?";
		String transSQL = "UPDATE `Replication`.`ReplicationTrans` SET `result` = ? WHERE `transactionId`=?";
		insertPst = connection.prepareStatement(insertSQL,
				Statement.RETURN_GENERATED_KEYS);
		updatePst = connection.prepareStatement(updateSQL);
		slavePst = connection.prepareStatement(insertSlave);
		invalidPst = connection.prepareStatement(invalidSQL);
		increaseCall = connection.prepareStatement(increaseTrans);		
		startServer = connection.prepareStatement(startSQL);
		shutdownServer = connection.prepareStatement(shutdownQL);
		transResult = connection.prepareStatement(transSQL);
	}
	public boolean checkPreviousComplete(String jrnFileName) throws SQLException {	   
	   StringBuilder str = new StringBuilder();
	   str = str.append("SELECT COUNT(*) FROM `Replication`.`ReplicationLog`")
	         .append(" WHERE `journalFile` LIKE ?")
	         .append(" AND `result` ='FAIL'");
	  PreparedStatement pst = connection.prepareStatement(str.toString());
	  pst.setString(1, jrnFileName + "%");
	   ResultSet rs = pst.executeQuery();
	   if (rs.next()) {
	      if (rs.getInt(1) == 0) {
	         return true;
	      } else {
	         return false;
	      }
	   } else {
	      return false;
	   }
	}
	public void startReplication(String journalFile) throws SQLException {
		this.journalFile = journalFile;
		insertPst.setString(1, journalFile);
		insertPst.execute();
		ResultSet rs = insertPst.getGeneratedKeys();
		if (rs.next()) {
			logId = rs.getInt(1);
		}
		Log.getInstance().trace("LogId= " + logId);
		connection.commit();

	}

	public void setFail(long transId) throws SQLException {
	   transResult.setBoolean(1, Boolean.FALSE);
	   transResult.setLong(2, transId);
	   transResult.executeUpdate();
	   connection.commit();
		haveFail = true;
	}

	public void endReplication() throws SQLException {
		updatePst.setString(1, haveFail ? "FAIL" : "DONE");
		updatePst.setInt(2, logId);
		updatePst.execute();
		connection.commit();
		connection.close();

	}

	public void insertSlave(Slave slave) throws SQLException {

		slavePst.setInt(1, logId);
		slavePst.setString(2, slave.getSlaveName());
		slavePst.setString(3, slave.getSlaveType());
		slavePst.setString(4, slave.getDetail());
		slavePst.execute();
		connection.commit();

	}

	public void insertInvalidCharacter(String tableName, long timeValue,
			TransactionType type) throws SQLException {
		Time time = new Time(timeValue);
		invalidPst.setString(1, tableName);
		invalidPst.setString(2, journalFile);
		invalidPst.setTime(3, time);
		invalidPst.setString(4, type.name());
		invalidPst.execute();
		connection.commit();
	}

	public long recordTransaction(Transaction trans, boolean success) 
			throws SQLException {		
	   if (!success) {
	      haveFail = true; 
	   }
			// parameter : logId, fnum, transactionTime, transactionTime, transactionType, tableName
	   Timestamp ts = new Timestamp(trans.getTime());
	   if (trans.getType().equals(TransactionType.START)) {
	      startServer.setTimestamp(1, ts);
	      startServer.setLong(2,logId);
	      startServer.executeUpdate();
	      connection.commit();
	      return 0;
	   } else if (trans.getType().equals(TransactionType.SHUTDOWN)) {
	      shutdownServer.setTimestamp(1, ts);
          shutdownServer.setLong(2,logId);
          shutdownServer.executeUpdate();
          connection.commit();
          return 0;
	   } else {
		increaseCall.setInt(1, logId);
		increaseCall.setInt(2,  trans.getFileId());		
		increaseCall.setTimestamp(3, ts);
		increaseCall.setString(4, trans.getType().name());
		
		String tableName = trans.getTableName();
		if (tableName == null) {
			increaseCall.setNull(5, Types.VARCHAR);
		} else {
			increaseCall.setString(5, tableName);
		}
		increaseCall.setString(6, success? "Y" : "X");
		increaseCall.execute();
		ResultSet rs = increaseCall.getResultSet();
		if (rs.next()) {
		   return rs.getLong(1);
		} else {
		   return 0;
		}
	   }
	}

   public void setSuccess(long transId) throws SQLException {
 transResult.setBoolean(1, Boolean.TRUE);
 transResult.setLong(2, transId);
 transResult.executeUpdate();
 connection.commit();
      
   }
}
