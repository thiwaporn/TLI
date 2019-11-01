package infoasset.replication;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLUtils {

	public static String getTextContent(Element elem, String tagName) {
		NodeList list = elem.getElementsByTagName(tagName);
		if (list.getLength() != 1) {
			return null;
		} else {
			return list.item(0).getTextContent();
		}		
	}
	public static boolean isTrue(Element elem, String attributeName) {
		String str = elem.getAttribute(attributeName);		
		return StringUtils.equals("true", str);
	}
	

}
