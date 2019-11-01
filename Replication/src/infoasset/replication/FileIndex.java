package infoasset.replication;

import java.util.HashMap;

public class FileIndex {
	public static FileIndex getInstance() {
		if (fileIndex == null) {
			fileIndex = new FileIndex();
		}
		return fileIndex;
	}
	private static FileIndex fileIndex = null;
	private HashMap<Integer, String> fileMap;
	private FileIndex() {
		fileMap = new HashMap<>();
	}
	public synchronized void openFile(int fileId, String fileName) {
		fileMap.put(fileId, fileName);
	}
	public synchronized void closeFile(int fileId) {
		fileMap.remove(fileId);
	}
	public synchronized String getFileName(int fileId) {
		return fileMap.get(fileId);
	}

}
