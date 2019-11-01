package infoasset.utils;

public class InfoException extends Exception {

   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


public InfoException() {
      
   }
   
   public InfoException(String message) {
      super(message);
   
   }
   public InfoException(String msgFormat, Object... param) {
      super(String.format(msgFormat,  param));
   }
   public InfoException(Throwable cause) {
      this("Exception Type <%s> Message '%s'", cause.getClass().getSigners(), cause.getMessage());   
   }

   
   public InfoException(String message, Throwable cause) {
      this("%s Exception Type <%s> Message '%s' Customize Message '%s'", message, cause.getClass().getSigners(), cause.getMessage());
   }

}
