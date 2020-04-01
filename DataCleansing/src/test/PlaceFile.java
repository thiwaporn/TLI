package test;

import java.io.IOException;
import java.io.RandomAccessFile;

import fmtaddr.LineAddress;

public class PlaceFile implements LineAddrInput {
	private RandomAccessFile raf;
	public PlaceFile(String fileName) throws IOException {
		raf = new RandomAccessFile(fileName, "r");
        raf.readLine(); // read header
	}
	@Override
	public LineAddress getNextRecord()  {
		String line;
		try {
			line = raf.readLine();
			line = new String(line.getBytes("ISO8859-1"),"UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		LineAddress lineAddr = null;
		if (line !=  null) {
			
		
		String[] section = line.split("\\|");
		lineAddr = new LineAddress();
		lineAddr.setAddrLine1(section[1]);
		lineAddr.setAddrLine2("");
		}
		return lineAddr;
		}
	}
	

