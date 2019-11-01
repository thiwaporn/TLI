package infoasset.replication;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;



public class SlaveTask implements Runnable {
	private SlaveController slave;
	private Transaction trans;
	private CountDownLatch countDown;

	SlaveTask(CountDownLatch countDown, SlaveController slave,  Transaction trans) {
		this.countDown = countDown;
		this.slave = slave;
		
		this.trans = trans;
	}
	@Override
	public void run() {
		try {
			int recordCount = slave.execute(trans);
			if (recordCount >= 0 && slave.getSlave() instanceof SlaveDB) {
			   ReplicationLog.getInstance().recordTransaction(trans, recordCount > 0);
			}
		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
			   if (slave.getSlave() instanceof SlaveDB) {
			      ReplicationLog.getInstance().recordTransaction(trans,false);
			   }
			} catch (IOException | SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		countDown.countDown();
	}
	



}
