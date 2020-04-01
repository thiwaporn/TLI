package fmtaddr;

import java.util.HashMap;
import java.util.Iterator;

public class FieldAddress {
	private final String originalStr;
	private String[]  arrField = {"place","number", "postbox", "building", "floor", "room","moo", "housing","village", "trok", "soi", "road", "sub_district", "district", "province", "zipcode"};
	private String[]  arrPrefix= {"","เลขที่", "ปณ.", "อาคาร", "ชั้น", "ห้อง","หมู่ที่", "บ้าน","หมู่บ้าน", "ตรอก", "ซอย", "ถนน", "ตำบล", "อำเภอ", "จังหวัด", ""};
	private HashMap<String,String> valueMap;
	
	public FieldAddress(String originalStr) {
		this.originalStr = originalStr;
		valueMap = new HashMap<>();
	}
	public final String getOriginalStr() {
		return originalStr;
	}
	public void set(String fieldName, String value) {
		valueMap.put(fieldName, value.trim());
	}
	
	public String get(String fieldName) {
		String value = valueMap.get(fieldName);
		if (value == null) {
			value = "";
		}
		return value;
	}
	@Override
	public String toString() {
		String str = "";
		Iterator<String> fieldIt = valueMap.keySet().iterator();
		while (fieldIt.hasNext()) {
			String key = fieldIt.next();
			str += String.format("%s=%s,", key, valueMap.get(key));
		}
		return str;
	}
	
	public String getFormatStr() {
		String lineStr="";
		for (int i = 0; i < arrField.length; i++) {
			String value = get(arrField[i]);
			if (!value.isEmpty()) {
				lineStr +=  arrPrefix[i]  + value + " ";
			}
		}
		lineStr = lineStr.replaceAll("\\s+", " ").trim();
		return lineStr;
	}

}
