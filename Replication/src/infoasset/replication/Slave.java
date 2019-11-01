package infoasset.replication;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public interface Slave {
	public void config(SlaveProperty prop) throws IOException, SQLException,
			ClassNotFoundException;
	public boolean executeSQL(String targetFileName,ArrayList<String> sqlList);
	public String getSlaveName();
	public String getSlaveType();
	public String getDetail();
	public void insertTimeLoad(String sourceTable, String sourceDatabase, String fullTargetTable2, Boolean success, Timestamp startTime, Timestamp timestamp) throws SQLException;
}

