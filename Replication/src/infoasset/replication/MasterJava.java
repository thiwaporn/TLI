package infoasset.replication;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import infoasset.replication.special.SpecialTable;
import infoasset.schema.MasicField;
import infoasset.schema.MasicFieldType;
import infoasset.schema.MasicKey;
import infoasset.schema.MasicRecord;
import infoasset.schema.MasicTable;
import infoasset.schema.MasicTableType;
import manit.M;



public class MasterJava implements Master {
	private static MasterJava INSTANCE = null;

	public static MasterJava getInstance() {
		return INSTANCE;
	}

	public static void newInstance(NodeList masterList) throws RepException {
		if (INSTANCE != null) {
			return;
		}
		INSTANCE = new MasterJava();
		INSTANCE.startServer();
		for (int i = 0; i < masterList.getLength(); i++) {
			Element masterElem = (Element) masterList.item(i);
			String databaseName = masterElem.getAttribute("database");
			String tableName = masterElem.getAttribute("table");
			System.out.println(databaseName + " " + tableName);

			MasicTable table = INSTANCE.openTable(masterElem, i, databaseName,
					tableName);
			String specialClass = masterElem.getAttribute("special");
			String idClass = masterElem.getAttribute("idClass");
			String descClass = masterElem.getAttribute("descClass");
			String textFile = masterElem.getAttribute("textfile");
			String separator = masterElem.getAttribute("separator");
			NodeList fieldList = masterElem.getElementsByTagName("field");
			try {
				String[][] data = null;
				if (!StringUtils.isEmpty(textFile)) {
					data = INSTANCE.getData(fieldList, textFile, separator);
				} else if (!StringUtils.isEmpty(specialClass)) {
					data = INSTANCE.getData(specialClass);
				} else if (!StringUtils.isEmpty(idClass)) {
					data = INSTANCE.getData(idClass, descClass);
				} else {
					data = INSTANCE.getData(fieldList);
				}
				if (data != null) {
					INSTANCE.insertData(masterElem, i, table, data);
				}
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | NoSuchFieldException
					| SecurityException | IOException e) {

				System.out.println(e.getMessage());
				throw new RepException(e);
			}

		}
		INSTANCE.stopServer();
	}

	private ArrayList<Transaction> transList;
	private int nextIndex = 0;

	private MasterJava() {
		transList = new ArrayList<>();

	}

	@Override
	public Transaction nextTransaction() throws IOException, RepException {
		if (nextIndex >= transList.size()) {
			return null;
		}
		return transList.get(nextIndex++);
	}

	@Override
	public String getMasterName() {
		return "Java";
	}

	private void startServer() {
		Transaction trans = Transaction.newInstance(TransactionType.START);
		trans.setDataAsString("IPAddress");
		transList.add(trans);
	}

	private void stopServer() {
		Transaction trans = Transaction.newInstance(TransactionType.SHUTDOWN);
		trans.setDataAsString("IPAddress");
		transList.add(trans);
	}

	private void insertData(Element masterElem, int fileId, MasicTable table,
			String[][] data) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			NoSuchFieldException, SecurityException, RepException {
		NodeList fldList = masterElem.getElementsByTagName("field");
		String[] fieldName = getFieldName(fldList);

		MasicRecord record;
		for (int i = 0; i < data[0].length; i++) {
			record = MasicRecord.newInstance(table);
			for (int f = 0; f < fieldName.length; f++) {
				record.set(fieldName[f], String.valueOf(data[f][i]));
			}
			Transaction trans = Transaction.newInstance(TransactionType.INSERT);
			byte[] transRec = record.get();
			trans.setFileId(fileId);
			trans.setDataLength(transRec.length);
			trans.setData(transRec);
			transList.add(trans);
		}
	}

	private String[] getFieldName(NodeList fldList) {
		String[] fieldName = new String[fldList.getLength()];
		for (int f = 0; f < fldList.getLength(); f++) {
			Element fieldElem = (Element) fldList.item(f);
			fieldName[f] = fieldElem.getAttribute("name");
		}
		return fieldName;
	}

	private String[][] getData(String idClass, String descClass)
			throws ClassNotFoundException, IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException, SecurityException {

		String[][] data = new String[2][];
		data[0] = ArrayUtils.EMPTY_STRING_ARRAY;
		data[1] = ArrayUtils.EMPTY_STRING_ARRAY;
		Class<?> idClazz = Class.forName(idClass);
		Class<?> descClazz = Class.forName(descClass);
		Field[] fldList = idClazz.getDeclaredFields();
		String idValue = null;
		String descValue = null;
		Field fldDesc;
		for (Field fld : fldList) {
			if (isAccessibleField(fld)) {
				idValue = String.valueOf(fld.get(null));
				fldDesc = descClazz.getField(fld.getName());
				if (isAccessibleField(fldDesc)) {
					descValue = String.valueOf(fldDesc.get(null));
					data[0] = ArrayUtils.add(data[0], idValue);
					data[1] = ArrayUtils.add(data[1], descValue);
				}
			}
		}

		return data;
	}

	private boolean isAccessibleField(Field fld) {
		int mod = fld.getModifiers();
		if (Modifier.isPublic(mod) && Modifier.isStatic(mod)
				&& String.class.isAssignableFrom(fld.getType())) {
			return true;
		} else {
			return false;
		}
	}

