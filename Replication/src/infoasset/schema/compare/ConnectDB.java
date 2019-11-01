package infoasset.schema.compare;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectDB {

	private static Connection conn = null;
	
	public static Connection getConnection() {
		
		try {
			String url = "jdbc:mysql://206.1.1.137:3306/Replication";
			String user = "infoasset";
			String pass = "infoasset";
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, pass);
			
			if(conn != null){
				System.out.println("Database Connected");
			}else{
			System.out.println("Database Connect Failed");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
}
