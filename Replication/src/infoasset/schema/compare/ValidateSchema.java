package infoasset.schema.compare;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import infoasset.schema.*;

public class ValidateSchema {
	
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
	
	String destPathCSV = "";
	String filenameCSV = "";
	String path_to_csv = "";
	String title = "OPERATION|TABLE/FIELD/KEY|SCHEMA NAME|TABLE NAME|PATH TABLE|FIELD NAME|DATA TYPE|LENGTH|SCALE|COMMENT|PROPERTY KEY|KEY NAME";
	
	boolean isExportCSV = true;
	
	// Schema
	MasicSchema msSch_Prod = null;
	MasicSchema msSch_Dev = null;
			
	// Table 
	MasicTable msTb_Prod = null;
	MasicTable msTb_Dev = null;
			
	// Field
	MasicField msField_Prod = null;
	MasicField msField_Dev = null;
			
	// Data Type 
	MasicFieldType msFieldType_Prod = null;
	MasicFieldType msFieldType_Dev = null;
	
	// Field Length
	String msFieldLength_Prod = null;
	String msFieldLength_Dev = null;
	
	// Field Scale
	String msFieldScale_Prod = null;
	String msFieldScale_Dev = null;
	
	// Field Comment
	String msFieldComment_Prod = null;
	String msFieldComment_Dev = null;
			
	// Primary Key
	MasicKey msKey_Prod = null;
	MasicKey msKey_Dev = null;
	
	// Table Count
	int tableCount_Prod = 0;
	int tableCount_Dev = 0;
	
	// Field Count
	int fieldCount_Prod = 0;
	int fieldCount_Dev = 0;	
	
	// Key Count
	int keyCount_Prod = 0 ;
	int keyCount_Dev = 0 ;
	
	// Segment Count
	int segmentCount_Prod = 0;
	int segmentCount_Dev = 0;
	
	// Pattern Information
	String[] data;

	// Export CSV
	ArrayList<String> dataList = new ArrayList<String>();
	
	public ValidateSchema(String prod, String dev, boolean isCSV, String destPathCSV) throws Exception{
		// TODO Auto-generated constructor stub
		Path path_prod = Paths.get(prod);
		Path path_dev = Paths.get(dev);
		Path filename_prod = path_prod.getFileName();
		Path filename_dev = path_dev.getFileName();
		
		File file_prod = path_prod.toFile();
		File file_dev = path_dev.toFile();
		
		System.out.println("Path to schema production : " + path_prod);
		System.out.println("Path to schema develop : " + path_dev);
		
		while(true) {
			
			// Check File Exists 
			if (isFilesExists(path_prod)) 
			{
				System.out.println("File Exists : " + path_prod);
			}else {
				System.out.println("Not Found Schema Production : " + filename_prod);
				break;
			}
			
			if (isFilesExists(path_dev)) 
			{
				System.out.println("File Exists : " + path_dev);
			}else {
				System.out.println("Not Found Schema Develop : " + filename_dev);
				break;
			}
			
			// Check Extension .sch
			if (isExtensionSch(path_prod)) 
			{
				System.out.println("Schema Production is extension .sch : " + path_prod);
			}else {
				System.out.println("Not Extension .sch : " + filename_prod);
				break;
			}
			
			if (isExtensionSch(path_dev)) 
			{
				System.out.println("Schema Develop is extension .sch : " + path_dev);
			}else {
				System.out.println("Not Extension .sch : " + filename_dev);
				break;
			}
			
			if(isSameFileName(filename_prod, filename_dev)) {
				System.out.println("Schema same name ");
			}else {
				System.out.println("Schema name different");
				break;
			}
			
			System.out.println("-- Let's go to Check Change Structure --");
			checkChangeStructureDevelop(file_prod, file_dev);
			checkChangeStructureProduction(file_prod, file_dev);
			
			if(isCSV == true && destPathCSV != "") {
				getData();
				Path pathDest = Paths.get(destPathCSV);
				if(isDirectoryExists(pathDest)) {
					LocalDateTime now = LocalDateTime.now();
					filenameCSV = msSch_Dev.getSchemaName() + "_" + dtf.format(now) + ".csv";
					path_to_csv = destPathCSV + "/" + filenameCSV;
					if(writeFile()) {
						System.out.println("Write CSV File : " + path_to_csv); // เติมชื่อไฟล์
					}else {
						System.out.println("Unsuccessful Write CSV File");
					}
				}
			}else if(isCSV == true && destPathCSV == "") {
				System.out.println("-- Please specific destination CSV File --");
			}else {
				getData();
			}
			
			System.out.println("-- Finish Program --");
			break;
		}
	}
	
