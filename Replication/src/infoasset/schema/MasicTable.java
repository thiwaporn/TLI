package infoasset.schema;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

/**
 * MASIC Table
 * @author Manisa
 * @since Sep 18, 2014
 */
public class MasicTable implements MasicInterface {
	public static MasicTable newInstance(MasicElement element) {
		if (Prefix.TABLE.equals(element.getPrefix())) {			
			MasicTable table = new MasicTable();
			String[] str = element.getCode().split("[\\s,]+");
			table.setTableName(str[0]);
			table.setType(MasicTableType.matchCode(str[1]));
			table.setComment(element.getComment());
			return table;
		} else {
			return null;
		}
	}
	public static MasicTable newInstance(String tableName, String tablePath, MasicTableType type) {
		MasicTable table = new MasicTable();
		table.setTableName(tableName);
		table.setTablePath(tablePath);
		table.setType(type);
		return table;
	}
	private String schemaName;
	private String tableName;	
	private String tablePath;
	private MasicTableType type;
	private ArrayList<MasicField> fieldList;
	private ArrayList<MasicKey> keyList;
	private String comment;
	private MasicTable() {	
		fieldList = new ArrayList<>();
		keyList = new ArrayList<>();
	}
	public String getSchemaName() {
		return schemaName;
	}
	protected void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;		
	}
	public String getTablePath() {
		return tablePath;
	}
	public void setTablePath(String tablePath) {
		this.tablePath = tablePath;
	}
	public MasicTableType getType() {
		return type;
	}
	public void setType(MasicTableType type) {
		this.type = type;
	}
	public void addField(MasicField field) {
		fieldList.add(field);
	}
	public int getFieldCount() {
		return fieldList.size();
	}
	public MasicField getFieldAt(int index) {
		return fieldList.get(index);
	}
	public MasicField getField(String name) {
		for (MasicField fld : fieldList) {
			if (fld.getFieldName().equals(name)) {
				return fld;
			}
		}
		return null;
	}
	public void addKey(MasicKey key) {
		keyList.add(key);		
	}
	public int getKeyCount() {
		return keyList.size();
	}
	public MasicKey getKeyAt(int index) {
		return keyList.get(index);
	}
	
	@Override
	public String getComment() {
		return comment;
	}
	@Override
	public void setComment(String comment) {
		this.comment = StringUtils.chomp(comment);
	}
	@Override
	public void appendComment(String comment) {
		if (this.comment == null) {
			setComment(comment);
		} else {
			setComment(getComment() + "\n" + comment);
		}		
	}
	
	public String getTableName(String physicalTablePath) {
		
		char[] formatTableName = getTableName().toCharArray();
		String schemaTablePath = getTablePath();
		
		int idx = 0;
		
		for (int i = 0; i < schemaTablePath.length(); i++) {
			if (schemaTablePath.charAt(i) == '#' || schemaTablePath.charAt(i) == '?') {
				for (; idx < formatTableName.length; idx++) {
					if (formatTableName[idx] == '#' || formatTableName[idx] == '?') {
						formatTableName[idx] = physicalTablePath.charAt(i);
						break;
					}
				}
			}
		}
		return new String(formatTableName);
		
	}
	@Override
	public String toString() {
		return "MasicTable [tableName=" + tableName + ", type=" + type + "]";
	}
	public MasicKey getPrimaryKey() {
		for (MasicKey key : keyList) {
			if (!key.isDuplicate()) {
				return key;
			}
		}	
		return null;
	}
	

}
