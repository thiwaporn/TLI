package fmtaddr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldAddressFormatter {

	
	private String wordPattern = "[ก-๙\\p{Graph}]+";

	public FieldAddressFormatter() {

	
	}

	
	public FieldAddress formatLineAddress(LineAddress lineAddr) {

		String lineStr = lineAddr.getAddrLine1() + " " + lineAddr.getAddrLine2();
		lineStr = lineStr.replaceAll("\\s+", " ").trim();
		FieldAddress fldAddr = new FieldAddress(lineStr);
		System.out.printf("%s\n", lineStr);
		lineStr = match(fldAddr,lineStr, "zipcode", "\\s?(?<value>\\d{5})\\Z");
		String checkLineStr = lineStr;
		fldAddr.set("check_place", checkLineStr);
		lineStr = match(fldAddr,lineStr, "province", "\\s?(?<value>กรุงเทพมหานคร)\\Z");
		lineStr = match(fldAddr,lineStr, "province", "\\s?(?<value>กรุงเทพฯ)\\Z");
		lineStr = match(fldAddr,lineStr, "province", "\\s?(?<value>กทม\\.?)\\Z");		
		lineStr = match(fldAddr,lineStr, "province", "\\s?จังหวัด\\s?(?<value>" + wordPattern + ")\\Z");
		lineStr = match(fldAddr,lineStr, "province", "\\s?จ\\.\\s?(?<value>" + wordPattern + ")\\Z");
		lineStr = match(fldAddr,lineStr, "district", "\\s?อำเภอ\\s?(?<value>" + wordPattern + ")\\Z");
		lineStr = match(fldAddr,lineStr, "district", "\\s?เขต\\s?(?<value>" + wordPattern + ")\\Z");
		lineStr = match(fldAddr,lineStr, "district", "\\s?อ\\.\\s?(?<value>" + wordPattern + ")\\Z");
		lineStr = match(fldAddr,lineStr, "district", "\\s?กิ่งอำเภอ\\s?(?<value>" + wordPattern + ")\\Z");
		lineStr = match(fldAddr,lineStr, "district", "\\s?กิ่งอ\\.\\s?(?<value>" + wordPattern + ")\\Z");
		
		lineStr = match(fldAddr,lineStr, "sub_district", "\\s?ตำบล\\s?(?<value>" + wordPattern + ")\\Z");
		lineStr = match(fldAddr,lineStr, "sub_district", "\\s?แขวง\\s?(?<value>" + wordPattern + ")\\Z");
		lineStr = match(fldAddr,lineStr, "sub_district", "\\s?ต\\.\\s?(?<value>" + wordPattern + ")\\Z");
		if (fldAddr.get("province").isEmpty() || fldAddr.get("district").isEmpty() || fldAddr.get("sub_district").isEmpty()) {
			fldAddr.set("province", "");
			fldAddr.set("district", "");
			fldAddr.set("sub_district", "");			 			
			fldAddr.set("place",  checkLineStr);
			return fldAddr;
		}
		
		lineStr = match(fldAddr,lineStr, "road", "\\sถนน\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "road", "\\sถ\\.\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");	
		lineStr = match(fldAddr,lineStr, "soi", "\\sซอย\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "soi", "\\sซ\\.\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");			
		lineStr = match(fldAddr,lineStr, "trok", "\\sตรอก\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "village", "\\sหมู่บ้าน\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "village", "\\sมบ\\.\\s?(?<value>(\\D" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "village", "\\sม\\.\\s?(?<value>(\\D" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "housing", "\\sบ้าน\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "moo", "\\sหมู่ที่\\s?(?<value>\\d\\S*)\\Z");
		lineStr = match(fldAddr,lineStr, "moo", "\\sหมู่\\s?(?<value>\\d\\S*)\\Z");
		lineStr = match(fldAddr,lineStr, "moo", "\\sม\\.\\s?(?<value>\\d\\S*)\\Z");

		lineStr = match(fldAddr,lineStr, "room", "\\sห้อง\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "floor", "\\sชั้น\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "building", "\\sอาคาร\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "building", "\\sตึก\\s?(?<value>(" + wordPattern + "\\s?)+)\\Z");
		lineStr = match(fldAddr,lineStr, "number", "(\\sเลขที่\\s?)?(?<value>\\d\\S*)\\Z");
		fldAddr.set("place", lineStr.trim());
		if (fldAddr.get("province").equals("กรุงเทพฯ") ||
			fldAddr.get("province").equals("กทม.")) {
				fldAddr.set("province", "กรุงเทพมหานคร");
			}
		lineStr = match(fldAddr,lineStr, "province", "\\s?(?<value>กรุงเทพมหานคร)\\Z");
		lineStr = match(fldAddr,lineStr, "province", "\\s?(?<value>กรุงเทพฯ)\\Z");
		lineStr = match(fldAddr,lineStr, "province", "\\s?(?<value>กทม\\.?)\\Z");	
		return fldAddr;
	}
	private String match (FieldAddress fldAddr, String lineStr, String fieldName, String pattern) {
		if (!fieldName.equals("soi")) {
		
		if (!fldAddr.get(fieldName).isEmpty()) {
			return lineStr;
		}
		}
		Matcher matcher = Pattern.compile(pattern).matcher(lineStr);
		if (matcher.find()) {
			if (fieldName.equals("soi") && !fldAddr.get("soi").isEmpty()) {
				fldAddr.set(fieldName, matcher.group("value") + " ซอย " + fldAddr.get(fieldName));
			} else {
			fldAddr.set(fieldName, matcher.group("value"));
			}
			lineStr = matcher.replaceAll("").trim();
		}

		return lineStr;
	}
	

}
