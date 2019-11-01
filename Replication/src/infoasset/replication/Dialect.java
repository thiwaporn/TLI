package infoasset.replication;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public interface Dialect {
	public ArrayList<String> createDB(String schemaName);
	public ArrayList<String> create(String srcfileName, String targetFileName, String tableEncoding);
	public ArrayList<String> insert(long time, String srcFileName, String targetFileName, String data, String tableEncoding, String dataEncoding) throws UnsupportedEncodingException;
	public ArrayList<String> delete(long time, String srcFileName, String targetFileName,String data, String tableEncoding, String dataEncoding) throws UnsupportedEncodingException;
	public ArrayList<String> update(long time, String srcFileName, String targetFileName, String oldData, String newData, String tableEncoding, String dataEncoding) throws UnsupportedEncodingException;
	//public ArrayList<String> alterate(DatabaseMetaData dbMeta, String srcFileName, String targetFileName);
}
