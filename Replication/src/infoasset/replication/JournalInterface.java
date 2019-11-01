package infoasset.replication;

import java.io.IOException;

public interface JournalInterface {
	public String getJournalName();
	public boolean read(byte[] b, int len) throws IOException;	  
}
