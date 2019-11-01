package infoasset.replication;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import infoasset.schema.MasicField;
import infoasset.schema.MasicFieldType;
import infoasset.schema.MasicKey;
import infoasset.schema.MasicRecord;
import infoasset.schema.MasicSchema;
import infoasset.schema.MasicTable;
import manit.M;

public class DialectMySQL implements Dialect {

	public static DialectMySQL newInstance() {
	  
		return new DialectMySQL();
	}

	@Override
	public ArrayList<String> create(String srcFileName, String targetFileName, String tableEncoding) {
		ArrayList<String> sqlList = new ArrayList<>();
		String schemaName = getSchemaName(srcFileName);
		String tableName = getTableName(srcFileName);
		MasicTable table = getMasicTable(schemaName, tableName);
		if (table == null) {
			Log.getInstance().getLogger()
					.error("Table Not Found : {}", tableName);
			return sqlList;
		}
		
		String targetSchema = getSchemaName(targetFileName);
		String targetTable = getTableName(targetFileName);
		
		sqlList.add("DROP TABLE IF EXISTS `" + targetSchema + "`.`" + targetTable
				+ "`;\n");
		StringBuilder sql = new StringBuilder();
		sql = sql.append("CREATE TABLE IF NOT EXISTS `").append(targetSchema)
				.append("`.`").append(targetTable).append("` (\n");
		for (int i = 0; i < table.getFieldCount(); i++) {
			MasicField field = table.getFieldAt(i);
			String name = field.getFieldName();
			String type = "";
			if (field.getType().equals(MasicFieldType.TEXT)) {
				if (field.getLength() > 255) {
					type = "TEXT(" + field.getLength() + ")";
				} else {
					type = "CHAR(" + field.getLength() + ")";
				}
			} else if (field.getType().equals(MasicFieldType.NUMBER)) {
				type = "NUMERIC(" + field.getLength() + "," + field.getScale()
						+ ")";
			}
			if (i > 0) {
				sql = sql.append(",\n");
			}
			sql = sql.append(String.format("\t%-30s %s", "`"+ name + "`", type));
		}
		for (int i = 0; i < table.getKeyCount(); i++) {
			MasicKey key = table.getKeyAt(i);
			if (!key.isDuplicate()) {
				sql = sql.append(",\n\tPRIMARY KEY (");
				for (int j = 0; j < key.getSegmentCount(); j++) {
					if (j > 0) {
						sql = sql.append(",");
					}
					sql = sql.append(key.getFieldAt(j).getFieldName());
				}
				sql = sql.append(")\n");
				break;
			}
		}
		sql = sql.append(") DEFAULT CHARACTER SET = ")
		      .append(tableEncoding).append(";");
		sqlList.add(sql.toString());

		return sqlList;
	}

	@Override
	public ArrayList<String> insert(long time, String srcFileName, String targetFileName,
			String data, String tableEncoding, String dataEncoding) throws UnsupportedEncodingException {
		ArrayList<String> sqlList = new ArrayList<>();
		String schemaName = getSchemaName(srcFileName);
		String tableName = getTableName(srcFileName);
		MasicTable table = getMasicTable(schemaName, tableName);
		if (table == null) {
			return sqlList;
		}
		String targetSchema = getSchemaName(targetFileName);
		String targetTable = getTableName(targetFileName);
		
		StringBuilder sql = new StringBuilder();
		StringBuilder value = new StringBuilder();
		//Log.getInstance().debug("source=%s target = %s data = (%d) %s", srcFileName, targetFileName, data.length(), data);
		MasicRecord mRecord = MasicRecord.newInstance(table, data);
		sql = sql.append("INSERT INTO ").append(targetSchema).append(".")
				.append(targetTable).append(" (");
		for (int i = 0; i < table.getFieldCount(); i++) {
			if (i > 0) {
				sql = sql.append(",");
				value = value.append(",");
			}
			MasicField field = table.getFieldAt(i);
			sql = sql.append("`").append(field.getFieldName()).append("`");
			value = value.append(fieldValue(mRecord.get(field.getFieldName()),field.getLength(), tableEncoding, dataEncoding));
		}
		sql.append(") VALUES (").append(value).append(");");
		sqlList.add(sql.toString());
		return sqlList;
	}

	@Override
	public ArrayList<String> delete(long time, String srcFileName, String targetFileName,
			String data, String tableEncoding, String dataEncoding) throws UnsupportedEncodingException {
		ArrayList<String> sqlList = new ArrayList<>();
		String schemaName = getSchemaName(srcFileName);
		String tableName = getTableName(srcFileName);
		MasicTable table = getMasicTable(schemaName, tableName);
		if (table == null) {
			return sqlList;
		}
		String targetSchema = getSchemaName(targetFileName);
		String targetTable = getTableName(targetFileName);
		StringBuilder sql = new StringBuilder();
		sql = sql.append("DELETE FROM ").append(targetSchema).append(".")
				.append(targetTable).append(" WHERE ");
		MasicRecord mRecord = MasicRecord.newInstance(table, data);
		MasicKey primaryKey = table.getPrimaryKey();
		if (primaryKey == null) {
			for (int i = 0; i < table.getFieldCount(); i++) {
				if (i > 0) {
					sql = sql.append(" AND ");
				}
				MasicField field = table.getFieldAt(i);
				sql = sql.append(dbFieldValue(field.getFieldName()))
						.append(" = ");
				sql = sql.append(fieldValue(mRecord.get(field.getFieldName()), field.getLength(), tableEncoding, dataEncoding));				
			}
			sql.append("LIMIT 1;");
		} else {
			for (int i = 0; i < primaryKey.getSegmentCount(); i++) {
				if (i > 0) {
					sql = sql.append(" AND ");
				}
				MasicField field = primaryKey.getFieldAt(i);
				sql = sql.append(dbFieldValue(field.getFieldName()))
						.append(" = ");
				sql = sql.append(fieldValue(mRecord.get(field.getFieldName()),field.getLength(), tableEncoding, dataEncoding));
				
			}
			sql.append(";");
		}

		sqlList.add(sql.toString());
		return sqlList;
	}