	void checkChangeStructureDevelop(File file_prod, File file_dev) throws Exception{
		msSch_Prod = SchemaCompiler.getSchema(file_prod);
		msSch_Dev = SchemaCompiler.getSchema(file_dev);
		
		tableCount_Dev = msSch_Dev.getTableCount();
		
		for(int t = 0 ; t < tableCount_Dev ; t++) {
			msTb_Dev = msSch_Dev.getTableAt(t);
			msTb_Prod = msSch_Prod.getTableByPath(msTb_Dev.getTablePath());
			
			fieldCount_Dev = msTb_Dev.getFieldCount();
			keyCount_Dev = msTb_Dev.getKeyCount();
			
			keyCount_Prod = msTb_Prod.getKeyCount();
			
			// ADD TABLE
			if(msTb_Prod == null) {
				data = new String[] {"ADD",
						msSch_Dev.getSchemaName(),
						msTb_Dev.getTableName(),
						msTb_Dev.getTablePath(),
						msTb_Dev.getComment()};
				
				dataList.add(textTablePattern(data));
				
				for(int f = 0 ; f < fieldCount_Dev ; f++) {
					msField_Dev = msTb_Dev.getFieldAt(f);
						
					msFieldType_Dev = msField_Dev.getType();
					msFieldLength_Dev = convertIntegerToString(msField_Dev.getLength());
					msFieldScale_Dev = convertIntegerToString(msField_Dev.getScale());
					msFieldComment_Dev = msField_Dev.getComment();
						
					// ADD FIELD
					if(msField_Dev != null) {
						data = new String[] {"ADD",
								msSch_Dev.getSchemaName(),
								msTb_Dev.getTableName(),
								msTb_Dev.getTablePath(),
								msField_Dev.getFieldName(),
								msFieldType_Dev.name(),
								msFieldLength_Dev,
								msFieldScale_Dev,
								msFieldComment_Dev};
					
						dataList.add(textFieldPattern(data));
					}
				}
				
				for(int k = 0 ; k < keyCount_Dev ; k++) {
					msKey_Dev = msTb_Dev.getKeyAt(k);
					segmentCount_Dev = msKey_Dev.getSegmentCount();
					
					String strFieldName = "";
					for(int s = 0 ; s < segmentCount_Dev ; s++) {
						if((s+1) == segmentCount_Dev) {
							strFieldName += msKey_Dev.getFieldAt(s).getFieldName();
						}else {
							strFieldName += msKey_Dev.getFieldAt(s).getFieldName() + ",";
						}
						
					}
					
					// ADD KEY
					data = new String[] {"ADD",
						msSch_Dev.getSchemaName(),
						msTb_Dev.getTableName(),
						msTb_Dev.getTablePath(),
						propertyKey(msKey_Dev.isDuplicate(), msKey_Dev.isModify()),
						strFieldName};
					
					dataList.add(textKeyPattern(data));
	
				}
			} 
			
			if(msTb_Prod != null) {
				// RENAME TABLE
				if(isSameTableName(msTb_Prod.getTableName(), msTb_Dev.getTableName()) == false) {
					data = new String[] {"RENAME",
							msSch_Dev.getSchemaName(),
							msTb_Dev.getTableName(),
							msTb_Dev.getTablePath(),
							msTb_Dev.getComment()};
					
					dataList.add(textTablePattern(data));
				}
				
				for(int f = 0 ; f < fieldCount_Dev ; f++) {
					msField_Dev = msTb_Dev.getFieldAt(f);
					msField_Prod = msTb_Prod.getField(msField_Dev.getFieldName());
						
					msFieldType_Dev = msField_Dev.getType();
					msFieldLength_Dev = convertIntegerToString(msField_Dev.getLength());
					msFieldScale_Dev = convertIntegerToString(msField_Dev.getScale());
					msFieldComment_Dev = msField_Dev.getComment();
						
					// ADD FIELD
					if(msField_Prod == null) {
						data = new String[] {"ADD",
								msSch_Dev.getSchemaName(),
								msTb_Dev.getTableName(),
								msTb_Dev.getTablePath(),
								msField_Dev.getFieldName(),
								msFieldType_Dev.name(),
								msFieldLength_Dev,
								msFieldScale_Dev,
								msFieldComment_Dev};
						
						dataList.add(textFieldPattern(data));
					}
						
					//ALTER FIELD
					if(msField_Prod != null) {
						
						msFieldType_Prod = msField_Prod.getType();
						msFieldLength_Prod = convertIntegerToString(msField_Prod.getLength());
						msFieldScale_Prod = convertIntegerToString(msField_Prod.getScale());
						msFieldComment_Prod = msField_Prod.getComment();
							
						if(isSameFieldType(msFieldType_Prod.name(), msFieldType_Dev.name()) == false || 
						   isSameFieldLength(msFieldLength_Prod, msFieldLength_Dev) == false || 
						   isSameFieldScale(msFieldScale_Prod, msFieldScale_Dev) == false) {
								
							data = new String[] {"ALTER",
									msSch_Dev.getSchemaName(),
									msTb_Dev.getTableName(),
									msTb_Dev.getTablePath(),
									msField_Dev.getFieldName(),
									msFieldType_Dev.name(),
									msFieldLength_Dev,
									msFieldScale_Dev,
									msFieldComment_Dev};
								
							dataList.add(textFieldPattern(data));
						}
					}
				}
				
				if(keyCount_Prod == keyCount_Dev) {
					for(int k = 0 ; k < keyCount_Dev ; k++) {
						msKey_Dev = msTb_Dev.getKeyAt(k);
						msKey_Prod = msTb_Prod.getKeyAt(k);
						
						segmentCount_Dev = msKey_Dev.getSegmentCount();
						segmentCount_Prod = msKey_Prod.getSegmentCount();
						
						if( (segmentCount_Prod == segmentCount_Dev) && 
								isSameKey(msKey_Prod.toString(), msKey_Dev.toString()) == false) {
							
							String strFieldName_Prod = "";
							for(int s = 0 ; s < segmentCount_Prod ; s++) {
								if((s+1) == segmentCount_Prod) {
									strFieldName_Prod += msKey_Prod.getFieldAt(s).getFieldName();
								}else {
									strFieldName_Prod += msKey_Prod.getFieldAt(s).getFieldName() + ",";
								}
								
							}
							
							// DROP KEY PROD
							data = new String[] {"DROP",
								msSch_Prod.getSchemaName(),
								msTb_Prod.getTableName(),
								msTb_Prod.getTablePath(),
								propertyKey(msKey_Prod.isDuplicate(), msKey_Prod.isModify()),
								strFieldName_Prod};
							
							dataList.add(textKeyPattern(data));
							
							
							String strFieldName_Dev = "";
							for(int s = 0 ; s < segmentCount_Dev ; s++) {
								if((s+1) == segmentCount_Dev) {
									strFieldName_Dev += msKey_Dev.getFieldAt(s).getFieldName();
								}else {
									strFieldName_Dev += msKey_Dev.getFieldAt(s).getFieldName() + ",";
								}
								
							}
							
							// ADD KEY DEV
							data = new String[] {"ADD",
								msSch_Dev.getSchemaName(),
								msTb_Dev.getTableName(),
								msTb_Dev.getTablePath(),
								propertyKey(msKey_Dev.isDuplicate(), msKey_Dev.isModify()),
								strFieldName_Dev};
							
							dataList.add(textKeyPattern(data));
							
						}else if( (segmentCount_Prod != segmentCount_Dev) && 
								isSameKey(msKey_Prod.toString(), msKey_Dev.toString()) == false) {
							
							String strFieldName_Prod = "";
							for(int s = 0 ; s < segmentCount_Prod ; s++) {
								if((s+1) == segmentCount_Prod) {
									strFieldName_Prod += msKey_Prod.getFieldAt(s).getFieldName();
								}else {
									strFieldName_Prod += msKey_Prod.getFieldAt(s).getFieldName() + ",";
								}
								
							}
							
							// DROP KEY PROD
							data = new String[] {"DROP",
								msSch_Prod.getSchemaName(),
								msTb_Prod.getTableName(),
								msTb_Prod.getTablePath(),
								propertyKey(msKey_Prod.isDuplicate(), msKey_Prod.isModify()),
								strFieldName_Prod};
							
							dataList.add(textKeyPattern(data));
							
							
							String strFieldName_Dev = "";
							for(int s = 0 ; s < segmentCount_Dev ; s++) {
								if((s+1) == segmentCount_Dev) {
									strFieldName_Dev += msKey_Dev.getFieldAt(s).getFieldName();
								}else {
									strFieldName_Dev += msKey_Dev.getFieldAt(s).getFieldName() + ",";
								}
								
							}
							
							// ADD KEY DEV
							data = new String[] {"ADD",
								msSch_Dev.getSchemaName(),
								msTb_Dev.getTableName(),
								msTb_Dev.getTablePath(),
								propertyKey(msKey_Dev.isDuplicate(), msKey_Dev.isModify()),
								strFieldName_Dev};
							
							dataList.add(textKeyPattern(data));
							
						}
						
						/*
						String strFieldName = "";
						for(int s = 0 ; s < segmentCount_Dev ; s++) {
							if((s+1) == segmentCount_Dev) {
								strFieldName += msKey_Dev.getFieldAt(s).getFieldName();
							}else {
								strFieldName += msKey_Dev.getFieldAt(s).getFieldName() + ",";
							}
							
						}
						
						// ADD KEY
						data = new String[] {"ADD",
							msSch_Dev.getSchemaName(),
							msTb_Dev.getTableName(),
							msTb_Dev.getTablePath(),
							propertyKey(msKey_Dev.isDuplicate(), msKey_Dev.isModify()),
							strFieldName};
						
						dataList.add(textKeyPattern(data));
						*/
	
					}
				}else if(keyCount_Prod != keyCount_Dev) {
					for(int k = 0 ; k < keyCount_Dev ; k++) {
						msKey_Dev = msTb_Dev.getKeyAt(k);
						segmentCount_Dev = msKey_Dev.getSegmentCount();
						
						String strFieldName = "";
						for(int s = 0 ; s < segmentCount_Dev ; s++) {
							if((s+1) == segmentCount_Dev) {
								strFieldName += msKey_Dev.getFieldAt(s).getFieldName();
							}else {
								strFieldName += msKey_Dev.getFieldAt(s).getFieldName() + ",";
							}
							
						}
						
						// ADD KEY
						data = new String[] {"ADD",
							msSch_Dev.getSchemaName(),
							msTb_Dev.getTableName(),
							msTb_Dev.getTablePath(),
							propertyKey(msKey_Dev.isDuplicate(), msKey_Dev.isModify()),
							strFieldName};
						
						dataList.add(textKeyPattern(data));
	
					}
				}
			}
		}
	}

