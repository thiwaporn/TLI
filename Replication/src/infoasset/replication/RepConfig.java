package infoasset.replication;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class RepConfig  {
	private static RepConfig config = null;
	///
	public static RepConfig getInstance() throws RepException {
		if (config == null) {
			throw new RepException("Replication has not been configured");
		}
		return config;
	}

	public static RepConfig newInstance(String xmlFile) throws RepException,
			ClassNotFoundException, SQLException, IOException {
		if (config != null) {
			throw new RepException("Replication has been configured");
		}
		config = new RepConfig(xmlFile);		
		return config;
	}
	private String fileDNS;
	private String dirSchema;
	private String errorPath;
	private String repType;
	private ArrayList<SlaveController> slaveList;

	RepConfig(String xmlFile) throws RepException,
			ClassNotFoundException, SQLException, IOException {
		super();
		File file = new File(xmlFile);
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(file);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new RepException(e);
		}

		doc.getDocumentElement().normalize();
		NodeList confList = doc.getElementsByTagName("replication");
		if (confList.getLength() == 0) {
			throw new RepException(
					"Invalid XML File : configuration node is not found");
		} else if (confList.item(0).getNodeType() != Node.ELEMENT_NODE) {
			throw new RepException(
					"Invalid XML File : configuration node is not element node");
		}
		Element conf = (Element) confList.item(0);
		repType = conf.getAttribute("type");
		if (StringUtils.isEmpty(repType) || repType.equalsIgnoreCase("MASIC")) {
			repType = "MASIC";
			setMasicConfig(conf);			
		} else if (repType.equalsIgnoreCase("JAVA")) {
			setJavaConfig(conf);
		}
		NodeList sList = conf.getElementsByTagName("slave");		
		setSlave(sList);
	}
	public String getReplicationType() {
		return repType;
	}
	private void setMasicConfig(Element conf) {
	   Log.getInstance().info("masic replication");
		NodeList cList = conf.getElementsByTagName("config");		
		fileDNS = XMLUtils.getTextContent((Element) cList.item(0), "dns");		
		dirSchema = XMLUtils.getTextContent((Element) cList.item(0), "schema");
		Log.getInstance().trace("Schema Dir = " + dirSchema);
		errorPath = conf.getAttribute("errorPath");		
	}
	private void setJavaConfig(Element conf) throws RepException {
		NodeList mList = conf.getElementsByTagName("master");
		MasterJava.newInstance(mList);
	}
	public String getFileDNS() {
		return fileDNS;
	}

	public String getDirSchema() {
		return dirSchema;
	}

	public String getErrorPath() {
		return errorPath;
	}
	public ArrayList<SlaveController> getSlaveList() {
		return slaveList;
	}
	private void setSlave(NodeList sList) throws ClassNotFoundException, IOException, SQLException {
		slaveList = new ArrayList<>();
		int sCount = 0;
		for (int i = 0; i < sList.getLength(); i++) {
			Element slvNode = (Element) sList.item(i);
			SlaveProperty prop = SlaveProperty.newInstance(slvNode);
			if (prop == null) {
				continue;
			}
			try {
				sCount++;
				Slave slave = SlaveFactory.createSlave(prop);
				Log.getInstance().info("Slave " + sCount + " : Name= " + prop.getSlaveName());
				Log.getInstance().info("Slave " + sCount + " : Class = " + slave.getClass().getSimpleName());
				Log.getInstance().info("Slave " + sCount + " : Detail = " + slave.getDetail());
				ArrayList<String[]> mapList = prop.listTableMap();
				for (String[] map : mapList) {
					Log.getInstance().info("Slave " + sCount + " : Map " + Arrays.toString(map));
				}
				Dialect dialect = SlaveFactory.createDialect(prop
						.getDialectName());
				SlaveController controller = SlaveController.newInstance(prop, slave, dialect);
				slaveList.add(controller);
			} catch (InstantiationException | IllegalAccessException e) {
				Log.getInstance().getLogger().error("Cannot initiate slave {}",
						prop.getSlaveName());
			}
		}
	}

}
