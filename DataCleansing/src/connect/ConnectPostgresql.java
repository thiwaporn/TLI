package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConnectPostgresql {

	public Connection ConnectPostgresql(String url, String user, String pass) throws Exception {
		// TODO Auto-generated constructor stub
		
		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", pass);

		Connection conn = DriverManager.getConnection(url, props);
		return conn;
 
	}

}
