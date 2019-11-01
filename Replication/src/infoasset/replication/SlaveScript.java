package infoasset.replication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import manit.M;

class SlaveScript implements Slave {
	private static final TimeZone timeZone = TimeZone
			.getTimeZone("Asia/Bangkok");
	private static final Locale locale = new Locale("th", "TH");
	
	private FileOutputStream outFile;
	private String name;
	private String detail;
	String encoding = "tis620";

	public void config(SlaveProperty prop) throws IOException {		
		String fileName = prop.getFileName();
		boolean append = prop.isAppend();
		name = prop.getSlaveName();
		detail = prop.getFileName();
		Calendar cal = Calendar.getInstance(timeZone, locale);
		
		if (fileName == null) {
			fileName = System.getProperty("journalFileName");
			fileName = StringUtils.replace(fileName, "masic", "mysql");
			fileName = StringUtils.replacePattern(fileName, "@.*", ".sql");
			fileName = prop.getFilePath() + "/" + fileName;
			
		} else {
			String jrn = Configuration.getInstance().getJournalFileName();
			fileName = VarConverter.convert(fileName, jrn, cal);			
		}
		System.out.println("SCRIPT FILE = " + fileName);
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		String fdate = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG,
				DateFormat.LONG, locale).format(cal.getTime());
		outFile = new FileOutputStream(fileName, append);
		StringBuilder header = new StringBuilder();
		header = header.append("-- Replication SQL Script --\n");
		header = header.append("-- DATE ").append(fdate);
		header = header.append("\n");
		if (prop.getDialectName().equalsIgnoreCase("wildcard")) {
			encoding="utf8";
			outFile.write(header.toString().getBytes("utf8"));
		} else {
			outFile.write(M.utos(header.toString()));
		}

	}

	@Override
	public boolean executeSQL(String targetFileeName,ArrayList<String> sqlList) {
		for (String sql : sqlList) {
			try {
			
				if (encoding.equalsIgnoreCase("utf8")) {
					outFile.write(sql.getBytes());
				} else {
					outFile.write(M.utos(sql));
				}
			
			//   outFile.write(sql.getBytes());
				outFile.write(10);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	public String getSlaveName() {
		return name;
	}

	@Override
	public String getSlaveType() {
		return "SQL";
	}

	@Override
	public String getDetail() {
		return detail;
	}

	@Override
	public String toString() {
		return "SlaveScript [outFile=" + outFile + ", name=" + name
				+ ", detail=" + detail + "]";
	}

   
   @Override
   public void insertTimeLoad(String sourceTable, String sourceDatabase, String fullTargetTable, Boolean success, Timestamp startTime, Timestamp endTime)
         throws SQLException {
      Log.getInstance().debug("Full target " + sourceTable + " " + sourceDatabase + " " + fullTargetTable);
      // TODO Auto-generated method stub
      
   }


}
