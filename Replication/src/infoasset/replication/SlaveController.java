package infoasset.replication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Replication Slave
 * 
 * @author Manisa
 * @since Sep 22, 2014
 */
public class SlaveController {
	public static SlaveController newInstance(SlaveProperty prop, Slave slave,
			Dialect dialect) {
		return new SlaveController(prop, slave, dialect);
	}

	private SlaveProperty prop;
	private Slave slave;
	private Dialect dialect;

	//private ArrayList<String> sqlList;

	protected SlaveController(SlaveProperty prop, Slave slave, Dialect dialect) {

		this.prop = prop;
		this.slave = slave;
		this.dialect = dialect;
	//	sqlList = new ArrayList<>();
	}

	public int execute(Transaction trans) throws SQLException, IOException {

		ArrayList<String>sqlList = null;
		String sourceFileName = FileIndex.getInstance().getFileName(trans.getFileId());
		String targetFileName = null;
		if (sourceFileName != null) {		   
		   targetFileName = prop.getTableName(sourceFileName);
		}
		trans.setFullTargetTable(targetFileName);
		
		switch (trans.getType()) {
		case START:
			sqlList = startServer(trans.getDataAsString().trim());
			break;
		case BUILD:
			sqlList = create(trans.getFileId(), trans.getDataAsString().trim());
			open(trans.getFileId(), trans.getDataAsString().trim());
			sourceFileName = trans.getDataAsString().trim();			
			targetFileName = prop.getTableName(sourceFileName);					
			break;
		case INSERT:
			sqlList = insert(trans.getFileId(), trans.getNewRecord(),trans.getTime());
			break;
		case DELETE:
			sqlList = delete(trans.getFileId(), trans.getOldRecord(), trans.getTime());
			break;
		case UPDATE:
			sqlList = update(trans.getFileId(), trans.getOldRecord(),
					trans.getNewRecord(), trans.getTime());
			break;
		case OPEN:
			open(trans.getFileId(), trans.getDataAsString().trim());
			break;
		case CLOSE:
		case PURGE:
			close(trans.getFileId());
			break;
		case SHUTDOWN:
			System.exit(0);
			break;
		default:
			break;
		}
		
		if (sqlList == null || sqlList.isEmpty()) {
		   return -1;
		}
		
		boolean success = slave.executeSQL(targetFileName,sqlList);
		if (!success) {
			Log.getInstance().failScript(getSlaveName(), sqlList);
			return 0;
		}
		return  sqlList.size();
	}

	private ArrayList<String> startServer(String ipAddress) {
	   ArrayList<String> sqlList = new ArrayList<>();
		if (!prop.isMapping()) {
			return sqlList;
		}
		for (String name : Schema.getInstance().getSchemaNames()) {
			String sql = "CREATE DATABASE IF NOT EXISTS " + name + ";";
			sqlList.add(sql);
		}
		return sqlList;
	}

	private void open(int fileId, String fileName) {
		// String targetFileName = prop.getTableName(fileName);
		FileIndex.getInstance().openFile(fileId, fileName);
	}

	private void close(int fileId) {
		FileIndex.getInstance().closeFile(fileId);
	}

	private ArrayList<String> create(int fileId, String fileName) {
	   ArrayList<String> sqlList = new ArrayList<>();
		String targetFileName = prop.getTableName(fileName);

		if (targetFileName == null) { // no map table found
			/*
			 * Log.getInstance().error("create targetFileNotFound : " +
			 * fileName);
			 */
			return sqlList;
		}
		String tableEncoding = prop.getTableEncoding(targetFileName);
		FileIndex.getInstance().openFile(fileId, targetFileName);
		sqlList = dialect.create(fileName, targetFileName, tableEncoding);
		return sqlList;
	}

	private ArrayList<String> insert(int fileId, String recData, long time) throws UnsupportedEncodingException {
	   ArrayList<String> sqlList = new ArrayList<>();
		String sourceFileName = FileIndex.getInstance().getFileName(fileId);

		if (sourceFileName == null) { // no map table found
			Log.getInstance().error("insert sourceFileNotFound : " + fileId);
			return sqlList;
		}
		String targetFileName = prop.getTableName(sourceFileName);
		
		if (targetFileName == null) { // no map table found
			/*
			 * Log.getInstance().error( "insert targetFileNotFound : " + fileId
			 * + " " + sourceFileName);
			 */
			return sqlList;
		}
		String tableEncoding = prop.getTableEncoding(targetFileName);
		String dataEncoding = prop.getDataEncoding(targetFileName);
		ArrayList<String> sql = dialect.insert(time,sourceFileName, targetFileName,
				recData, tableEncoding, dataEncoding);
		
		sqlList.addAll(sql);
		return sqlList;
	}

	private ArrayList<String> delete(int fileId, String recData, long time) throws UnsupportedEncodingException {
	   ArrayList<String> sqlList = new ArrayList<>();
		String sourceFileName = FileIndex.getInstance().getFileName(fileId);
		if (sourceFileName == null) { // no map table found
			Log.getInstance().error("delete sourceFileNotFound : " + fileId);
			return sqlList;
		}
		String targetFileName = prop.getTableName(sourceFileName);
		if (targetFileName == null) { // no map table found
			/*
			 * Log.getInstance().error( "delete targetFileNotFound : " + fileId
			 * + " " + sourceFileName);
			 */
			return sqlList;
		}
		  String tableEncoding = prop.getTableEncoding(targetFileName);
	        String dataEncoding = prop.getDataEncoding(targetFileName);
		sqlList.addAll(dialect.delete(time, sourceFileName, targetFileName, recData, tableEncoding, dataEncoding));
		return sqlList;
	}

	private ArrayList<String> update(int fileId, String oldRecord, String newRecord, long time) throws UnsupportedEncodingException {
	   ArrayList<String> sqlList = new ArrayList<>();
		String sourceFileName = FileIndex.getInstance().getFileName(fileId);
		if (sourceFileName == null) { // no map table found
			Log.getInstance().error("update sourceFileNotFound : " + fileId);
			return sqlList;
		}
		String targetFileName = prop.getTableName(sourceFileName);
		if (targetFileName == null) { // no map table found
			/*
			 * Log.getInstance().error( "update targetFileNotFound : " + fileId
			 * + " " + sourceFileName);
			 */
			return sqlList;
		}
		  String tableEncoding = prop.getTableEncoding(targetFileName);
	        String dataEncoding = prop.getDataEncoding(targetFileName);
		sqlList.addAll(dialect.update(time, sourceFileName, targetFileName,
				oldRecord, newRecord, tableEncoding, dataEncoding));
		return sqlList;
	}

	public String getSlaveName() {
		return prop.getSlaveName();
	}

	public Slave getSlave() {
		return slave;
	}
}
