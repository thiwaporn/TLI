package fmtaddr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldAddressFormatter_OLD {
	HashMap<String,ArrayList<Pattern>> patternMap;
public FieldAddressFormatter_OLD() {
	patternMap = new HashMap<>();
	initPattern();
}
public FieldAddress formatLineAddress(LineAddress lineAddr) {

	String lineStr = lineAddr.getAddrLine1() + " " + lineAddr.getAddrLine2();
	FieldAddress fldAddr = new FieldAddress(lineStr);
	lineStr = lineStr.replaceAll("\\s+", " ").trim();
	lineStr = lineStr.replaceAll("\\s\\/", "/").trim();
	lineStr = match("zipcode", fldAddr, lineStr);
	lineStr = match("province", fldAddr, lineStr);
	lineStr = match("district", fldAddr, lineStr);
	lineStr = match("sub_district", fldAddr, lineStr);
	lineStr = match("road", fldAddr, lineStr);
	lineStr = match("soi", fldAddr, lineStr);
	lineStr = match("trok", fldAddr, lineStr);
	lineStr = match("village", fldAddr, lineStr);
	lineStr = match("home", fldAddr, lineStr);
	lineStr = match("moo", fldAddr, lineStr);
	lineStr = match("room", fldAddr, lineStr);
	lineStr = match("floor", fldAddr, lineStr);
	lineStr = match("building", fldAddr, lineStr);
	lineStr = match("postbox", fldAddr, lineStr);
	lineStr = match("number", fldAddr, lineStr);
	if (lineStr.trim().length() > 0) {
		String place = lineStr.trim();
		if (place.startsWith("บ.")) {
			if (fldAddr.get("home").isEmpty()) {
			if (place.indexOf("จำกัด") < 0 && place.indexOf("จก") < 0) {
				fldAddr.set("home_test", place.substring(2));
				place = "";
			}
			}
		} else if (place.matches("^ม.\\s?\\d\\S*\\s.*")) {
			place = place.replace("ม\\.\\s", "ม\\.");
			if (fldAddr.get("moo").isEmpty()) {
				if (place.indexOf(" ") > 0) {
				fldAddr.set("moo", place.substring(2,place.indexOf(" ")));
				place = place.substring(place.indexOf(" ")+1);
				} else {
					fldAddr.set("moo", place.substring(2));
					place = "";
				}
				
			}
		
			
		} else if (place.matches("\\d\\S*")) {		
			if (fldAddr.get("number").isEmpty()) {
				fldAddr.set("number", place);
				place = "";
			}
		}
	fldAddr.set("place", place);
	}
	return fldAddr;
}
private void initPattern() {
	addPattern("zipcode", "(?<value>\\d{5})$");
	addPattern("moo", "([\\s\\\\A]P?หมู่ที่\\s?)(?<value>\\d\\S*)");
	addPattern("moo", "([\\s\\\\A]P?หมู่\\s?)(?<value>\\d\\S*)");
	addPattern("moo", "([\\s\\\\A]P?[มฒใ]\\.?\\s?)(?<value>\\d\\S*)$");
	addPattern("province", "(?<value>กรุงเทพมหานคร)$");
	addPattern("province", "(?<value>กรุงเทพฯ)$");
	addPattern("province", "(?<value>กทม\\.)$");
	addPattern("province", "([\\s\\\\A]P?\\จ\\.\\s?)(?<value>กทม\\.)$");
	
	addPattern("province", "([\\s\\\\A]P?จ\\.\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("district", "([\\s\\\\A]P?เขต\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("district", "([\\s\\\\A]P?(กิ่ง)?อำเภอ\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	
	addPattern("district", "([\\s\\\\A]P?(กิ่ง)?อ\\.\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("sub_district", "([\\s\\\\A]P?แขวง\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("sub_district", "([\\s\\\\A]P?ตำบล\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("sub_district", "([\\s\\\\A]P?ต\\.\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("road", "([\\s\\\\A]P?ถนน\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("road", "([\\s\\\\A]P?ถ\\.\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("soi", "([\\s\\\\A]P?ซอย\\s?)(?<value>([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("soi", "([\\s\\\\A]P?ซ\\.\\s?)(?<value>([ก-๙\\p{Graph}]+\\s?)+)$");
	
	addPattern("trok", "([\\s\\\\A]P?ตรอก\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("village", "([\\s\\\\A]P?หมู่บ้าน\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("village", "([\\s\\\\A]P?มบ\\.\\s?)(?<value>[ก-๙]([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("home", "([\\s\\\\A]P?บ้าน\\s?)(?<value>[ก-๙]([ก-๙\\w\\p{Graph}]+\\s?)+)$");
	

	
	addPattern("room", "([\\s\\\\A]P?ห้อง\\s?)(?<value>\\s?([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("floor", "([\\s\\\\A]P?ชั้น\\s?)(?<value>\\s?([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("building", "([\\s\\\\A]P?อาคาร\\s?)(?<value>\\s?([ก-๙\\p{Graph}]+\\s?)+)$");
	addPattern("building", "([\\s\\\\A]P?ตึก\\s?)(?<value>\\s?([ก-๙\\p{Graph}]+\\s?)+)$");
	
	addPattern("postbox", "([\\s\\\\A]P?(ตู้)?\\s?ป\\.?ณ\\.?\\s?)(?<value>\\d+)$");	
	addPattern("postbox", "(^(ตู้)?\\s?ป\\.?ณ\\.?\\s?)(?<value>\\d+)$");
	addPattern("number", "^(?<value>\\d\\S*)");
	addPattern("number", "(^เลขที่\\s?)(?<value>\\d\\S*)");
	addPattern("number", "(^บ้านเลขที่\\s?)(?<value>\\d\\S*)");
	addPattern("number", "([\\s\\\\A]P?บ้านเลขที่\\s?)(?<value>\\d\\S*)");
	addPattern("number", "([\\s\\\\A]P?เลขที่\\s?)(?<value>\\d\\S*)");
	addPattern("number", "([\\s\\\\A]P?\\s?)(?<value>\\d\\S*)$");
	
}
private void addPattern(String name, String regExp) {
	
	ArrayList<Pattern> patternList  = patternMap.get(name);
	if (patternList == null) {
		patternList = new ArrayList<>();
		patternMap.put(name, patternList);
	}
	patternList.add(Pattern.compile(regExp));
	
}
private String match(String patternName, FieldAddress fldAddr, String lineStr) {
	
	ArrayList<Pattern> patternList  = patternMap.get(patternName);
	if (patternList == null) {
		return lineStr;
	}
	System.out.printf("Check %s\n", patternName);
	for (Pattern pattrn : patternList) {
		System.out.printf("\tRegExp  %s\n",  pattrn.pattern());
		Matcher matcher = pattrn.matcher(lineStr);
		if (patternName.equals("soi")) {
			while (matcher.find()) {
				System.out.printf("\t\tFound %s\n",  matcher.group("value"));
				if (fldAddr.get("soi").isEmpty()) {
					fldAddr.set(patternName, matcher.group("value"));
				} else {
				fldAddr.set(patternName, matcher.group("value") + " ซอย " + fldAddr.get("soi"));
				}
				lineStr = matcher.replaceFirst("");											
			}
		} else {
		if ( matcher.find()) {
			System.out.printf("\t\tFound %s\n",  matcher.group("value"));
			fldAddr.set(patternName, matcher.group("value"));
			lineStr = matcher.replaceFirst("");									
				break;				
		}
		}
	}
	lineStr = lineStr.trim();
	System.out.printf("\tAfter match %s\n", lineStr);
	return lineStr;
}
}