	void checkChangeStructureProduction(File file_prod, File file_dev) throws Exception{
		msSch_Prod = SchemaCompiler.getSchema(file_prod);
		msSch_Dev = SchemaCompiler.getSchema(file_dev);
		
		tableCount_Prod = msSch_Prod.getTableCount();
		
		for(int t = 0 ; t < tableCount_Prod ; t++) {
			msTb_Prod = msSch_Prod.getTableAt(t);
			msTb_Dev = msSch_Dev.getTableByPath(msTb_Prod.getTablePath());
			
			fieldCount_Prod = msTb_Prod.getFieldCount();
			keyCount_Prod = msTb_Prod.getKeyCount();
			
			// DROP TABLE
			if(msTb_Dev == null) {
				data = new String[] {"DROP",
						msSch_Prod.getSchemaName(),
						msTb_Prod.getTableName(),
						msTb_Prod.getTablePath(),
						msTb_Prod.getComment()};
				
				dataList.add(textTablePattern(data));
				
				for(int f = 0 ; f < fieldCount_Prod ; f++) {
					msField_Prod = msTb_Prod.getFieldAt(f);
						
					msFieldType_Prod = msField_Prod.getType();
					msFieldLength_Prod = convertIntegerToString(msField_Prod.getLength());
					msFieldScale_Prod = convertIntegerToString(msField_Prod.getScale());
					msFieldComment_Prod = msField_Prod.getComment();
						
					// DROP FIELD
					data = new String[] {"DROP",
						msSch_Prod.getSchemaName(),
						msTb_Prod.getTableName(),
						msTb_Prod.getTablePath(),
						msField_Prod.getFieldName(),
						msFieldType_Prod.name(),
						msFieldLength_Prod,
						msFieldScale_Prod,
						msFieldComment_Prod};
					
					dataList.add(textFieldPattern(data));
					
				}
				
				for(int k = 0 ; k < keyCount_Prod ; k++) {
					msKey_Prod = msTb_Prod.getKeyAt(k);
					segmentCount_Prod = msKey_Prod.getSegmentCount();
					
					String strFieldName = "";
					for(int s = 0 ; s < segmentCount_Prod ; s++) {
						if((s+1) == segmentCount_Prod) {
							strFieldName += msKey_Prod.getFieldAt(s).getFieldName();
						}else {
							strFieldName += msKey_Prod.getFieldAt(s).getFieldName() + ",";
						}
						
					}
					
					// DROP KEY
					data = new String[] {"DROP",
						msSch_Prod.getSchemaName(),
						msTb_Prod.getTableName(),
						msTb_Prod.getTablePath(),
						propertyKey(msKey_Prod.isDuplicate(), msKey_Prod.isModify()),
						strFieldName};
					
					dataList.add(textKeyPattern(data));

				}
			}
			
			if(msTb_Dev != null) {
				for(int f = 0 ; f < fieldCount_Prod ; f++) {
					msField_Prod = msTb_Prod.getFieldAt(f);
					msField_Dev = msTb_Dev.getField(msField_Prod.getFieldName());
						
					msFieldType_Prod = msField_Prod.getType();
					msFieldLength_Prod = convertIntegerToString(msField_Prod.getLength());
					msFieldScale_Prod = convertIntegerToString(msField_Prod.getScale());
					msFieldComment_Prod = msField_Prod.getComment();
						
					// DROP FIELD
					if(msField_Dev == null) {
						data = new String[] {"DROP",
							msSch_Prod.getSchemaName(),
							msTb_Prod.getTableName(),
							msTb_Prod.getTablePath(),
							msField_Prod.getFieldName(),
							msFieldType_Prod.name(),
							msFieldLength_Prod,
							msFieldScale_Prod,
							msFieldComment_Prod};
						dataList.add(textFieldPattern(data));
					}
				}
			}
		}
	}
	
