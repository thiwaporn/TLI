package infoasset.replication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import manit.M;

public class SlaveScriptFolder implements Slave {
	private static final TimeZone timeZone = TimeZone.getTimeZone("Asia/Bangkok");
	private static final Locale locale = new Locale("th", "TH");
	private File outputFolder;
	private String name;
	private String detail;
	private HashMap<String,FileOutputStream> fileMap;
	public SlaveScriptFolder() {
		fileMap = new HashMap<>();
	}

	@Override
	public void config(SlaveProperty prop) throws IOException, SQLException, ClassNotFoundException {
		String fileName = prop.getFileName();

		String jrn = Configuration.getInstance().getJournalFileName();	
		Calendar cal = Calendar.getInstance(timeZone, locale);
				
			fileName = VarConverter.convert(fileName, jrn, cal);	
		
		
		outputFolder = new File(fileName);
		outputFolder.mkdirs();
		if (!prop.isAppend()) {

			FileUtils.cleanDirectory(outputFolder);
		}

		name = prop.getSlaveName();
		detail = prop.getFileName();
	}

	@Override
	public synchronized boolean executeSQL(String targetFileName, ArrayList<String> sqlList) {
		
		if (targetFileName == null) {
			return false;
		}
		try {
		FileOutputStream outFile = fileMap.get(targetFileName);
		if (outFile == null) {
			String dbName = StringUtils.substringAfter(targetFileName, "@");
			String tbName = StringUtils.substringBefore(targetFileName, "@");
			File file = new File(outputFolder, dbName + "_" + tbName + ".sql");
			
			outFile = new FileOutputStream(file, true);		
			
			String jrn = Configuration.getInstance().getJournalFileName();
			String header = "-- Generated from " + jrn + "\n";
			outFile.write(M.utos(header));
			fileMap.put(targetFileName,outFile);
		}
		
		// TODO Auto-generated method stub
	
			
			for (String sql : sqlList) {

				
				outFile.write(M.utos(sql));
				// outFile.write(sql.getBytes());
				outFile.write(10);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String getSlaveName() {
		return name;
	}

	@Override
	public String getSlaveType() {
		return "SQLFolder";
	}

	@Override
	public String getDetail() {
		return detail;
	}

	@Override
	public void insertTimeLoad(String sourceTable, String sourceDatabase, String fullTargetTable2, Boolean success, Timestamp startTime, Timestamp endTime)
			throws SQLException {
		// TODO Auto-generated method stub
System.out.printf("INSERT TIME %-15s.%-15s  to %-30s  %s %s\n",sourceTable, sourceDatabase, fullTargetTable2, startTime,endTime);
	}

	@Override
	public String toString() {
		return "SlaveScriptFolder [outputFolder=" + outputFolder + ", name=" + name + "]";
	}
	

}
