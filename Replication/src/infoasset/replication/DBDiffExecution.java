package infoasset.replication;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;


public class DBDiffExecution {
	public static void main(String[] args) {		
		
		if (args.length != 4) {
			System.out.println("Parameter  :  user1:password1@server1  user2:password2@server2 db1 db2");
			System.exit(1);
		}
		String server1 = args[0];
		String server2 = args[1];
		
		
		String firstDB = args[2];
		String secondDB = args[3];
			
		
		
		Connection firstConnect;
		Connection secondConnect;
		try {
			firstConnect = getDataSource(server1).getConnection();
			secondConnect = getDataSource(server2).getConnection();
			
			String firstIP = StringUtils.substringAfter(server1,"@");
			String secondIP = StringUtils.substringAfter(server2,"@");
			
			DBDiff diff = DBDiff.newInstance(firstConnect, secondConnect ,firstDB, secondDB, firstIP, secondIP);
			diff.compare();
			firstConnect.close();
			secondConnect.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Connection OK");		
	}
	private static BasicDataSource getDataSource(String server) {
	   String pattern = "(\\w+):(.*)@(.*)";
	   Matcher matcher = Pattern.compile(pattern).matcher(server);
	   //System.out.println("Matcher " + matcher.find());
	   if (!matcher.find()) {
	      return null;
	   }
	   
	   BasicDataSource dsource = new BasicDataSource();
	   dsource.setUsername(matcher.group(1));
       dsource.setPassword(matcher.group(2));
       dsource.setConnectionProperties("characterEncoding=tis620");
       dsource.setDriverClassName("com.mysql.jdbc.Driver");
       dsource.setUrl(StringUtils.join("jdbc:mysql://", matcher.group(3), "/"));
       
       return dsource;
	}
}
