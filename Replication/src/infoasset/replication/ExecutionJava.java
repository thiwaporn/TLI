package infoasset.replication;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class ExecutionJava {
	public static void main(String[] args) {		
		
		if (args.length != 1) {
			System.out.println("Invalid parameter  [configFile]");
			System.exit(1);
		}
		String xmlFileName = args[0];
		
		
				
		StringBuilder logHeader = new StringBuilder();
		logHeader.append(StringUtils.repeat('-', 80))
		.append("\n")
		.append(StringUtils.repeat('-', 80))
		.append("\n");
		System.setProperty("logHeader", logHeader.toString());
		System.setProperty("configFileName", xmlFileName);
		try {
			
			
			RepConfig config = RepConfig.newInstance(xmlFileName);										
			Master master= MasterFactory.getJava();	
			ArrayList<SlaveController> slaveList = config.getSlaveList();
			if (slaveList.isEmpty()) {
				throw new RepException("No active slave found");
			}
			RepProcess.newInstance(master, slaveList);
		} catch (RepException | IOException | ClassNotFoundException | SQLException | InterruptedException  e) {		
			e.printStackTrace();
			
		}
		
	}
	

}
