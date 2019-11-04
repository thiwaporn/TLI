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
			
	// Primary Key
	MasicKey msKey_Prod = null;
	MasicKey msKey_Dev = null;
	
	// Table Count
	int tableCount_Prod = 0;
	int tableCount_Dev = 0;

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
		
		for(int i = 0 ; i < tableCount_Prod ; i++) {
			msTb_Prod = msSch_Prod.getTableAt(i);
			msTb_Dev = msSch_Dev.getTableByPath(msTb_Prod.getTablePath());
			
		}
	}
	
	void checkChangeStructureDevelop(File file_prod, File file_dev) throws Exception{
		msSch_Prod = SchemaCompiler.getSchema(file_prod);
		msSch_Dev = SchemaCompiler.getSchema(file_dev);
		
		tableCount_Dev = msSch_Dev.getTableCount();
		
		for(int i = 0 ; i < tableCount_Dev ; i++) {
			
		}
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
