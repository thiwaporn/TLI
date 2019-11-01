package infoasset.schema;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class MasicSchema implements MasicInterface {
	public static MasicSchema newInstance(String schemaName) {
		MasicSchema schema = new MasicSchema();
		schema.setSchemaName(schemaName);
		return schema;
	}

	private String schemaName;
	private String dataPath;
	private String tempPath;
	private String comment;
	private ArrayList<MasicTable> tableList;
	private ArrayList<String> nameList;
	private MasicSchema() {
		tableList = new ArrayList<>();
		nameList = new ArrayList<>();
	}
	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	public String getDataPath() {
		return dataPath;
	}
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}
	public String getTempPath() {
		return tempPath;
	}
	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}
	public void addTable(MasicTable table) {
		tableList.add(table);
		nameList.add(table.getTableName());
	}
	public int getTableCount() {
		return tableList.size();
	}
    public MasicTable getTableByPath(String tablePath) {

        tablePath = StringUtils.removeStart(tablePath, dataPath);
        int tableCount = tableList.size();

        for (int i = 0; i < tableCount; i++) {
            MasicTable tb = tableList.get(i);

            if (tb.getTablePath().equals(tablePath)) {
                return tb;
            }

            if (tb.getTablePath().length() != tablePath.length()) {
                continue;
            }

            //if (tb.getTablePath().length() != )
            boolean match = true;

            for (int x = 0; x < tablePath.length() && match; x++) {

                if (tb.getTablePath().charAt(x) == tablePath.charAt(x) || tb.getTablePath().charAt(x) == '?') {
                    continue;
                } else if (tb.getTablePath().charAt(x) == '#' && Character.isDigit(tablePath.charAt(x))) {
                    continue;
                } else {
                    match = false;
                }
            }
            if (match) {
                return tb;
            }

        }

        return null;
    }

	public MasicTable getTableByName(String tableName) {
		int idx = nameList.indexOf(tableName);
		if (idx >= 0) {
			return getTableAt(idx);
		} else {			
			for (int i = 0; i < nameList.size(); i++) {
				String name = nameList.get(i);
				if (name.length() != tableName.length()) {
					continue;
				}
				boolean match = true;
				for (int j = 0; j < tableName.length(); j++) {
					char p = name.charAt(j);
					char t = tableName.charAt(j);
					
					if (p == t || p == '?') {
						
					} else if (p == '#' && Character.isDigit(t)) {
						
					} else {
						match = false;
						break;
					}
				}
				if (match) {
					return getTableAt(i);
				}
			}
			return null;
		}
	}
	public MasicTable getLastTable() {
		return getTableAt(getTableCount() - 1);
	}
	public MasicTable getTableAt(int index) {
		return tableList.get(index);
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
	@Override
	public String toString() {
		return "MasicSchema [schemaName=" + schemaName + ", dataPath="
				+ dataPath + ", tempPath=" + tempPath + ", comment=" + comment
				+ ", tableCount=" + getTableCount() 
				+ "]";
	}
	
}
