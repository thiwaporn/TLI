package infoasset.schema.compare;

import java.io.File;
import java.nio.file.*;

import infoasset.schema.*;

public class ValidateSchema {
	
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
	
	// Pattern Information
	String[] data;

	public ValidateSchema(String prod, String dev) throws Exception{
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
			
			System.out.println("Go to Check Change Structure");
			checkChangeStructureProduction(file_prod, file_dev);
			checkChangeStructureDevelop(file_prod, file_dev);
			
			System.out.println("-- Finish Program --");
			break;
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
			
			// DROP TABLE
			if(msTb_Dev == null) System.out.println("DROP TABLE | " + msTb_Prod.getTableName());
			
			// Test Add Drop Field
			if(msTb_Prod.getTableName().equals("beneficiary####")) {
				System.out.println("Yes Production : beneficiary####");
				
				for(int f = 0 ; f < fieldCount_Prod ; f++) {
					msField_Prod = msTb_Prod.getFieldAt(f);
					msField_Dev = msTb_Dev.getField(msField_Prod.getFieldName());
					
					// DROP FIELD
					if(msField_Dev == null) System.out.println("DROP Field | " + msField_Prod.getFieldName());
					
				}
			}
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
			
			// ADD TABLE
			if(msTb_Prod == null) System.out.println("ADD TABLE | " + msTb_Dev.getTableName());
			
			// Test Add Drop Field
			if(msTb_Dev.getTableName().equals("beneficiary####")) {
				System.out.println("Yes Develop : beneficiary####");
				
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
								 msField_Dev.getFieldName(),
								 msFieldType_Dev.name(),
								 msFieldLength_Dev,
								 msFieldScale_Dev,
								 msFieldComment_Dev};
						System.out.println(textFieldPattern(data));
					}
				}
			}
		}
	}
	
	String textFieldPattern(String[] data) {
		
		String pattern = "";
		String operType = "";
		String schemaName = "";
		String tableName = "";
		String fieldName = "";
		String dataType = "";
		String length = "";
		String scale = "";
		String comment = "";
		
		operType = data[0].trim();
		schemaName = data[1].trim();
		tableName = data[2].trim();
		fieldName = data[3].trim();
		dataType = data[4].trim();
		length = data[5].trim();
		scale = data[6].trim();
		comment = data[7].trim();

		// Pattern : ADD/ALTER FIELD | SCHEMA_NAME | TABLE_NAME | FIELD_NAME | DATA_TYPE | LENGTH | SCALE | COMMENT
		if(operType.equals("ADD")) {
			
			pattern = "ADD FIELD|" + schemaName + "|" + tableName + "|" + fieldName + "|" + 
					dataType + "|" + length + "|" + scale + "|" + comment;
			System.out.println(pattern);
			
		}else if(operType.equals("ALTER")){
			
			pattern = "ALTER FIELD|" + schemaName + "|" + tableName + "|" + fieldName + "|" + 
					dataType + "|" + length + "|" + scale + "|" + comment;
			System.out.println(pattern);
			
		}else {
			pattern = "";
		}
			
		return pattern;
	}
	
	String convertIntegerToString(int value) {
		return Integer.toString(value);
	}
	
	
	
	boolean isSameFileName(Path filename_prod, Path filename_dev) {
		boolean isSameFileName = false;
		isSameFileName = filename_prod.equals(filename_dev);
		return isSameFileName;
	}
	
	boolean isFilesExists(Path path) {
		boolean pathExists = false;
		pathExists = Files.exists(path, new LinkOption[] {LinkOption.NOFOLLOW_LINKS});
		return pathExists;
	}
	
	boolean isExtensionSch(Path path) {
		boolean pathSch = false;
		pathSch = path.toString().endsWith(".sch");
		return pathSch;
	}
	
}
