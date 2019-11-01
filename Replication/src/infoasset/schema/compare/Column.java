package infoasset.schema.compare;

public class Column {

	private String status="-";
	private String name="-";
	private String type="-";
	private int length=0;
	private int scale=0;
	
	public Column(String status) { //FOR DROP TABLE
		// TODO Auto-generated constructor stub
		this.status = status;
	}
	
	public Column(String status,String name) { //FOR DROP TABLE
		// TODO Auto-generated constructor stub
		this.status = status;
		this.name = name;
	}
	
	public Column(String status,String name,String type,int length) { //CHAR
		// TODO Auto-generated constructor stub
		this.status = status;
		this.name = name;
		this.type = type;
		this.length = length;
	}
	
	public Column(String status,String name,String type,int length,int scale) { //DECIMAL
		// TODO Auto-generated constructor stub
		this.status = status;
		this.name = name;
		this.type = type;
		this.length = length;
		this.scale = scale;
	}
	
	public String getStatus() {
		return status;
	}
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public int getLength() {
		return length;
	}
	public int getScale() {
		return scale;
	}
}