	private String[][] getData(String specialClass)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		Class<?> clazz = ClassUtils.getClass(specialClass);
		if (infoasset.replication.special.SpecialTable.class
				.isAssignableFrom(clazz)) {
			SpecialTable sp = (SpecialTable) clazz.newInstance();
			return sp.getData();

		} else {
			return null;
		}

	}

	private String[][] getData(NodeList fldList) throws ClassNotFoundException,
			NoSuchFieldException, SecurityException, RepException,
			IllegalArgumentException, IllegalAccessException {
		String[][] data = new String[fldList.getLength()][];
		
		for (int i = 0; i < fldList.getLength(); i++) {
			Element fieldElem = (Element) fldList.item(i);
			String className = fieldElem.getAttribute("class");
			String varName = fieldElem.getAttribute("variable");
			String index = fieldElem.getAttribute("index");
			if (StringUtils.isEmpty(index)) {
				index = "0";
			}				
			Object[] object = (Object[]) Class.forName(className).getDeclaredField(varName).get(null);
			for (int j = 0; j < object.length; j++) {
				data[i] = getDataItem(data[i], object[j], Integer.valueOf(index) );
			}
			System.out.println(Arrays.toString(data[i]));
			String charset = fieldElem.getAttribute("charset");
			String start = fieldElem.getAttribute("start");
			String end = fieldElem.getAttribute("end");
			for (int j = 0; j < data[i].length; j++) {
				if (charset.equals("s")) {
					data[i][j] = M.stou(data[i][j].toString());
				}
				if (!StringUtils.isEmpty(start)) {
					if (StringUtils.isEmpty(end)) {
						data[i][j] = data[i][j].substring(Integer
								.valueOf(start));
					} else {
						data[i][j] = data[i][j].substring(
								Integer.valueOf(start), Integer.valueOf(end));
					}
				}
			}
		}
		
		return data;

	}

	private String[] getDataItem(String[] data, Object obj, int idx) {
		if (obj != null) {
			if (obj.getClass().isArray()) {
				Object[] array = (Object[]) obj;
				if (obj.getClass().getComponentType().isArray()) {					
					for (int i = 0; i < array.length; i++) {
						data = getDataItem(data, array[i], idx);
					}
				} else {					
					data = ArrayUtils.add(data, String.valueOf(array[idx]));					
				}
			} else {
				data = ArrayUtils.add(data, String.valueOf(obj));
			}
		}
		return data;
	}

	private String[][] getData(NodeList fieldList, String textFile,
			String separator) throws IOException {
		File file = new File(textFile);
		if (!file.exists()) {
			return null;
		}
		String[][] data = new String[fieldList.getLength()][];
		int[] start = new int[fieldList.getLength()];
		int[] end   = new int[fieldList.getLength()];
		String[] charset = new String[fieldList.getLength()];
		for (int i = 0; i < fieldList.getLength(); i++) {
			data[i] = ArrayUtils.EMPTY_STRING_ARRAY;
			start[i] = -1;
			end[i] = -1;
			charset[i] = "u";
			Element fldElem = (Element)fieldList.item(i);
			if (! StringUtils.isEmpty(fldElem.getAttribute("start"))) {
				start[i] = Integer.valueOf(fldElem.getAttribute("start"));
			}
			if (! StringUtils.isEmpty(fldElem.getAttribute("end"))) {
				end[i] = Integer.valueOf(fldElem.getAttribute("end"));
			}
			if (! StringUtils.isEmpty(fldElem.getAttribute("charset"))) {
				charset[i] = fldElem.getAttribute("charset");				
			} 
		}
		try (RandomAccessFile rdf = new RandomAccessFile(file, "r")) {
		String line = rdf.readLine();
		for (; line != null; line = rdf.readLine()) {
			String[] rec = null;
			if (StringUtils.isEmpty(separator)) {
				rec = new String[fieldList.getLength()];
				for (int i = 0; i < rec.length; i++) {
					if (end[i] > 0) {
						rec[i] = line.substring(start[i], end[i]);	
					} else {
						rec[i] = line.substring(start[i]);
					}					
				}
			} else {
				rec = line.split(separator);
			}
			for (int i = 0; i < fieldList.getLength(); i++) {
				if (charset[i].equals("s")) {
					data[i] = ArrayUtils.add(data[i], M.stou(rec[i]));
				} else {
					data[i] = ArrayUtils.add(data[i], rec[i]);	
				}
				
			}
		}
		}
		return data;
	}

	private MasicTable openTable(Element masterElem, int fileId,
			String schemaName, String tableName) {
		String primaryKey = masterElem.getAttribute("primaryKey");
		MasicTable table = MasicTable.newInstance(tableName, "/",
				MasicTableType.ISAM);
		NodeList fldList = masterElem.getElementsByTagName("field");
		ArrayList<MasicField> keyField = new ArrayList<>();
		for (int f = 0; f < fldList.getLength(); f++) {
			Element fieldElem = (Element) fldList.item(f);
			String fieldName = fieldElem.getAttribute("name");
			String fieldType = fieldElem.getAttribute("type");
			String fieldLength = fieldElem.getAttribute("length");
			String fieldScale = fieldElem.getAttribute("scale");
			if (fieldScale == null
					|| StringUtils.isWhitespace(fieldScale)) {
				fieldScale = "0";
			}
			MasicField field = MasicField.newInstance(fieldName,
					MasicFieldType.matchScript(fieldType.toUpperCase()),
					Integer.valueOf(fieldLength), Integer.valueOf(fieldScale));
			table.addField(field);
			if (primaryKey != null) {
				if (ArrayUtils.contains(primaryKey.split(","), fieldName)) {
					keyField.add(field);
				}
			}
		}
		if (primaryKey != null) {
			MasicKey key = MasicKey.newInstance(false, false);
			key.setFieldList(keyField);
			table.addKey(key);
		}
		Schema.getInstance().getSchema(schemaName).addTable(table);
		Transaction trans = Transaction.newInstance(TransactionType.OPEN);
		trans.setFileId(fileId);
		trans.setDataAsString(tableName + "@" + schemaName);
		transList.add(trans);
		return table;
	}

	

}
