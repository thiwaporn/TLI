package infoasset.replication;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class LoadMapping  {
	public static void main(String[] args) {
	   try {
         new LoadMapping(args[0], args[1]);
      } catch (ClassNotFoundException | RepException | SQLException
            | IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
	}

	LoadMapping(String xmlFile, String serverName) throws RepException,
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
		NodeList slaveList = conf.getElementsByTagName("slave");
		StringBuilder sql = null;
		for (int i = 0; i < slaveList.getLength(); i++) {
		   Element slvNode = (Element)slaveList.item(i);
		   NodeList mapList = slvNode.getElementsByTagName("map");
		   for (int j = 0; j < mapList.getLength(); j++) {
		      Element map = (Element)mapList.item(j);
		      String source = map.getAttribute("source");
		      String target = map.getAttribute("target");
		      sql = new StringBuilder();
		      sql = sql.append("CALL `Replication`.`InsertMap`")
		            .append("('LOCUS_DB','").append(serverName).append("','")
		            .append(source.substring(source.indexOf("@") + 1)).append("','")
		            .append(source.substring(0, source.indexOf("@"))).append("','")
		                  .append(target.substring(target.indexOf("@") + 1)).append("','")
		                  .append(target.substring(0, target.indexOf("@"))).append("');");
		      
		      System.out.println(sql.toString());
		   }
		}
		
	}		
}
