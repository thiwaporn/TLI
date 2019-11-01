package infoasset.replication;

import java.util.ArrayList;

class MasterTest implements Master {
	static MasterTest newInstance() {
		return new MasterTest();
	}
	int idx = 0; 
	private ArrayList<Transaction> transList;
	private MasterTest() {
		transList = new ArrayList<>();
		Transaction t1 = Transaction.newInstance(TransactionType.START);
		t1.setDataAsString("192.1.2.42");
		
		Transaction t2 = Transaction.newInstance(TransactionType.BUILD);
		t2.setDataAsString("appln5809@cunderwrite");
				
		Transaction t0 = Transaction.newInstance(TransactionType.SHUTDOWN);
		transList.add(t1);		
		//transList.add(t2);
		transList.add(t0);
	}
	public Transaction nextTransaction() {		
		return transList.get(idx++);
	}
	@Override
	public String getMasterName() {
	return "test";
	}

}
