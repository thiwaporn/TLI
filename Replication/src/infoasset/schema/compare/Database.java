package infoasset.schema.compare;

import java.util.ArrayList;

public class Database {

	private String databaseName;
	private ArrayList<Table> tableList = null;
	
	public Database(String databaseName) {
		// TODO Auto-generated constructor stub
		tableList = new ArrayList<Table>();
		this.databaseName = databaseName;
	}
	public String getDatabase() {
		return databaseName;
	}
	public void addTable(Table table) {
		tableList.add(table);
	}
	public Table getTable(int index) {
		return tableList.get(index);
	}
	public int getTableCount() {
		return tableList.size();
	}

}
