package test;

import fmtaddr.FieldAddress;

public class FieldAddrConsole implements FieldAddrOutput {
	public FieldAddrConsole() {
		
	}

	@Override
	public void insertNewRecord(FieldAddress fldAddr) {
		System.out.println(fldAddr);
		
	}
}
