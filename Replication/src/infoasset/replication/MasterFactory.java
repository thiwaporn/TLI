package infoasset.replication;

import java.io.FileNotFoundException;

class MasterFactory {

	
	static MasterJournal getJournal(String jrnFileName) throws FileNotFoundException {
		JournalInterface file = null;
		Log.getInstance().trace("JournalFileName=" + jrnFileName);
		if (jrnFileName.indexOf("@") > 0) {
			
			file = JournalMasicFile.newInstance(jrnFileName);
		} else {
			file = JournalTextFile.newInstance(jrnFileName);
		}
		return MasterJournal.newInstance(file);
	}
	static MasterJava getJava() {
		return MasterJava.getInstance();
	}
	static MasterTest getTest() {
		return MasterTest.newInstance();
	}
}
