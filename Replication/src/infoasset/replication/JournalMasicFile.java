package infoasset.replication;

import java.io.FileNotFoundException;
import java.io.IOException;

import manit.JournalFile;

public class JournalMasicFile implements JournalInterface {
	public static JournalMasicFile newInstance(String fileName) throws FileNotFoundException {		
		JournalMasicFile returnValue = null;
		returnValue = new JournalMasicFile(fileName);		
		return returnValue;
	}
	private JournalFile file;
	private String fileName; 
	private JournalMasicFile(String fileName) throws FileNotFoundException {
		this.fileName = fileName;
		file = new JournalFile();
		if (!file.open(fileName, true)) {
			throw new FileNotFoundException();
		}
		
	}
	  public boolean read(byte[] b, int len) throws IOException
	  {	    
		  return file.read(b, len);		
	  }
	@Override
	public String getJournalName() {
		return fileName;
	}
	  
	  

}
