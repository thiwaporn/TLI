package infoasset.replication;

import java.io.IOException;
import java.sql.SQLException;

class SlaveFactory {

	public static Slave createSlave(SlaveProperty prop) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException {
		
		Slave slave = prop.getSlaveClass().newInstance();
		slave.config(prop);
		Log.getInstance().trace("Slave " + slave);
		
		return slave;
	}
	public static Dialect createDialect(String dialectName) {
		
		if (dialectName == null || dialectName.equalsIgnoreCase("MYSQL")) {
		return DialectMySQL.newInstance();
		} else if (dialectName.equalsIgnoreCase("wildcard")) {
			return DialectWildcard.newInstance();
			
		} else {
			return null;
		}
	}
	
}
