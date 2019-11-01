package infoasset.schema.compare;

import java.util.ArrayList;

public class Server {

	private String serverName;
	private ArrayList<Database> databaseList = null;
	
	public Server(String serverName) {
		// TODO Auto-generated constructor stub
		databaseList = new ArrayList<Database>();
		this.serverName = serverName;
	}
	public String getServerName() {
		return serverName;
	}
	public void addDatabase(Database database) {
		databaseList.add(database);
	}
	public Database getDatabase(int index) {
		return databaseList.get(index);
	}
	public int getDatabaseCount() {
		return databaseList.size();
	}

}
