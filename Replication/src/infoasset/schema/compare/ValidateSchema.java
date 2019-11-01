package infoasset.schema.compare;

import java.io.File;
import java.nio.file.*;

import infoasset.schema.MasicSchema;
import infoasset.schema.SchemaCompiler;

public class ValidateSchema {

	public ValidateSchema(String[] prod, String[] dev) throws Exception{
		// TODO Auto-generated constructor stub
		Path path = null;
		Path path_prod = null;
		Path path_dev = null;
		Path filename_prod = null;
		Path filename_dev = null;
		
		File file_prod = null;
		File file_dev = null;
		
		int amt_prod = prod.length;
		int amt_dev = dev.length;
		
		System.out.println("Amount schema prod : " + amt_prod);
		System.out.println("Amount schema dev : " + amt_dev);
		
		for(String str : prod) {
			path = Paths.get(str);
			
			System.out.println("isFilesExists : Prod");
			if(isFilesExists(path) == false) {
				System.out.println("Not Found : " + str);
			}
			
			System.out.println("isExtensionSch : Prod");
			if(isExtensionSch(path) == false) {
				System.out.println("Not Schema :" + str);
			}
			
		}
		
		for(String str : dev) { 
			path = Paths.get(str);
			
			System.out.println("isFilesExists : Dev");
			if(isFilesExists(path) == false) {
				System.out.println("Not Found : " + str);
			}
			
			System.out.println("isExtensionSch : Dev");
			if(isExtensionSch(path) == false) {
				System.out.println("Not Schema :" + str);
			}
		}
		
		for(String str_prod : prod) {
			for(String str_dev : dev) {
				path_prod = Paths.get(str_prod);
				path_dev = Paths.get(str_dev);
				filename_prod = path_prod.getFileName();
				filename_dev = path_dev.getFileName();
				
				file_prod = path_prod.toFile();
				file_dev = path_dev.toFile();
				
				if(filename_prod.equals(filename_dev)) 
					checkChangeStructure(file_prod, file_dev);
			}
		}
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
	
	void checkChangeStructure(File file_prod, File file_dev) throws Exception{
		MasicSchema msSch_Prod = SchemaCompiler.getSchema(file_prod);
		MasicSchema msSch_Dev = SchemaCompiler.getSchema(file_dev);
		
		int tableCount_Prod = msSch_Prod.getTableCount();
		int tableCount_Dev = msSch_Dev.getTableCount();
		
		
	}
	
}