	@Override
	public ArrayList<String> update(long time, String srcFileName, String targetFileName,
			String oldData, String newData, String tableEncoding, String dataEncoding) throws UnsupportedEncodingException {
		ArrayList<String> sqlList = new ArrayList<>();
		String schemaName = getSchemaName(srcFileName);
		String tableName = getTableName(srcFileName);
		MasicTable table = getMasicTable(schemaName, tableName);
		if (table == null) {
			return sqlList;
		}
		MasicKey primaryKey = table.getPrimaryKey();
		if (primaryKey == null) {
			sqlList.addAll(delete(time, srcFileName, targetFileName, oldData, tableEncoding, dataEncoding));
			sqlList.addAll(insert(time, srcFileName, targetFileName, newData, tableEncoding, dataEncoding));
		} else {

			String targetSchema = getSchemaName(targetFileName);
			String targetTable = getTableName(targetFileName);
			StringBuilder select = new StringBuilder();
			select = select.append("SELECT * FROM ").append(targetSchema).append(".")
                  .append(targetTable);
			StringBuilder sql = new StringBuilder();
			sql = sql.append("UPDATE ").append(targetSchema).append(".")
					.append(targetTable).append(" SET ");

			StringBuilder where = new StringBuilder();

			MasicRecord oRecord = MasicRecord.newInstance(table, oldData);
			MasicRecord mRecord = MasicRecord.newInstance(table, newData);
			String oldValue = null;
			String newValue = null;
			// MasicKey primaryKey = table.getPrimaryKey();
			int setCount = 0;

			for (int i = 0; i < table.getFieldCount(); i++) {

				MasicField field = table.getFieldAt(i);
				oldValue = oRecord.get(field.getFieldName());
				newValue = mRecord.get(field.getFieldName());
				if (StringUtils.equals(oldValue, newValue)) {
					continue;
				}

				if (setCount > 0) {
					sql = sql.append(" , ");
				}
				sql = sql.append("`").append(field.getFieldName())
						.append("` = ");
				sql = sql.append(fieldValue(mRecord.get(field.getFieldName()), field.getLength(),tableEncoding, dataEncoding));
				
				setCount++;
			}
			if (setCount > 0) {
				for (int i = 0; i < primaryKey.getSegmentCount(); i++) {
					if (i > 0) {
						where = where.append(" AND ");
					}
					MasicField field = primaryKey.getFieldAt(i);
					where = where.append(dbFieldValue(field.getFieldName()));		
					where = where.append("=");
					where = where.append(fieldValue(oRecord.get(field.getFieldName()), field.getLength(), tableEncoding, dataEncoding));					
				}
				sql = sql.append(" WHERE ").append(where).append(";");
				select = select.append(" WHERE ").append(where).append(";");
			//	sqlList.add(select.toString());
				sqlList.add(sql.toString());
			}
		}
		return sqlList;
	}
	private String dbFieldValue(String fieldName) {
	   return StringUtils.join("`", fieldName, "`");
	   /*
	   String result = StringUtils.join("CONVERT(`",fieldName, "` USING tis620)");
	   return result;
	   */
	}
	private String fieldValue(String value, int len, String tableEncoding, String dataEncoding) throws UnsupportedEncodingException {
		//String result= value.replaceAll("'", "\\\\'");
		String result =  value.replaceAll(",", " ");
		result = result.replaceAll("\\\\", "\\\\\\\\");
		result = result.replaceAll("'", "\\\\'");
		
		
		result = StringUtils.rightPad(result,len, ' ');
      /*
      if (RepConfig.getInstance().getReplicationType().equalsIgnoreCase("JAVA")) {
      	result = StringUtils.trim(result);
      } else {
         result = StringUtils.rightPad(result,len, ' ');
      }
      */
		//result = StringUtils.join("CONVER   T('",result,"' USING latin1)");
		String encodedResult = result;
		if (!tableEncoding.equalsIgnoreCase(dataEncoding)) {
		   if (dataEncoding.equalsIgnoreCase("tis620") ||  dataEncoding.equalsIgnoreCase("latin1")) {
		      encodedResult = new String(M.utos(result), "latin1");		      
		   }
		} else if (!tableEncoding.equalsIgnoreCase("utf8")) {
		      encodedResult = new String(M.utos(result), "latin1");
		}
		result = StringUtils.join("'", encodedResult, "'");
		
	
		
		return result;
	}
	@Override
	public ArrayList<String> createDB(String schemaName) {
		ArrayList<String> sqlList = new ArrayList<>();
		for (String name : Schema.getInstance().getSchemaNames()) {
			String sql = "CREATE DATABASE IF NOT EXISTS `" + name
					+ "` CHARACTER SET=utf8;";
			sqlList.add(sql);
		}
		return sqlList;
	}

	private String getTableName(String fileName) {
		return StringUtils.substringBefore(fileName, "@");
	}

	private String getSchemaName(String fileName) {
		return StringUtils.substringAfter(fileName, "@");
	}

	private MasicTable getMasicTable(String schemaName, String tableName) {
		MasicSchema schema = Schema.getInstance().getSchema(schemaName);
		if (schema == null) {
			return null;
		} else {
			return schema.getTableByName(tableName);
		}
	}

}
