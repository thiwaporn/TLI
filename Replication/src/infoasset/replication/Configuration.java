package infoasset.replication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;

public class Configuration {
   private static Configuration INSTANCE = null;

   public static Configuration getInstance() {
      return INSTANCE;
   }

   public static void createInstance(String configName, String journalFileName,
         Transaction trans) throws IOException, SQLException, RepException,
         ClassNotFoundException, InstantiationException, IllegalAccessException {
      INSTANCE = new Configuration(journalFileName);
      INSTANCE.configure(configName, trans);
   }

   private Integer configId;
   private Integer executeId;
   private String serverName;
   private String schemaPath;

   private String journalFileName;
   private Boolean logAll;
   private Boolean logData;
   private Boolean hasError = Boolean.FALSE;
   private Connection connection = null;
   private PreparedStatement pstTrans;
   private PreparedStatement pstExec;
   private ArrayList<SlaveController> slaveList;
   private ArrayList<Integer> slaveIdList;
   private Configuration(String journalFileName) throws IOException,
         SQLException {
      this.journalFileName = journalFileName;
      connection = getConnection();
   }

   private synchronized void configure(String configName, Transaction trans)
         throws SQLException, RepException, IOException,
         ClassNotFoundException, InstantiationException, IllegalAccessException {
	   Calendar.getInstance().getTime();
      slaveList = new ArrayList<>();
      slaveIdList = new ArrayList<>();
      String ipAddress = trans.getDataAsString();
      
      String findSrvSQL = "SELECT serverName FROM Configuration.Server WHERE ipAddress = ?";
      String findCfgSQL = "SELECT * FROM Replication.ConfigFile WHERE configName = ?";
      String findSchSQL = "SELECT * FROM Configuration.SchemaConfig WHERE serverName = ?";
      String findSlvSQL = "SELECT * FROM Replication.Slave WHERE configId = ? AND active = TRUE";
      String findMapSQL = "SELECT * FROM Replication.Mapping WHERE serverName = ? AND slaveId = ? AND active = TRUE";
      String insExSQL = "INSERT INTO Replication.Execution (configId, startTime, dataStartTime, exportLOCUS, journalFileName, IPAddress) VALUES (?, NOW(), ?, ?, ?, ?)";

      try (PreparedStatement findSrv = connection.prepareStatement(findSrvSQL);
            PreparedStatement findCfg = connection.prepareStatement(findCfgSQL);
            PreparedStatement findSch = connection.prepareStatement(findSchSQL);
            PreparedStatement findSlv = connection.prepareStatement(findSlvSQL);
            PreparedStatement findMap = connection.prepareStatement(findMapSQL);
            PreparedStatement insExec = connection.prepareStatement(insExSQL,
                  Statement.RETURN_GENERATED_KEYS)) {

         findSrv.setString(1, ipAddress);

         try (ResultSet findSrvRs = findSrv.executeQuery()) {
            if (!findSrvRs.next()) {
               throw new RepException("Server not found");
            }
            serverName = findSrvRs.getString("serverName");
         }

         findCfg.setString(1, configName);
         try (ResultSet findCfgRs = findCfg.executeQuery()) {
            if (!findCfgRs.next()) {
               throw new RepException("Configuration not found");
            }
            configId = findCfgRs.getInt("id");
            schemaPath = findCfgRs.getString("schemaPath");
            logAll = findCfgRs.getBoolean("logAll");
            logData = findCfgRs.getBoolean("logData");
            /*
            if (findCfgRs.getBoolean("sendInfo")) {
            	infoReceiver = findCfgRs.getString("infoReceiver");
            }
            if (findCfgRs.getBoolean("sendError")) {
            	errorReceiver = findCfgRs.getString("errorReceiver");
            }
            */
         }
         findSch.setString(1, serverName);
         File schemaDir = new File(schemaPath);
         try (ResultSet findSchRs = findSch.executeQuery()) {
            while (findSchRs.next()) {
               Schema.getInstance().addSchema(schemaDir,
                     findSchRs.getString("schemaName"));
            }
         }
         findSlv.setInt(1, configId);
         
         try (ResultSet findSlvRs = findSlv.executeQuery()) {
            while (findSlvRs.next()) {            	
            	SlaveProperty sProp = null;
            
               if (findSlvRs.getBoolean("mapping")) {            	   
                  findMap.setString(1, serverName);
                  findMap.setInt(2, findSlvRs.getInt("id"));

                  try (ResultSet findMapRs = findMap.executeQuery()) {
                	 
                	  findMapRs.beforeFirst();
                     sProp = SlaveProperty.newInstance(findSlvRs,
                           findMapRs);
                 
                  }
               } else {
            	   sProp = SlaveProperty.newInstance(findSlvRs,
                           null);            	   
               }
               
               Dialect dialect = SlaveFactory.createDialect(sProp
                       .getDialectName());
                 Slave slave = SlaveFactory.createSlave(sProp);
                 
                 SlaveController controller = SlaveController.newInstance(
                       sProp, slave, dialect);
                 slaveList.add(controller);
                 slaveIdList.add(findSlvRs.getInt("id"));
            }
         }
         Date dataStart = new Date(trans.getTime());
         insExec.setInt(1, configId);
         insExec.setTimestamp(2, new Timestamp(trans.getTime()));
         insExec.setTimestamp(3, new Timestamp(VarConverter.getExportLocus(dataStart)
               .getTime()));
         insExec.setString(4, journalFileName);
         insExec.setString(5, ipAddress);
         insExec.executeUpdate();
         try (ResultSet insExecRs = insExec.getGeneratedKeys()) {
            if (!insExecRs.next()) {
               throw new RepException("Cannot insert execution");
            }

            executeId = insExecRs.getInt(1);
         }
         connection.commit();

      }
      String transSQL = "INSERT INTO Replication.Transaction ( executionId, fnum, time, type, tableName, oldData, newData) VALUES (?, ?, ?, ?, ?, ?, ?)";
      String execSQL = "INSERT INTO Replication.ExecResult (slaveId, transactionId, result, fullTargetTable) VALUES (?, ?, ?, ?)";
      pstTrans = connection.prepareStatement(transSQL,
            Statement.RETURN_GENERATED_KEYS);
      pstExec = connection.prepareStatement(execSQL);

   }
   public static Connection getConnection() throws IOException, SQLException {
	   return getConnection(null, null);
   }
   public static Connection getConnection(String xUser, String xPass) throws IOException, SQLException {
      Properties prop = new Properties();
      
      
      FileInputStream inStream = new FileInputStream(
            System.getenv("CONNECTION_FILE"));
      prop.load(inStream);
      String driver = prop.getProperty("DB_DRIVER");
      String url = prop.getProperty("DB_URL");
      String user = prop.getProperty("DB_USER");
      String password = prop.getProperty("DB_PASSWORD");
      if (xUser != null) {
    	  user = xUser;
    	  password = xPass;
      }

      BasicDataSource bsd = new BasicDataSource();
      bsd.setUrl(url);
      bsd.setDriverClassName(driver);
      bsd.setUsername(user);
      bsd.setPassword(password);
      bsd.setMaxTotal(-1);
      Connection connection = bsd.getConnection();
      connection.setAutoCommit(false);
      
      return connection;
   }
  
  

