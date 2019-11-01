package infoasset.replication;

/**
 * Exception
 * @author Manisa
 * @since Sep 22, 2014
 */
public class RepException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	RepException(String message) {
		super(message);
	}
	RepException(String format, Object... param) {
       super(String.format(format, param));
   }
	RepException(Throwable cause) {
		super(cause);
	}	
}
