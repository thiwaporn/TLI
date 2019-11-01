 package infoasset.replication;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;





public class Log {
	private static Log INSTANCE = null;
	private Logger defaultLogger;
	private Logger scriptLogger;
	private Log()  {				
		defaultLogger = LogManager.getLogger("infoasset.replication");		
		scriptLogger = LogManager.getLogger("infoasset.failscript");
		
	}
	public void debug(String message, Object... param) {
       defaultLogger.debug(String.format(message,param));
   }
    public void trace(String message, Object... param) {
       defaultLogger.trace( String.format(message, param));
   }
	public void info(String message, Object... param) {
		defaultLogger.info( String.format(message, param));
	}
	public void error(String message, Object... param) {
		defaultLogger.error(String.format(message, param));
	}
	

	public void failScript(String slaveName, ArrayList<String> sqlList) {
	   
		for (String sql : sqlList) {
			scriptLogger.info(sql);
		}
	}
	
	
	public final Logger getLogger() {
		return defaultLogger;
	}
	
	
	public static Log getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Log();
		}
		return INSTANCE;
	}
	
	
}
