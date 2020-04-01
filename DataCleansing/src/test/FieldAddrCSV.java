package test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import fmtaddr.FieldAddress;

public class FieldAddrCSV implements FieldAddrOutput {
	private FileOutputStream outFile;
	private String[] field = {"original", "compare", "place","number", "postbox", "building", "room", "floor","moo", "home","village", "trok", "soi", "road", "sub_district", "district", "province", "zipcode"};
	public FieldAddrCSV(String fileName) throws IOException {
		outFile = new FileOutputStream(fileName, false);
		writeLine(field);
		
	}
	@Override
	public void insertNewRecord(FieldAddress fldAddr) {
		
		String[] lineStr = new String[field.length];
		lineStr[0] = fldAddr.getOriginalStr();
		lineStr[1] = fldAddr.getFormatStr();
		for (int i = 2; i < field.length; i++) {
			lineStr[i] = fldAddr.get(field[i]);
			if (lineStr[i] == null) {
				lineStr[i] = "";
			}
		}
		try {
			writeLine(lineStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void writeLine(String[] str) throws IOException {
		String line = "";
		for (String column : str) {
			line += "\"" + column + "\"" + "\t";
		}
		
		outFile.write(line.getBytes());
		outFile.write(10);
	}
}