   public void shutdown(Transaction trans) throws SQLException, IOException {
      String sql = "UPDATE Replication.Execution SET endTime = NOW(), dataEndTime = ? , status = ? WHERE id= ?";

      try (PreparedStatement pst = connection.prepareStatement(sql)) {
         pst.setTimestamp(1, new Timestamp(trans.getTime()));
         pst.setString(2, hasError ? "ERROR_FOUND" : "SUCCESS");
         pst.setInt(3, executeId);
         pst.executeUpdate();
         connection.commit();
      }
      /*
      for (int i = 0; i < slaveList.size(); i++) {
         if (slaveList.get(i).getSlave().getSlaveName().equals("TEST_LOCUS_DB")) {
            
            String sumSQL = "SELECT * FROM Replication.TEST_TABLE_REPORT WHERE slaveId = ? AND executionId = ?";
            try (PreparedStatement pst = connection.prepareStatement(sumSQL)) {
               pst.setInt(1, slaveIdList.get(i));
               pst.setInt(2, executeId);
               try (ResultSet rs = pst.executeQuery()) {
                  while (rs.next()) {
                     String fullTargetTable = rs.getString("fullTargetTable");

                     boolean success = rs.getInt("failRecord") == 0;
                     slaveList
                           .get(i)
                           .getSlave()
                           .insertTimeLoad(rs.getString("sourceTable"), rs.getString("sourceDatabase"),fullTargetTable, success, rs.getTimestamp("startTime"), rs.getTimestamp("endTime"));
                  }
               }
            }
         }       
      }*/
      
   }
  
   private Long insertTransaction(Transaction trans) throws SQLException,
         RepException {
      

      pstTrans.setInt(1, executeId);
      pstTrans.setInt(2, trans.getFileId());
      pstTrans.setTimestamp(3, new Timestamp(trans.getTime()));
      pstTrans.setString(4, trans.getType().name());
      pstTrans.setString(5, trans.getTableName());

      String oldData = null;
      String newData = null;
      if (logData) {
         oldData = trans.getOldRecord();
         newData = trans.getNewRecord();
      }
      if (oldData == null) {
         pstTrans.setNull(6, Types.LONGVARCHAR);
      } else {
         pstTrans.setString(6, oldData);
      }
      if (newData == null) {
         pstTrans.setNull(7, Types.LONGVARCHAR);
      } else {
         pstTrans.setString(7, newData);
      }
      pstTrans.executeUpdate();

      try (ResultSet rs = pstTrans.getGeneratedKeys()) {
         if (!rs.next()) {
            throw new RepException("Cannot insert transaction");
         }
         connection.commit();
         return rs.getLong(1);
      }
   }

   public synchronized void executeTransaction(Transaction trans) throws SQLException {
      Long transId = null;
      
      for (int i = 0; i < slaveList.size(); i++) {
         try {
            int exec = slaveList.get(i).execute(trans);
            int slvId = slaveIdList.get(i);
     
            if (exec < 0) {
               continue;
            }
         //   trans.printLog("Execute");
            if (logAll || exec >= 0) {
               if (transId == null) {
                  transId = insertTransaction(trans);
               }
               if (exec == 0) {
                  hasError = Boolean.TRUE;                
               }

               Log.getInstance().info("Execution %10d | %d | %s | %s", transId,
                     exec, trans, hasError);
               pstExec.setInt(1, slvId);
               pstExec.setLong(2, transId);
               pstExec.setBoolean(3, exec != 0);
               pstExec.setString(4, trans.getFullTargetTable());
               pstExec.executeUpdate();

            }
         } catch (IOException | SQLException | RepException e) {
            System.out.println(trans);
            e.printStackTrace();
         }
      }

      connection.commit();
     
   }

   public String getJournalFileName() {
      return journalFileName;
   }
  
}