	String textTablePattern(String[] data) {
		
		String pattern = "";
		String operType = "";
		String schemaName = "";
		String tableName = "";
		String tablePath = "";
		String comment = "";
		
		operType = data[0];
		schemaName = data[1];
		tableName = data[2];
		tablePath = data[3];
		comment = data[4];
		
		// Pattern : ADD/DROP/RENAME | TABLE | SCHEMA_NAME | TABLE_NAME | PATH_TABLE|  |  |  |  | COMMENT
		if(operType.equals("ADD")) {
			pattern = "ADD" + "|TABLE|" + schemaName + "|" + tableName + "|" + tablePath + "|" + " " + "|" + 
					" " + "|" + " " + "|" + " " + "|" + comment + "|" + " " + "|" + " ";
		}else if(operType.equals("DROP")) {
			pattern = "DROP" + "|TABLE|" + schemaName + "|" + tableName + "|" + tablePath + "|" + " " + "|" + 
					" " + "|" + " " + "|" + " " + "|" + comment + "|" + " " + "|" + " ";
		}else if(operType.equals("RENAME")) {
			pattern = "RENAME" + "|TABLE|" + schemaName + "|" + tableName + "|" + tablePath + "|" + " " + "|" + 
					" " + "|" + " " + "|" + " " + "|" + comment + "|" + " " + "|" + " ";
		}
		
		return pattern;
	}
	
