package test;

import java.io.FileNotFoundException;
import java.io.IOException;

import fmtaddr.FieldAddress;
import fmtaddr.FieldAddressFormatter;
import fmtaddr.LineAddress;

public class FieldAddressFormatTester {
	public static void main(String[] args) {
		try {
			new FieldAddressFormatTester();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private int MAX_RECORD = 1_000;
	private FieldAddressFormatTester() throws IOException {
		int recordCount = 0;
		FieldAddressFormatter format = new FieldAddressFormatter();
		LineAddrInput lineInput = new PlaceFile("/tmp/place_v1.csv");
		//FieldAddrOutput fieldOutput = new FieldAddrConsole();
		FieldAddrOutput fieldOutput = new FieldAddrCSV("/tmp/address.csv");
		while (MAX_RECORD < 0 || recordCount <= MAX_RECORD) {
			recordCount++;
		    LineAddress lineAddr = lineInput.getNextRecord();
            if (lineAddr == null) {
                break;
            }
    
          
 
			
			FieldAddress fldAddr = format.formatLineAddress(lineAddr);
			
			//System.out.printf("%,8d %s\n",recordCount,lineAddr);
		//	System.out.printf("%,8d %s\n",recordCount,fldAddr);
			fieldOutput.insertNewRecord(fldAddr);
			
		}
	}
}
