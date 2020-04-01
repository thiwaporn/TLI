package fmtaddr;

import java.util.ArrayList;

public class TelephoneFormatter {	
	public TelephoneFormatter() {
	
	}

	public ArrayList<String[]> formatPhone(String phone, String extension) {		
		phone = phone == null ? "" : phone.trim();
		phone = phone.toUpperCase();
		phone = phone.replaceAll("\\s", "");
		phone = phone.replaceAll("มือถือ", "");
		phone = phone.replaceAll("[\\s\\:\\(\\)]", "");
		phone = phone.replaceAll("[\\s\\;\\(\\)]", "");
		phone = phone.replaceAll("[\\s\\+\\(\\)]", "");
		phone = phone.replaceAll("[มือถือ\\.\\(\\)]", "");
		phone = phone.replaceAll("[โทร\\(\\)]", "");
		extension = extension == null ? "" : extension.trim();		
		ArrayList<String[]> phoneList = new ArrayList<>();
		String[] arrPhone =  phone.split("\\,");
		for (String strPhone : arrPhone) {
			int idx = phoneList.size() - 1;		
			String[] part = strPhone.split("[\\*\\#(EXT\\.@)]");
			if (part == null || part.length == 0) {
				continue;
			}
			String[] data = new String[]{part[0], extension, "T"};
			if (data[0].indexOf("H") >= 0) {
				data[2] = "H";
			} else if (data[1].indexOf("O") >= 0) {
				data[2] = "O";
			} 
			data[0] = data[0].replaceAll("[A-Z]", "");
			if (part.length > 1) {
				if (extension.isEmpty()) {
				data[1] = part[1];
				} else {
					data[1] = part[1] + "," + extension;
				}
			} 
			int dash = data[0].indexOf("-");
			while (dash >= 0 && dash < 7) {
				data[0] = data[0].substring(0,dash) + data[0].substring(dash + 1);
				dash = data[0].indexOf("-");
			}
			if (data[0].matches("\\d{7}")) {
				data[0] = "02" + data[0];
				}
			if (data[0].length() < 7 && !phoneList.isEmpty()) {				
				int len = phoneList.get(idx)[0].length();
				if (len >= 7) {
					int dif = len - data[0].length();
					data[0] = phoneList.get(idx)[0].substring(0,dif) + data[0];
				}
			}
			
			int plus = data[0].indexOf("+");
			while (plus >= 0 && plus < 7) {
				data[0] = data[0].substring(0,plus) + data[0].substring(plus + 1);
				plus = data[0].indexOf("+");
			}
			if (data[0].matches("\\d{7}")) {
				data[0] = "02" + data[0];
				}
			if (data[0].length() < 7 && !phoneList.isEmpty()) {				
				int len = phoneList.get(idx)[0].length();
				if (len >= 7) {
					int dif = len - data[0].length();
					data[0] = phoneList.get(idx)[0].substring(0,dif) + data[0];
				}
			}
			
			if(data[0].length() >= 2 && data[0].length() <= 4 && data[1] != "") {data[0] = data[0] + data[1] ; data[1] = "" ;}; 
			
			phoneList.add(data);
		}
		
		printResult(phoneList);
		return phoneList;
	}
	private void printResult(ArrayList<String[]> phoneList) {
			
		for (int i = 0; i < phoneList.size(); i++) {
			String[] fmtPhone = phoneList.get(i);
			System.out.printf("\t%2d) %s, %-20s Ext %s\n", i, fmtPhone[2], fmtPhone[0], fmtPhone[1]);
		}

	}
}