	String textFieldPattern(String[] data) {
		
		String pattern = "";
		String operType = "";
		String schemaName = "";
		String tableName = "";
		String tablePath = "";
		String fieldName = "";
		String dataType = "";
		String length = "";
		String scale = "";
		String comment = "";
		
		operType = data[0].trim();
		schemaName = data[1].trim();
		tableName = data[2].trim();
		tablePath = data[3].trim();
		fieldName = data[4].trim();
		dataType = data[5].trim();
		length = data[6].trim();
		scale = data[7].trim();
		comment = data[8].trim();

		// Pattern : ADD/ALTER/DROP | FIELD | SCHEMA_NAME | TABLE_NAME | PATH_TABLE| FIELD_NAME | DATA_TYPE | LENGTH | SCALE | COMMENT |  | 
		if(operType.equals("ADD")) {
			
			pattern = "ADD" + "|FIELD|" + schemaName + "|" + tableName + "|" + tablePath + "|" + fieldName + "|" + 
					dataType + "|" + length + "|" + scale + "|" + comment + "|" + " " + "|" + " ";
		}else if(operType.equals("ALTER")){
			
			pattern = "ALTER" + "|FIELD|" + schemaName + "|" + tableName + "|"+ tablePath + "|" + fieldName + "|" + 
					dataType + "|" + length + "|" + scale + "|" + comment + "|" + " " + "|" + " ";
		}else if(operType.equals("DROP")){
			
			pattern = "DROP" + "|FIELD|" + schemaName + "|" + tableName + "|"+ tablePath + "|" + fieldName + "|" + 
					dataType + "|" + length + "|" + scale + "|" + comment + "|" + " " + "|" + " ";
		}
			
		return pattern;
	}
	
