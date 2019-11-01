package infoasset.schema;

import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import manit.M;

public class MasicRecord   {
	public static MasicRecord newInstance(MasicTable table, String data) {
		MasicRecord record = new MasicRecord(table);
		record.set(data);
		return record;
	}
	public static MasicRecord newInstance(MasicTable table) {
		MasicRecord record = new MasicRecord(table);		
		return record;
	}
	private HashMap<String, String> dataMap;
	private MasicTable table;
	private MasicRecord(MasicTable table) {
		this.table = table;
		dataMap = new HashMap<>();
		
	}
	public void set(String data) {
		int offset = 0;
		for (int i = 0; i < table.getFieldCount(); i++) {
			int len = table.getFieldAt(i).getLength();
			String fdata = data.substring(offset, offset + len);
			if (table.getFieldAt(i).getType().equals(MasicFieldType.NUMBER)) {
				int scale = table.getFieldAt(i).getScale();				
				if (scale > 0) {
					fdata = M.endot(fdata, scale);					
				} 
			} else {				
				fdata = fdata.replace("\n", "|");	
			}
			set(table.getFieldAt(i).getFieldName(), fdata);			
			offset += len;
		}
	}

	public String get(String fieldName) {
		
		return dataMap.get(fieldName);
	}
	public byte[] get() {
		byte[] dataByte = ArrayUtils.EMPTY_BYTE_ARRAY;
		
		for (int i = 0; i < table.getFieldCount(); i++) {
			int len = table.getFieldAt(i).getLength();			
			String data = get(table.getFieldAt(i).getFieldName());			
			if (table.getFieldAt(i).getType().equals(MasicFieldType.NUMBER)) {
				data = M.setlen(M.undot(data), len);				
			} else {
				data = StringUtils.rightPad(data, len, ' ');
			}			
			dataByte = ArrayUtils.addAll(dataByte, M.utok(data));					
		}		
		return dataByte;
	}

	public void set(String fieldName, String fdata) {
		MasicField fld = table.getField(fieldName);		
		if (fld.getType().equals(MasicFieldType.NUMBER)) {						
			fdata = toNumber(fdata, fld.getLength(), fld.getScale());
		//	Log.getInstance().trace("Table %-20s Field %-20s BF=%s AF=%s", table.getTableName(), fld.getFieldName(), before,fdata);
		} else {							
			fdata = fdata.replace("\n", "|");	
		}
		String result = "";
		for (int i = 0; i < fdata.length(); i++) {
			if (fdata.charAt(i) < 32) {
				result += " ";
			} else {
				result += fdata.charAt(i);
			}
		}
		dataMap.put(fieldName, result);
	}
	private String toNumber(String fdata, int length, int scale) {
		String result = null;
		if (fdata.matches("-*\\d+\\.*\\d*")) {
			result = fdata;
		} else {
			
			result = StringUtils.repeat('0', length - scale);
			result += "." + StringUtils.repeat('0', scale);			
		} 
		return result;
		
	}
}
