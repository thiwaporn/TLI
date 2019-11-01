package infoasset.replication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

public class DBDiff {
	public static DBDiff newInstance(Connection firstConnection, Connection secondConnection, String firstDB, String secondDB, String firstIP, String secondIP) throws SQLException {
		DBDiff diff = new DBDiff();
		diff.setConnection(firstConnection, secondConnection);
		diff.setFirstDB(firstDB, firstIP);
		diff.setSecondDB(secondDB,secondIP);
		
		diff.initMetaData();
		return diff;
	}
	private Connection firstConnection;
	private Connection secondConnection;
	private String firstDB;
	private String secondDB;
	private String firstIP;
	private String secondIP;
	private DatabaseMetaData meta;
	private DBDiff() {
		
	}
	private void setConnection(Connection firstConnection, Connection secondConnection) {
		this.firstConnection = firstConnection;
		this.secondConnection = secondConnection;
	}
	private void setFirstDB(String firstDB, String firstIP) {
		this.firstDB = firstDB;
		this.firstIP = firstIP;
	}
	private void setSecondDB(String secondDB, String secondIP) {
		this.secondDB = secondDB;
		this.secondIP = secondIP;
	}
	private void initMetaData() throws SQLException {
		meta = firstConnection.getMetaData();
	}
	public void compare() throws SQLException  {
		ArrayList<String> firstList = listTable(firstDB);
		ArrayList<String> secondList = listTable(secondDB);
		String[] fail = ArrayUtils.EMPTY_STRING_ARRAY;
		String[] notFound = ArrayUtils.EMPTY_STRING_ARRAY;
		String[] success  = ArrayUtils.EMPTY_STRING_ARRAY;
		for (String firstTable : firstList) {
			if (! secondList.contains(firstTable)) {
				System.out.printf("%-30s not found in %-30s\n", firstTable, secondDB);
				notFound = ArrayUtils.add(notFound, firstTable);
			}
		}
		for (String secondTable : firstList) {
			if (! firstList.contains(secondTable)) {
				System.out.printf("%-30s not found in %-30s\n", secondTable, firstDB);
				notFound = ArrayUtils.add(notFound, secondTable);
			}
		}	
						
		String diffFormat = "cmp -s %s %s";		
		String diffCmd = null;	
		Process pcs = null;
		int value = 0;			
		for (String firstTable : firstList) {
			if (secondList.contains(firstTable)) {
			   
			   String firstFile;
            try {
               firstFile = getFile(firstConnection, firstDB,  firstTable, firstIP, "1");
               String secondFile = getFile(secondConnection, secondDB,firstTable, secondIP, "2");
               diffCmd = String.format(diffFormat, firstFile, secondFile);
               
               
               pcs  = Runtime.getRuntime().exec(diffCmd);
               pcs.waitFor();
               value = pcs.exitValue();
               if (value == 0) {
                   System.out.printf("Compare table %-30s Success\n", firstTable);
                   success = ArrayUtils.add(success, firstTable);
               } else {
                   System.out.printf("Compare table %-30s Fail\n", firstTable);
                   fail = ArrayUtils.add(fail, firstTable);
               }
            } catch (InterruptedException | IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
			  
			   
				
 			
			}
		}
		System.out.printf("%-20s  count = %4d %s\n", "Not Found", notFound.length, Arrays.toString(notFound));
		System.out.printf("%-20s  count = %4d %s\n", "Fail", fail.length, Arrays.toString(fail));
		System.out.printf("%-20s  count = %4d %s\n", "Success", success.length, Arrays.toString(success));
	}
	private String getFile(Connection connect, String dbName, String tbName,String ip, String number) throws InterruptedException, IOException, SQLException {
	   String fileName = String.format("/tmp/%s_%s_%s.CSV", dbName, tbName, number);
	 
	   String cmd = String.format("ssh root@%s  rm %s", ip, fileName);
	   Runtime.getRuntime().exec(cmd).waitFor();	   
	   cmd = String.format("rm %s", fileName);
	   Runtime.getRuntime().exec(cmd).waitFor();
       
	   StringBuilder sqlFormat = new StringBuilder();
       sqlFormat = sqlFormat.append("SELECT * FROM `%s`.`%s`");
       sqlFormat = sqlFormat.append(" INTO OUTFILE '%s'");
       sqlFormat = sqlFormat.append(" FIELDS TERMINATED BY  ',' ENCLOSED BY '\"'");
       sqlFormat = sqlFormat.append(" LINES TERMINATED BY '\\n';");
       connect.createStatement().executeQuery(String.format(sqlFormat.toString(), dbName, tbName, fileName));
       cmd = String.format("scp root@%s:%s /tmp/", ip, fileName);
       Runtime.getRuntime().exec(cmd).waitFor();
	   	   return fileName;
	}
	private ArrayList<String> listTable(String databaseName) throws SQLException {
		ArrayList<String> tableList = new ArrayList<>();
		ResultSet result = meta.getTables(databaseName, null, null,null);
		while (result.next()) {
			tableList.add(result.getString("TABLE_NAME"));			
		}
		return tableList;
	}
}