	String textKeyPattern(String[] data) {
		
		String pattern = "";
		String operType = "";
		String schemaName = "";
		String tableName = "";
		String tablePath = "";
		String propertyKey = "";
		String keyName = "";
		
		operType = data[0];
		schemaName = data[1];
		tableName = data[2];
		tablePath = data[3];
		propertyKey = data[4];
		keyName = data[5];
		
		// Pattern : ADD/ALTER/DROP | KEY | SCHEMA_NAME | TABLE_NAME | PATH_TABLE|  |  |  |  |  | PROPERTY KEY | KEY NAME
		if(operType.equals("ADD")) {
			
			pattern = "ADD" + "|KEY|" + schemaName + "|" + tableName + "|" + tablePath + "|" + " " + "|" + 
					" " + "|" + " " + "|" + " " + "|" + " " + "|" + propertyKey + "|" + keyName;
		}else if(operType.equals("ALTER")){
			
			pattern = "ALTER" + "|KEY|" + schemaName + "|" + tableName + "|"+ tablePath + "|" + " " + "|" + 
					" " + "|" + " " + "|" + " " + "|" + " " + "|" + propertyKey + "|" + keyName;
		}else if(operType.equals("DROP")){
			
			pattern = "DROP" + "|KEY|" + schemaName + "|" + tableName + "|"+ tablePath + "|" + " " + "|" + 
					" " + "|" + " " + "|" + " " + "|" + " " + "|" + propertyKey + "|" + keyName;
		}
		
		return pattern;
	}
	
