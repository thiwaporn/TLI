package infoasset.replication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class JournalTextFile implements JournalInterface {
	public static JournalTextFile newInstance(String fileName) throws FileNotFoundException {		
		JournalTextFile returnValue = null;
		returnValue = new JournalTextFile(fileName);		
		return returnValue;
	}
	private InputStream file;
	private String fileName; 
	private JournalTextFile(String fileName) throws FileNotFoundException {
		this.fileName = fileName;
			file = new FileInputStream(fileName);		
	}
	  public boolean read(byte[] b, int len) throws IOException
	  {	    
	    int rlen = 0;
	    
	    for (;;)
	    {   
	      rlen = file.read(b, 0, len);
	      if (rlen != 0)
	        break;
	    /*
	      if (sec < 5)
	        sec++;
	      try { Thread.sleep(sec*1000); }
	      catch (InterruptedException e) {}
	      */
	    }   
	    return rlen == len;
	  }
	  @Override
		public String getJournalName() {
			return fileName;
		}
}
