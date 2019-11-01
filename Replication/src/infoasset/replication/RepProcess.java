package infoasset.replication;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

class RepProcess {
	static RepProcess newInstance(Master master, ArrayList<SlaveController> slaveList) throws IOException, RepException, InterruptedException, SQLException {
		return new RepProcess(master, slaveList);
	}
	private Master master;
	private ArrayList<SlaveController> slaveList;
	
	RepProcess(Master master, ArrayList<SlaveController> slaveList) throws IOException, RepException, InterruptedException, SQLException {
		this.master = master;
		this.slaveList = slaveList;
		startRep();
	}
	
	private void startRep() throws IOException, RepException, InterruptedException, SQLException {
		
		Log.getInstance().getLogger().info("Hello Info");
		Log.getInstance().getLogger().error("Hello Error");
		CountDownLatch countDown ;
		ReplicationLog.getInstance().startReplication(master.getMasterName());
		for (SlaveController ctrl : slaveList) {
			ReplicationLog.getInstance().insertSlave(ctrl.getSlave());
		}
		ExecutorService service = Executors.newFixedThreadPool(slaveList.size());
		for (;;) {			
			Transaction trans = master.nextTransaction();
			
			Log.getInstance().trace(trans.toString());
			if (trans.isInvalid()) {
				ReplicationLog.getInstance().insertInvalidCharacter(trans.getTableName(), trans.getTime(), trans.getType());
			}
			
			
			if (trans.getType().equals(TransactionType.SHUTDOWN)) {
			   ReplicationLog.getInstance().recordTransaction(trans, true);
				break;
			}
			else if (trans.getType().equals(TransactionType.START)){
				Log.getInstance().trace("load schema from [" + trans.getDataAsString() + "]");
				ReplicationLog.getInstance().recordTransaction(trans, true);
				if (RepConfig.getInstance().getReplicationType().equalsIgnoreCase("MASIC")) {				  
					ArrayList<String> schemaNameList = DNSFile.getInstance().getSchemaList(trans.getDataAsString().trim());
					for (String schemaName : schemaNameList) {
						boolean added = Schema.getInstance().addSchema(schemaName);
						if (added) {
							Log.getInstance().getLogger().debug("Schema : {}", schemaName);
						}
					}
				}
			} else if (trans.getType().equals(TransactionType.OPEN)) {
				FileIndex.getInstance().openFile(trans.getFileId(), trans.getDataAsString());				
			} else {					
				
			}
			countDown = new CountDownLatch(slaveList.size());			
			for (int i = 0; i < slaveList.size(); i++) {				
				service.execute(new SlaveTask(countDown,slaveList.get(i),trans));
			}			
			countDown.await();				
		}
		service.shutdown();
		ReplicationLog.getInstance().endReplication();
	}

}