	boolean writeFile() throws Exception{
		boolean isWrite = false;
		File file = new File(path_to_csv); 
		FileWriter write = new FileWriter(file, false);
		
		write.write(title + "\n");
		for(String data : dataList) {
			write.write(data + "\n");
		}
		write.close();
		
		isWrite = isFilesExists(Paths.get(path_to_csv));
		
		return isWrite;
	}
	
	void getData() {
		int dataCount = 0;
		dataCount = dataList.size();
			if(dataCount > 0) {
				System.out.println(title);
				for(String data : dataList) {
					System.out.println(data);
				}
			}else {
				System.out.println("-- Not Change Structure Schema --");
			}
	}
	
	String propertyKey(boolean isDuplicate, boolean isModify) {
		String propertyKey = "";
		
		if(isDuplicate == true && isModify == true) {
			propertyKey = "DUP, MOD";
		}else if(isDuplicate == true && isModify == false) {
			propertyKey = "DUP";
		}else if(isDuplicate == false && isModify == true) {
			propertyKey = "MOD";
		}else if(isDuplicate == false && isModify == false) {
			propertyKey = "MOD";
		}
		
		return propertyKey;
	}
	
	String convertIntegerToString(int value) {
		return Integer.toString(value);
	}
	
	boolean isSameKey(String key_Prod, String key_Dev) {
		boolean isSameKey = false;
		isSameKey = key_Prod.equals(key_Dev);
		return isSameKey;
	}
	
	boolean isSameFieldType(String fieldType_Prod, String fieldType_Dev) {
		boolean isSameFieldType = false;
		isSameFieldType = fieldType_Prod.equals(fieldType_Dev);
		return isSameFieldType;
	}
	
	boolean isSameFieldLength(String fieldLength_Prod, String fieldLength_Dev) {
		boolean isSameFieldLength = false;
		isSameFieldLength = fieldLength_Prod.equals(fieldLength_Dev);
		return isSameFieldLength;
	}
	
	boolean isSameFieldScale(String fieldScale_Prod, String fieldScale_Dev) {
		boolean isSameFieldScale = false;
		isSameFieldScale = fieldScale_Prod.equals(fieldScale_Dev);
		return isSameFieldScale;
	}
	
	boolean isSameFieldName(String fieldName_Prod, String fieldName_Dev) {
		boolean isSameFieldName = false;
		isSameFieldName = fieldName_Prod.equals(fieldName_Dev);
		return isSameFieldName;
	}
	
	boolean isSameTableName(String tableName_Prod, String tableName_Dev) {
		boolean isSameTableName = false;
		isSameTableName = tableName_Prod.equals(tableName_Dev);
		return isSameTableName;
	}
	
	boolean isSameFileName(Path filename_prod, Path filename_dev) {
		boolean isSameFileName = false;
		isSameFileName = filename_prod.equals(filename_dev);
		return isSameFileName;
	}
	
	boolean isFilesExists(Path path) {
		boolean pathFileExists = false;
		pathFileExists = Files.exists(path, new LinkOption[] {LinkOption.NOFOLLOW_LINKS});
		return pathFileExists;
	}
	
	boolean isDirectoryExists(Path path) {
		boolean pathDirectoryExists = false;
		pathDirectoryExists = Files.exists(path, new LinkOption[] {LinkOption.NOFOLLOW_LINKS});
		return pathDirectoryExists;
	}
	
	boolean isExtensionSch(Path path) {
		boolean pathSch = false;
		pathSch = path.toString().endsWith(".sch");
		return pathSch;
	}
}
