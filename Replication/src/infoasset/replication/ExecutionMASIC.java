package infoasset.replication;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class ExecutionMASIC {
	public static void main(String[] args) {		
		
		if (args.length != 2) {
			System.out.println("Invalid parameter  [configFile] [jrnFile]");
			System.exit(1);
		}
		String xmlFileName = args[0];
		String jrnFileName = args[1];
		
		String serverSeq = "XX";
		String fileName = StringUtils.substringAfterLast(jrnFileName, "/");
		if (fileName.matches("masic-\\d{2}-\\d{6}-\\d{4}.*")) {
			serverSeq = fileName.substring(6, 8);
		}
		
		StringBuilder logHeader = new StringBuilder();
		logHeader.append(StringUtils.repeat('-', 80))
		.append("\n")
		.append("Journal File : ")
		.append(jrnFileName)
		.append("\n")
		.append(StringUtils.repeat('-', 80))
		.append("\n");
		System.setProperty("logHeader", logHeader.toString());
		System.setProperty("serverSeq", serverSeq);
		System.setProperty("journalFileName", jrnFileName);
		try {
			
			if (!ReplicationLog.getInstance().checkPreviousComplete(jrnFileName)) {
			   throw new RepException("Previous journal file is not fixed");
			}
			RepConfig config = RepConfig.newInstance(xmlFileName);			
			DNSFile.getInstance();						
			Master master = MasterFactory.getJournal(jrnFileName);	
			ArrayList<SlaveController> slaveList = config.getSlaveList();
			if (slaveList.isEmpty()) {
				throw new RepException("No active slave found");
			}
			RepProcess.newInstance(master, slaveList);
		} catch (RepException | IOException | ClassNotFoundException | SQLException | InterruptedException e) {		
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	

}
