package infoasset.replication;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SlaveProperty {
	public static SlaveProperty newInstance(ResultSet rsSlave, ResultSet rsMap)
			throws SQLException, ClassNotFoundException {
		SlaveProperty prop = new SlaveProperty();

		prop.setSlaveName(rsSlave.getString("name"));
		prop.setSlaveClass(Class.forName("infoasset.replication." + rsSlave.getString("type")));
		prop.setDriver(rsSlave.getString("driver"));
		prop.setUrl(rsSlave.getString("dbUrl"));
		prop.setUserName(rsSlave.getString("dbUser"));
		prop.setPassword(rsSlave.getString("dbPassword"));
		prop.setFileName(rsSlave.getString("fileName"));
		prop.setAppend(rsSlave.getBoolean("append"));
		if (rsSlave.getBoolean("wildcardField")) {
			prop.setDialectName("wildcard");
		}
		if (rsMap != null) {
			while (rsMap.next()) {
				String source = rsMap.getString("sourceTable") + "@" + rsMap.getString("sourceDatabase");
				String target = null;
				if (rsMap.getString("targetTable") == null) {
					target = rsMap.getString("sourceTable") + "@" + rsMap.getString("sourceDatabase");
				} else {
					target = rsMap.getString("targetTable") + "@" + rsMap.getString("targetDatabase");
				}

				String dataEncode = rsMap.getString("dataEncoding");
				String tableEncode = rsMap.getString("tableEncoding");

				prop.addMap(source, target, dataEncode, tableEncode);
			}
		}
		return prop;
	}

	public static SlaveProperty newInstance(Element slvNode) throws ClassNotFoundException {
		if (!XMLUtils.isTrue(slvNode, "active")) {
			return null;
		}
		SlaveProperty prop = new SlaveProperty();
		prop.setSlaveName(slvNode.getAttribute("name"));
		String clazzName = slvNode.getAttribute("class");
		if (clazzName == null) {
			String type = slvNode.getAttribute("type");
			if (type.equals("script")) {
				clazzName = "SlaveScript";
			} else if (type.equals("database")) {
				clazzName = "SlaveDB";
			} else if (type.equals("console")) {
				clazzName = "SlaveConsole";
			}
		}
		prop.setSlaveClass(Class.forName("infoasset.replication." + clazzName));
		prop.setDriver(XMLUtils.getTextContent(slvNode, "driver"));
		prop.setUrl(XMLUtils.getTextContent(slvNode, "url"));
		prop.setUserName(XMLUtils.getTextContent(slvNode, "userName"));
		prop.setPassword(XMLUtils.getTextContent(slvNode, "password"));
		prop.setFileName(XMLUtils.getTextContent(slvNode, "filename"));
		prop.setFilePath(XMLUtils.getTextContent(slvNode, "filepath"));
		prop.setAppend(StringUtils.equals("true", XMLUtils.getTextContent(slvNode, "append")));
		NodeList mapList = slvNode.getElementsByTagName("map");
		int mapSize = mapList.getLength();
		for (int j = 0; j < mapSize; j++) {
			Element mNode = (Element) mapList.item(j);
			prop.addMap(mNode.getAttribute("source"), mNode.getAttribute("target"), mNode.getAttribute("dataEncode"),
					mNode.getAttribute("tableEncode"));
			Log.getInstance().trace("map " + mNode.getAttribute("source") + " --> " + mNode.getAttribute("target"));
		}

		return prop;
	}

	private String slaveName;
	private Class<? extends Slave> slaveClass;
	private String fileName;
	private String filePath;
	private boolean append;
	private String driver;
	private String url;
	private String userName;
	private String password;
	private String dialectName;

	private HashMap<String, String> tableMap;
	private HashMap<String, String[]> encodingMap;

	private SlaveProperty() {
		tableMap = new HashMap<>();
		encodingMap = new HashMap<>();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isMapping() {
		return !tableMap.isEmpty();
	}

	public synchronized void addMap(String sourceTableName, String targetTableName, String dataEncoding,
			String tableEncoding) {

		tableMap.put(sourceTableName, targetTableName);
		encodingMap.put(targetTableName, new String[] { dataEncoding, tableEncoding });
	}

	public String getTableEncoding(String targetSchema, String targetTable) {
		return getTableEncoding(targetTable + "@" + targetSchema);
	}

	public String getTableEncoding(String targetFileName) {
		String[] encode = encodingMap.get(targetFileName);
		if (encode == null) {
			return "utf8";
		} else {
			return encode[1];
		}
	}

	public String getDataEncoding(String targetSchema, String targetTable) {
		return getDataEncoding(targetTable + "@" + targetSchema);
	}

	public String getDataEncoding(String targetFileName) {
		String[] encode = encodingMap.get(targetFileName);
		if (encode == null) {
			return "latin1";
		} else {
			return encode[0];
		}
	}

	public synchronized String getTableName(String searchTableName) {

		if (tableMap.isEmpty()) {
			return searchTableName;
		} else {

			Iterator<String> keySet = tableMap.keySet().iterator();
			while (keySet.hasNext()) {
				String key = keySet.next();
				if (key.equals(searchTableName)) {
					return tableMap.get(key);
				}
				if (searchTableName.length() != key.length()) {
					continue;
				}
				boolean match = true;
				String var = "";
				for (int j = 0; j < searchTableName.length(); j++) {
					char p = key.charAt(j);
					char t = searchTableName.charAt(j);
					if (p == '?' || p == '#') {
						var += t;
					}
					if (p == t || p == '?') {

					} else if (p == '#' && Character.isDigit(t)) {

					} else {
						match = false;
						break;
					}
				}
				if (match) {
					String targetTableName = tableMap.get(key);
					String returnValue = "";
					for (int i = 0; i < targetTableName.length(); i++) {
						if (targetTableName.charAt(i) == '?' || targetTableName.charAt(i) == '#') {
							returnValue += var.charAt(0);
							var = var.substring(1);
						} else {
							returnValue += targetTableName.charAt(i);
						}
					}
					return returnValue;
				}
			}
			return null;
		}
	}

	public String sourceTableName(String targetTableName) {
		return null;
	}

	public String getSlaveName() {
		return slaveName;
	}

	public void setSlaveName(String slaveName) {
		this.slaveName = slaveName;
	}

	public Class<? extends Slave> getSlaveClass() {
		return slaveClass;
	}

	public void setSlaveClass(Class<?> class1) {
		this.slaveClass = (Class<? extends Slave>) class1;
	}

	public String getDialectName() {
		return dialectName;
	}

	public void setDialectName(String dialectName) {
		if (dialectName == null) {
			dialectName = "MySQL";
		}
		this.dialectName = dialectName;
	}

	public ArrayList<String[]> listTableMap() {
		ArrayList<String[]> list = new ArrayList<>();
		Iterator<String> keyList = tableMap.keySet().iterator();
		while (keyList.hasNext()) {
			String key = keyList.next();
			String map = tableMap.get(key);
			list.add(new String[] { key, map });
		}
		return list;
	}

	@Override
	public String toString() {
		return "SlaveProperty [slaveName=" + slaveName + ", slaveClass=" + slaveClass + ", fileName=" + fileName
				+ ", driver=" + driver + ", url=" + url + ", userName=" + userName + ", password=" + password + "]";
	}

}
