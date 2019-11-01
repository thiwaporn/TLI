package infoasset.schema.compare;

import java.util.ArrayList;

public class Table {

	private String tableName;
	private String status;
	private ArrayList<Column> columnList = null;
	
	public Table(String tableName) {
		// TODO Auto-generated constructor stub
		columnList = new ArrayList<Column>();
		this.tableName = tableName;
	}
	public Table(String tableName,String status) {
		// TODO Auto-generated constructor stub
		this.tableName = tableName;
		this.status = status;
	}
	public String getTableName() {
		return tableName;
	}
	public String getStatus() {
		return status;
	}
	public void addColumn(Column column) {
		// TODO Auto-generated method stub
		columnList.add(column);
	}
	public Column getColumn(int index) {
		return columnList.get(index);
	}
	public int getColumnCount() {
		return columnList.size();
	}
}
