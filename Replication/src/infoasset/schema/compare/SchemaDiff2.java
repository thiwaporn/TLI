package infoasset.schema.compare;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import infoasset.report.ReportInfo;
import infoasset.report.ReportType;
import infoasset.schema.MasicField;
import infoasset.schema.MasicFieldType;
import infoasset.schema.MasicKey;
import infoasset.schema.MasicSchema;
import infoasset.schema.MasicTable;
import infoasset.schema.SchemaCompiler;
import utility.database.SecuredConnect;
import utility.mail.PostMan;
import utility.mail.TLMail;

public class SchemaDiff2 {

	String strScript="";
	String isHave = "n";
	
	ArrayList<String> sqlScript = null;
	ArrayList<String> serverName = null;
	ArrayList<Server> arrList = new ArrayList<Server>(); 
	ArrayList<Database> arrCompare = new ArrayList<Database>();
	
	static Connection conn;
	static Statement st = null;
	
	public static void main(String[] args) throws Exception {
		String option = args[0];
		new SchemaDiff2(option);
	}
	
	public SchemaDiff2(){
		
	}
	
	public SchemaDiff2(String option) throws Exception{
		if(option.equals("M")||option.equals("m")){
			System.out.println("option sent mail");
			String newSch = "", oldSch = ""; 
			
			//-- changed name folder : new_schema --> schema_changed by view 12/09/2019
			//-- changed name folder : schema --> schema_original by view 12/09/2019
			newSch = "/home/mysql/replication/compare_schema/schema_changed/";
			oldSch = "/home/mysql/replication/compare_schema/schema_original/";
			
			sentMail(newSch,oldSch);
			//getSentMail();
		}else if(option.equals("C")||option.equals("c")){
			System.out.println("option c");
			String currSch = "/home/mysql/replication/current_schema/";
			String preSch = "/home/mysql/replication/previous_schema/";
			compareSch(currSch, preSch);
			getSentSch();
		}else if(option.equals("S")||option.equals("s")){
			System.out.println("option create sql script");
			createSQL();
		}else{
			System.out.println("Option Incorrect");
		}
	}
	
	private void compareSch(String currSch , String preSch) throws Exception{
		File pathCurrSche = new File(currSch);
		File pathPreSch = new File(preSch);
		
		File[] listFileCurr = pathCurrSche.listFiles();
		File[] listFilePre = pathPreSch.listFiles();
		
		int no_database=0 ;
		
		for(File newDBName : listFileCurr){
			for(File oldDBName : listFilePre){
				if(newDBName.getName().endsWith(".sch")&&oldDBName.getName().endsWith(".sch")){
				if(newDBName.getName().equals(oldDBName.getName())){
					/*================== START ================*/
					MasicSchema oldSchema = SchemaCompiler.getSchema(oldDBName.getCanonicalFile());
					MasicSchema newSchema = SchemaCompiler.getSchema(newDBName.getCanonicalFile());
					//arrList.get(no_server).addDatabase(new Database(oldSchema.getSchemaName()));
					System.out.println("Database ---> "+oldSchema.getSchemaName());
					arrCompare.add(new Database(oldSchema.getSchemaName()));
					
					MasicTable oldTable = null;
					MasicTable newTable = null;
					
					MasicField oldField = null;
					MasicField newField = null;
					
					MasicFieldType oldFieldType = null;
					MasicFieldType newFieldType = null;
					
					MasicKey oldPK = null;
					MasicKey newPK = null;
					

					String strKey; 
					
					for(int i = 0 ; i < oldSchema.getTableCount() ; i++){
						oldTable = oldSchema.getTableAt(i);
						newTable = newSchema.getTableByName(oldTable.getTableName()); //COMPARE TABLE NEW & OLD
						
						if(newTable == null){ //DROP TABLE
							//arrList.get(no_server).getDatabase(no_database).addTable(new Table(oldTable.getTableName(),"DROP"));
							arrCompare.get(no_database).addTable(new Table(oldTable.getTableName(),"DROP"));
						}
						else{ // ALTER TABLE
							//arrList.get(no_server).getDatabase(no_database).addTable(new Table(oldTable.getTableName()));
							arrCompare.get(no_database).addTable(new Table(oldTable.getTableName()));
							
							for(int x = 0 ; x < oldTable.getFieldCount() ; x++){ // OLD TABLE
								oldField = oldTable.getFieldAt(x);
								newField = newTable.getField(oldField.getFieldName());
								if(newField == null){ //DROP COLUMN
									if(oldFieldType.getCode() == "TEXT"){
										//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("DROP", oldField.getFieldName(), "CHAR", oldField.getLength()));
										arrCompare.get(no_database).getTable(i).addColumn(new Column("DROP", oldField.getFieldName(), "CHAR", oldField.getLength()));
									}else{
										//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("DROP", oldField.getFieldName(), "DECIMAL", oldField.getLength(), oldField.getScale()));
										arrCompare.get(no_database).getTable(i).addColumn(new Column("DROP", oldField.getFieldName(), "DECIMAL", oldField.getLength(), oldField.getScale()));
									}
								}
								else{
									oldFieldType = oldField.getType();
									newFieldType = newField.getType();
									if(newFieldType == oldFieldType){ //COMPARE FIELD TYPE LENGTH SCALE
										if(newField.getLength() == oldField.getLength()){
											if(newField.getScale() == oldField.getScale()){
												continue;
											}
											else{
												//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
												arrCompare.get(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
											}
										}
										else{
											if(newFieldType.getCode() == "TEXT"){
												//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "CHAR", newField.getLength()));
												arrCompare.get(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "CHAR", newField.getLength()));
											}
											else if(newFieldType.getCode() == "NUMBER"){
												//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));;
												arrCompare.get(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));;
											}
										}
									}
									else{
										if(newFieldType.getCode() == "TEXT"){
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "CHAR", newField.getLength()));
											arrCompare.get(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "CHAR", newField.getLength()));
										}
										else if(newFieldType.getCode() == "NUMBER"){
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
											arrCompare.get(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
										}
									}
								}
							}
							
							for(int y = 0 ; y < newTable.getFieldCount() ; y++){ //NEW TABLE
								newField = newTable.getFieldAt(y);
								newFieldType = newField.getType();
								oldField = oldTable.getField(newField.getFieldName());
								if(oldField == null){ //ADD COLUMN
									if(newFieldType.getCode() == "TEXT"){
										if(y == 0){
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "CHAR", newField.getLength()));
											arrCompare.get(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "CHAR", newField.getLength()));
										}else{
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "CHAR", newField.getLength()));
											arrCompare.get(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "CHAR", newField.getLength()));
										}
									}
									else if(newFieldType.getCode() == "NUMBER"){
										if(y == 0){
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
											arrCompare.get(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
										}else{
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
											arrCompare.get(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
										}
									}
								}
							}
							
							newPK = newTable.getPrimaryKey();
							oldPK = oldTable.getPrimaryKey();
							
							ArrayList<MasicField> listNewPK = null;
							ArrayList<MasicField> listOldPK = null;
							
							if(newPK == null){
								if(oldPK == null){
									continue;
								}else{ //DROP PRIMARY KEY
									//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("DROP"));
									arrCompare.get(no_database).getTable(i).addColumn(new Column("DROP"));
								}
							}
							else{ //NEW PK NOT NULL
								if(oldPK == null){ //ADD PRIMARY KEY
									strKey="";
									listNewPK = newPK.getFieldList();
									for(int z = 0 ; z < listNewPK.size() ; z++){
										if(z == (listNewPK.size()-1)){
											strKey+=listNewPK.get(z).getFieldName();
										}else{
											strKey+=listNewPK.get(z).getFieldName()+",";
										}
									}
									//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", strKey));
									arrCompare.get(no_database).getTable(i).addColumn(new Column("ADD", strKey));
								}else{ //OLD PK NOT NULL
									if(newPK.getSegmentCount() == oldPK.getSegmentCount()){
										listNewPK = newPK.getFieldList();
										listOldPK = oldPK.getFieldList();
										for(int z = 0 ; z < listNewPK.size() ; z++){ //CHECK SEGMENT NAME
											if(listOldPK.get(z).getFieldName().equals(listNewPK.get(z).getFieldName())){
												continue;
											}
											else{ //PK NAME NOT SAME
												strKey="";
												for(int t = 0 ; t < listNewPK.size() ; t++){
													if(t == (listNewPK.size()-1)){
														strKey+=listNewPK.get(t).getFieldName();
													}else{
														strKey+=listNewPK.get(t).getFieldName()+",";
													}
												}
												//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", strKey));
												arrCompare.get(no_database).getTable(i).addColumn(new Column("UPDATE", strKey));
												break;
											}
										}
									}
									else{
										strKey="";
										listNewPK = newPK.getFieldList();
										for(int z = 0 ; z < listNewPK.size() ; z++){
											if(z == (listNewPK.size()-1)){
												strKey+=listNewPK.get(z).getFieldName();
											}else{
												strKey+=listNewPK.get(z).getFieldName()+",";
											}
										}
										//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", strKey));
										arrCompare.get(no_database).getTable(i).addColumn(new Column("UPDATE", strKey));
									}
								}
							}
						}
					}
					/*================== END =================*/
					}
				}//if *.sch
			}
			
			if(newDBName.getName().endsWith(".sch")){
				System.out.println("before : "+no_database);
				no_database++;
				System.out.println("after : "+no_database);
			}
			
		}
	}
	
	private void sentMail(String newSch , String oldSch) throws IOException{
		System.out.println("start method : sentMail");
		
		File pathNewSchema = new File(newSch);
		File pathOldSchema = new File(oldSch);
		
		System.out.println("path schema_changed : \t" + pathNewSchema.getPath());
		System.out.println("path schema_original : \t" + pathOldSchema.getPath());
		
		FileFilter directory = new FileFilter() {
			@Override
			public boolean accept(File file) {
				// TODO Auto-generated method stub
				if(file.isDirectory()){
					return true;
				}
				else if(file.isFile()){
					return true;
				} 
				return false;
			}
		};
		
		File[] pathNew = pathNewSchema.listFiles(directory);
		File[] pathOld = pathOldSchema.listFiles(directory);
		
		int no_server=0;
		
		for(File listNewServer : pathNew){
			for(File listOldServer : pathOld ){
				
				if(listNewServer.isDirectory()&&listOldServer.isDirectory()){
					if(listNewServer.getName().equals(".svn")&&listOldServer.getName().equals(".svn")){
						//continue;
					}
					
					if(listNewServer.getName().equals(listOldServer.getName())){
						
						
						System.out.println(listNewServer.getName());
						System.out.println(listOldServer.getName());
						
						arrList.add(new Server(listNewServer.getName()));
						
						File pathNewFolder = new File(listNewServer.getCanonicalPath());
						File pathOldFolder = new File(listOldServer.getCanonicalPath());
						File[] listDBNew = pathNewFolder.listFiles();
						File[] listDBold = pathOldFolder.listFiles();
						
						int no_database=0 ;
						
						for(File newDBName : listDBNew){
							for(File oldDBName : listDBold){
								if(newDBName.getName().endsWith(".sch")&&oldDBName.getName().endsWith(".sch")){
								if(newDBName.getName().equals(oldDBName.getName())){
									/*================== START ================*/
									MasicSchema oldSchema = SchemaCompiler.getSchema(oldDBName.getCanonicalFile());
									MasicSchema newSchema = SchemaCompiler.getSchema(newDBName.getCanonicalFile());
									arrList.get(no_server).addDatabase(new Database(oldSchema.getSchemaName()));
									
									MasicTable oldTable = null;
									MasicTable newTable = null;
									
									MasicField oldField = null;
									MasicField newField = null;
									
									MasicFieldType oldFieldType = null;
									MasicFieldType newFieldType = null;
									
									MasicKey oldPK = null;
									MasicKey newPK = null;
					
									String strKey; 
									
									for(int i = 0 ; i < oldSchema.getTableCount() ; i++){
										oldTable = oldSchema.getTableAt(i);
										newTable = newSchema.getTableByName(oldTable.getTableName()); //COMPARE TABLE NEW & OLD
										
										//System.out.println("Path old : " + oldTable.getTablePath());
										//System.out.println("Path new : " + newTable.getTablePath());
										
										
										if(newTable == null){ //DROP TABLE
											arrList.get(no_server).getDatabase(no_database).addTable(new Table(oldTable.getTableName(),"DROP"));
										}
										else{ // ALTER TABLE
											arrList.get(no_server).getDatabase(no_database).addTable(new Table(oldTable.getTableName()));
											
											for(int x = 0 ; x < oldTable.getFieldCount() ; x++){ // OLD TABLE
												oldField = oldTable.getFieldAt(x);
												newField = newTable.getField(oldField.getFieldName());
												if(newField == null){ //DROP COLUMN
													if(oldFieldType.getCode() == "TEXT"){
														arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("DROP", oldField.getFieldName(), "CHAR", oldField.getLength()));
													}else{
														arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("DROP", oldField.getFieldName(), "DECIMAL", oldField.getLength(), oldField.getScale()));
													}
												}
												else{
													oldFieldType = oldField.getType();
													newFieldType = newField.getType();
													if(newFieldType == oldFieldType){ //COMPARE FIELD TYPE LENGTH SCALE
														if(newField.getLength() == oldField.getLength()){
															if(newField.getScale() == oldField.getScale()){
																continue;
															}
															else{
																arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
															}
														}
														else{
															if(newFieldType.getCode() == "TEXT"){
																arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "CHAR", newField.getLength()));
															}
															else if(newFieldType.getCode() == "NUMBER"){
																arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));;
															}
														}
													}
													else{
														if(newFieldType.getCode() == "TEXT"){
															arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "CHAR", newField.getLength()));
														}
														else if(newFieldType.getCode() == "NUMBER"){
															arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
														}
													}
												}
											}
											
											for(int y = 0 ; y < newTable.getFieldCount() ; y++){ //NEW TABLE
												newField = newTable.getFieldAt(y);
												newFieldType = newField.getType();
												oldField = oldTable.getField(newField.getFieldName());
												if(oldField == null){ //ADD COLUMN
													if(newFieldType.getCode() == "TEXT"){
														if(y == 0){
															arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "CHAR", newField.getLength()));
														}else{
															arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "CHAR", newField.getLength()));
														}
													}
													else if(newFieldType.getCode() == "NUMBER"){
														if(y == 0){
															arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
														}else{
															arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
														}
													}
												}
											}
											
											newPK = newTable.getPrimaryKey();
											oldPK = oldTable.getPrimaryKey();
											
											ArrayList<MasicField> listNewPK = null;
											ArrayList<MasicField> listOldPK = null;
											
											if(newPK == null){
												if(oldPK == null){
													continue;
												}else{ //DROP PRIMARY KEY
													arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("DROP"));
												}
											}
											else{ //NEW PK NOT NULL
												if(oldPK == null){ //ADD PRIMARY KEY
													strKey="";
													listNewPK = newPK.getFieldList();
													for(int z = 0 ; z < listNewPK.size() ; z++){
														if(z == (listNewPK.size()-1)){
															strKey+=listNewPK.get(z).getFieldName();
														}else{
															strKey+=listNewPK.get(z).getFieldName()+",";
														}
													}
													arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", strKey));
												}else{ //OLD PK NOT NULL
													if(newPK.getSegmentCount() == oldPK.getSegmentCount()){
														listNewPK = newPK.getFieldList();
														listOldPK = oldPK.getFieldList();
														for(int z = 0 ; z < listNewPK.size() ; z++){ //CHECK SEGMENT NAME
															if(listOldPK.get(z).getFieldName().equals(listNewPK.get(z).getFieldName())){
																continue;
															}
															else{ //PK NAME NOT SAME
																strKey="";
																for(int t = 0 ; t < listNewPK.size() ; t++){
																	if(t == (listNewPK.size()-1)){
																		strKey+=listNewPK.get(t).getFieldName();
																	}else{
																		strKey+=listNewPK.get(t).getFieldName()+",";
																	}
																}
																arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", strKey));
																break;
															}
														}
													}
													else{
														strKey="";
														listNewPK = newPK.getFieldList();
														for(int z = 0 ; z < listNewPK.size() ; z++){
															if(z == (listNewPK.size()-1)){
																strKey+=listNewPK.get(z).getFieldName();
															}else{
																strKey+=listNewPK.get(z).getFieldName()+",";
															}
														}
														arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", strKey));
													}
												}
											}
										}
									}
									/*================== END =================*/
								}}
							}
							no_database++;
						}
						no_server++;
					}
				}
			}
		}
	}
	
	private void createSQL() throws IOException{
		serverName = new ArrayList<String>();
		sqlScript = new ArrayList<String>();
		
		File pathNewSchema = new File("/home/mysql/replication/schema");
		File pathOldSchema = new File("/home/mysql/replication/previous");
		
		FileFilter directory = new FileFilter() {
			@Override
			public boolean accept(File file) {
				// TODO Auto-generated method stub
				if(file.isDirectory()){
					return true;
				}
				else if(file.isFile()){
					return true;
				}
				return false;
			}
		};
		
		File[] pathNew = pathNewSchema.listFiles(directory);
		File[] pathOld = pathOldSchema.listFiles(directory);
		
		for(File listNewServer : pathNew){
			for(File listOldServer : pathOld ){
				if(listNewServer.isDirectory()&&listOldServer.isDirectory()){
					if(listNewServer.getName().equals(".svn")&&listOldServer.getName().equals(".svn")){
						continue;
					}
					if(listNewServer.getName().equals(listOldServer.getName())){
						File pathNewFolder = new File(listNewServer.getCanonicalPath());
						File pathOldFolder = new File(listOldServer.getCanonicalPath());
						File[] listDBNew = pathNewFolder.listFiles();
						File[] listDBold = pathOldFolder.listFiles();
						for(File newDBName : listDBNew){
							for(File oldDBName : listDBold){
								if(newDBName.getName().endsWith(".sch")&&oldDBName.getName().endsWith(".sch")){
								if(newDBName.getName().equals(oldDBName.getName())){
									strScript="";
									/*================== START ================*/
									MasicSchema oldSchema = SchemaCompiler.getSchema(oldDBName.getCanonicalFile());
									MasicSchema newSchema = SchemaCompiler.getSchema(newDBName.getCanonicalFile());
									
									MasicTable oldTable = null;
									MasicTable newTable = null;
									
									MasicField oldField = null;
									MasicField newField = null;
									
									MasicFieldType oldFieldType = null;
									MasicFieldType newFieldType = null;
									
									MasicKey oldPK = null;
									MasicKey newPK = null;
									
									int err;									
									String strPK;
									
									ArrayList<String> listDrop = new ArrayList<String>();
									ArrayList<String> listAlter = new ArrayList<String>();
									
									for(int i = 0 ; i < oldSchema.getTableCount() ; i++){
										err=0;
										
										listAlter.clear();
										listDrop.clear();
										
										oldTable = oldSchema.getTableAt(i);
										newTable = newSchema.getTableByName(oldTable.getTableName()); //COMPARE TABLE NEW & OLD
										
										if(newTable == null){ //DROP TABLE
											listDrop.add("DROP TABLE "+oldTable.getTableName()+";\n");
										}
										else{ // ALTER TABLE
											for(int x = 0 ; x < oldTable.getFieldCount() ; x++){ // OLD TABLE
												oldField = oldTable.getFieldAt(x);
												newField = newTable.getField(oldField.getFieldName());
												if(newField == null){ //DROP COLUMN
													listAlter.add("DROP COULMN "+oldField.getFieldName());
													err++;
												}
												else{
													oldFieldType = oldField.getType();
													newFieldType = newField.getType();
													if(newFieldType == oldFieldType){ //COMPARE FIELD TYPE LENGTH SCALE
														if(newField.getLength() == oldField.getLength()){
															if(newField.getScale() == oldField.getScale()){
																continue;
															}
															else{
																listAlter.add("CHANGE COLUMN `"+newField.getFieldName()+"` `"+newField.getFieldName()+"` DECIMAL("+newField.getLength()+","+newField.getScale()+")");
																err++;
															}
														}
														else{
															if(newFieldType.getCode() == "TEXT"){
																listAlter.add("CHANGE COLUMN `"+newField.getFieldName()+"` `"+newField.getFieldName()+"` CHAR("+newField.getLength()+")");
																err++;
															}
															else if(newFieldType.getCode() == "NUMBER"){
																listAlter.add("CHANGE COLUMN `"+newField.getFieldName()+"` `"+newField.getFieldName()+"` DECIMAL("+newField.getLength()+","+newField.getScale()+")");
																err++;
															}
														}
													}
													else{
														if(newFieldType.getCode() == "TEXT"){
															listAlter.add("CHANGE COLUMN `"+newField.getFieldName()+"` `"+newField.getFieldName()+"` CHAR("+newField.getLength()+")");
															err++;
														}
														else if(newFieldType.getCode() == "NUMBER"){
															listAlter.add("CHANGE COLUMN `"+newField.getFieldName()+"` `"+newField.getFieldName()+"` DECIMAL("+newField.getLength()+","+newField.getScale()+")");
															err++;
														}
													}
												}
											}
											
											for(int y = 0 ; y < newTable.getFieldCount() ; y++){ //NEW TABLE
												newField = newTable.getFieldAt(y);
												newFieldType = newField.getType();
												oldField = oldTable.getField(newField.getFieldName());
												if(oldField == null){ //ADD COLUMN
													if(newFieldType.getCode() == "TEXT"){
														if(y == 0){
															listAlter.add("ADD COLUMN `"+newField.getFieldName()+"` CHAR("+newField.getLength()+") FIRST");
															err++;
														}else{
															listAlter.add("ADD COLUMN `"+newField.getFieldName()+"` CHAR("+newField.getLength()+") AFTER `"+newTable.getFieldAt(y-1).getFieldName()+"`");
															err++;
														}
													}
													else if(newFieldType.getCode() == "NUMBER"){
														if(y == 0){
															listAlter.add("ADD COLUMN `"+newField.getFieldName()+"` DECIMAL("+newField.getLength()+","+newField.getScale()+") FIRST");
															err++;
														}else{
															listAlter.add("ADD COLUMN `"+newField.getFieldName()+"` DECIMAL("+newField.getLength()+","+newField.getScale()+") AFTER `"+newTable.getFieldAt(y-1).getFieldName()+"`");
															err++;
														}
													}
												}
											}
											
											newPK = newTable.getPrimaryKey();
											oldPK = oldTable.getPrimaryKey();
											
											ArrayList<MasicField> listNewPK = null;
											ArrayList<MasicField> listOldPK = null;
											
											if(newPK == null){
												if(oldPK == null){
													continue;
												}else{ //DROP PRIMARY KEY
													listAlter.add("DROP PRIMARY KEY");
													err++;
												}
											}
											else{ //NEW PK NOT NULL
												if(oldPK == null){ //ADD PRIMARY KEY
													strPK="";
													strPK="ADD PRIMARY KEY (";
													listNewPK = newPK.getFieldList();
													for(int z = 0 ; z < listNewPK.size() ; z++){
														if(z == (listNewPK.size()-1)){
															strPK+="`"+listNewPK.get(z).getFieldName()+"`)";
														}else{
															strPK+="`"+listNewPK.get(z).getFieldName()+"`,";
														}
													}
													listAlter.add(strPK);
													err++;
												}else{ //OLD PK NOT NULL
													if(newPK.getSegmentCount() == oldPK.getSegmentCount()){
														listNewPK = newPK.getFieldList();
														listOldPK = oldPK.getFieldList();
														for(int z = 0 ; z < listNewPK.size() ; z++){ //CHECK SEGMENT NAME
															if(listOldPK.get(z).getFieldName().equals(listNewPK.get(z).getFieldName())){
																continue;
															}
															else{ //PK NAME NOT SAME
																strPK="";
																listAlter.add("DROP PRIMARY KEY");
																strPK="ADD PRIMARY KEY (";
																for(int t = 0 ; t < listNewPK.size() ; t++){
																	if(t == (listNewPK.size()-1)){
																		strPK+="`"+listNewPK.get(t).getFieldName()+"`)";
																	}else{
																		strPK+="`"+listNewPK.get(t).getFieldName()+"`,";
																	}
																}
																listAlter.add(strPK);
																err++;
																break;
															}
														}
													}
													else{
														strPK="";
														listAlter.add("DROP PRIMARY KEY");
														strPK="ADD PRIMARY KEY (";
														listNewPK = newPK.getFieldList();
														for(int z = 0 ; z < listNewPK.size() ; z++){
															if(z == (listNewPK.size()-1)){
																strPK+="`"+listNewPK.get(z).getFieldName()+"`)";
															}else{
																strPK+="`"+listNewPK.get(z).getFieldName()+"`,";
															}
														}
														listAlter.add(strPK);
														err++;
													}
												}
											}
										}
										if(!listDrop.isEmpty()){
											for(int t = 0 ; t < listDrop.size() ; t++){ // FOR GET SQL
												strScript+=listDrop.get(t);
											}
										}
										if(err>0){											
											strScript+="ALTER TABLE `"+oldSchema.getSchemaName()+"`.`"+oldTable.getTableName()+"`\n";
											for(int t = 0 ; t < listAlter.size() ; t++){ // FOR GET SQL
												if(t == (listAlter.size()-1)){
													strScript+=listAlter.get(t)+";\n\n";
												}
												else{
													strScript+=listAlter.get(t)+",\n";
												}
											}
										}
									}
									
									/*================== END =================*/
								}}
							}
							if(!(strScript == "")){
								serverName.add(listNewServer.getName());
								sqlScript.add(strScript);
							}
						}
					}
				}
			}
		}
		getSQL();
		getWriteTextFile();
	}
	
	private void getSQL() {
		if(serverName.size() > 0){
			for(int i = 0 ; i < serverName.size() ; i++){
				System.out.println(serverName.get(i));
				System.out.println(sqlScript.get(i));
			}
		}else{
			System.out.println("No Data");
		}
	}
	
	private void getWriteTextFile() throws IOException {
		String path = null;
		
		File file;
		FileWriter writer;
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		
		if(serverName.size() == sqlScript.size()){
			if(serverName.size() > 0 && sqlScript.size() > 0){
				for(int i = 0 ; i < serverName.size() ; i++){
					path = "/home/mysql/replication/script/"+serverName.get(i)+" "+dateFormat.format(date)+".sql";
					file = new File(path);
					writer = new FileWriter(file, false);
					writer.write(sqlScript.get(i));
					writer.close();
				}
				System.out.println("Write Success!!!");
			}else{
				System.out.println("Write Unsuccess");
			}
		}
	}
	
	public void getSentSch() throws Exception{
		System.out.println("*** Send Mail Compare Schema ***");
		
		TLMail tlmail;
		
		String subject = "Table Structure Change";
		String sender = "thiwaporn.kha@thailife.com";
		
		Vector<String> receiver = new Vector<>();
		Vector<String> cc = new Vector<>();
		
		//receiver.add("manisa@thailife.com");
		//receiver.add("pawisarat.rha@thailife.com");
		receiver.add("thiwaporn.kha@thailife.com");
		cc.add("thiwaporn.kha@thailife.com");
		
		String prefix = "<html><body>";
		StringBuilder sb = new StringBuilder(prefix);
		sb.append("<p>");
		sb.append("เรียน ผู้เกี่ยวข้อง <br>ไทยประกันชีวิตจะเปลี่ยนแปลงโครงสร้างตารางของฐานข้อมูล ซึ่งจะมีผลกระทบ <br>ภายในวันที่ <strong>"+dateThai()+"<strong>");
		sb.append("</p>\n");
		sb.append("<table border=1 cellpadding=1 cellspacing=1>\n");
		sb.append("<thead>");
		sb.append("<tr bgcolor=#9FC5E8>");
		sb.append("<th>");
		sb.append("DATABASE");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("TABLE");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("CHANGE TYPE");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("COLUMN");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("TYPE");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("LENGTH");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("SCALE");
		sb.append("</th>");
		sb.append("</tr>\n");
		sb.append("</thead>");
		sb.append("<tbody>");
		
		for(int i = 0 ; i < arrCompare.size() ; i++){
			for(int x = 0 ; x < arrCompare.get(i).getTableCount() ; x++){
				String DBName = arrCompare.get(i).getDatabase();
				String statusTB = arrCompare.get(i).getTable(x).getStatus();
				if(statusTB == null){
					int countCol = arrCompare.get(i).getTable(x).getColumnCount();
					if(countCol > 0){
						isHave = "y";
						
						System.out.println("TBName : "+arrCompare.get(i).getTable(x).getTableName()+" Status : "+arrCompare.get(i).getTable(x).getStatus()+" CountCol : "+countCol);
						String TBName = arrCompare.get(i).getTable(x).getTableName();
						
						sb.append("<tr>");
						sb.append("<td rowspan="+countCol+">");
						sb.append(DBName);
						sb.append("</td>");
						sb.append("<td rowspan="+countCol+">");
						sb.append(TBName);
						sb.append("</td>\n");
						for(int y = 0 ; y < arrCompare.get(i).getTable(x).getColumnCount() ; y++){
							String status = arrCompare.get(i).getTable(x).getColumn(y).getStatus();
							String ColName = arrCompare.get(i).getTable(x).getColumn(y).getName();
							String Type = arrCompare.get(i).getTable(x).getColumn(y).getType();
							int length = arrCompare.get(i).getTable(x).getColumn(y).getLength();
							int scale = arrCompare.get(i).getTable(x).getColumn(y).getScale();
							
							if(y == 0){
								sb.append("<td align=center>");
								sb.append(status);
								sb.append("</td>\n");
								sb.append("<td>");
								sb.append(ColName);
								sb.append("</td>\n");
								sb.append("<td align=center>");
								sb.append(Type);
								sb.append("</td>\n");
								sb.append("<td align=right>");
								sb.append(length);
								sb.append("</td>\n");
								sb.append("<td align=right>");
								sb.append(scale);
								sb.append("</td>\n");
							}
						sb.append("</tr>\n");
						}
						for(int y = 0 ; y < arrCompare.get(i).getTable(x).getColumnCount() ; y++){
							String status = arrCompare.get(i).getTable(x).getColumn(y).getStatus();
							String ColName = arrCompare.get(i).getTable(x).getColumn(y).getName();
							String Type = arrCompare.get(i).getTable(x).getColumn(y).getType();
							int length = arrCompare.get(i).getTable(x).getColumn(y).getLength();
							int scale = arrCompare.get(i).getTable(x).getColumn(y).getScale();
								
							if(y == 0 ){
								continue;
							}else{
								sb.append("<tr>");
								sb.append("<td align=center>");
								sb.append(status);
								sb.append("</td>\n");
								sb.append("<td>");
								sb.append(ColName);
								sb.append("</td>\n");
								sb.append("<td align=center>");
								sb.append(Type);
								sb.append("</td>\n");
								sb.append("<td align=right>");
								sb.append(length);
								sb.append("</td>\n");
								sb.append("<td align=right>");
								sb.append(scale);
								sb.append("</td>\n");
								sb.append("</tr>\n");
							}
						}
					}
				}else if(statusTB != null){//STATUS DROP TABLE
					isHave = "y";
					String TBName = arrCompare.get(i).getTable(x).getTableName();
					
					sb.append("<tr>");
					sb.append("<td>");
					sb.append(DBName);
					sb.append("</td>");
					sb.append("<td>");
					sb.append(TBName);
					sb.append("</td>");
					sb.append("<td align=center>");
					sb.append(statusTB);
					sb.append("</td>");
					sb.append("<td>");
					sb.append("</td>");
					sb.append("<td>");
					sb.append("</td>");
					sb.append("<td>");
					sb.append("</td>");
					sb.append("<td>");
					sb.append("</td>");
					sb.append("</tr>");
				}
			}
		}
		
		sb.append("</tbody>\n");
		sb.append("</table>");
		sb.append("\n<p>");
		sb.append("จึงเรียนมาเพื่อทราบ<br>ขอบคุณค่ะ");
		sb.append("</p>\n");
		sb.append("</body>");
		sb.append("</html>");
		
		try {
			System.out.println("isHave : "+isHave);
			if(isHave.equals("y")){
				tlmail = new TLMail();
				tlmail.setSender(sender);
				tlmail.setSubject(subject);
				tlmail.setContent(sb.toString());
				tlmail.setAllCCReceivers(cc);
				tlmail.setAllReceivers(receiver);
				PostMan post = new PostMan("mail.thailife.com");
				post.sendMail(tlmail); 
				
				System.out.println("Sender : "+sender);
				System.out.println("To : "+cc.toString());
				System.out.println("Cc : "+receiver.toString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getSentMail() throws Exception {
		TLMail tlmail;
		//ReportType code = ReportType.LOCUS_TABLE_STRUCTURE_CHANGE;
		//ReportInfo info = new ReportInfo(code);
		//String subject = info.getSubject();
		String subject = "Change Structure";
		String sender = "thiwaporn.kha@thailife.com";
		//String sender = "manisa@thailife.com";
		//String sender = info.getSender();
		
		//ArrayList<String> receivers = info.getTO();
		//ArrayList<String> ccReceivers = info.getCC();
		
		Vector<String> receiver = new Vector<>();
		Vector<String> cc = new Vector<>();
		
		//receiver.add("manisa@thailife.com");
		//receiver.add("pawisarat.rha@thailife.com");
		//receiver.add("iproc@thailife.com");
		receiver.add("thiwaporn.kha@thailife.com");
		cc.add("thiwaporn.kha@thailife.com");
		
		/*
		for(int i = 0 ; i < receivers.size() ; i++ ){
			receiver.add(receivers.get(i));
		}
		
		for(int i = 0 ; i < ccReceivers.size() ; i++){
			cc.add(ccReceivers.get(i));
		}
		*/
		
		String prefix = "<html><body>";
		StringBuilder sb = new StringBuilder(prefix);
		sb.append("<p>");
		sb.append("เรียน ผู้เกี่ยวข้อง <br>ไทยประกันชีวิตจะเปลี่ยนแปลงโครงสร้างตารางของฐานข้อมูล ซึ่งจะมีผลกระทบ <br>ภายในวันที่ <strong>"+dateThai()+"<strong>");
		sb.append("</p>\n");
		sb.append("<table border=1 cellpadding=1 cellspacing=1>\n");
		sb.append("<thead>");
		sb.append("<tr bgcolor=#9FC5E8>");
		sb.append("<th>");
		sb.append("DATABASE");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("TABLE");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("CHANGE TYPE");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("COLUMN");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("TYPE");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("LENGTH");
		sb.append("</th>");
		sb.append("<th>");
		sb.append("SCALE");
		sb.append("</th>");
		sb.append("</tr>\n");
		sb.append("</thead>");
		sb.append("<tbody>");
		
		System.setProperty("DB_CONFIG_PATH", "/c/resources/db");
		try(Connection conn = SecuredConnect.createConnection("infoasset");
				Statement st = conn.createStatement()){
			
			for(int sv = 0 ; sv < arrList.size() ; sv++){
				String serverName = arrList.get(sv).getServerName();
				for(int db = 0 ; db < arrList.get(sv).getDatabaseCount() ; db++){
					String databaseName = arrList.get(sv).getDatabase(db).getDatabase();
					for(int tb = 0 ; tb < arrList.get(sv).getDatabase(db).getTableCount() ; tb++){
						String tableName = arrList.get(sv).getDatabase(db).getTable(tb).getTableName();
						if(arrList.get(sv).getDatabase(db).getTable(tb).getColumnCount()>0){
							String sql = "select targetDatabase,targetTable "
									+ "from Replication.Mapping"
									+ " where slaveId=1 and serverName='"+serverName+"'"
									+ " and sourceDatabase='"+databaseName+"'"
									+ " and sourceTable='"+tableName+"'";
							ResultSet rs = st.executeQuery(sql);
							if(rs.next()){
								int countCol = arrList.get(sv).getDatabase(db).getTable(tb).getColumnCount();
								sb.append("<tr>");
								sb.append("<td rowspan="+countCol+">");
								sb.append(rs.getString("targetDatabase"));
								sb.append("</td>");
								sb.append("<td rowspan="+countCol+">");
								sb.append(rs.getString("targetTable"));
								sb.append("</td>");
								
								for(int cl = 0 ; cl < arrList.get(sv).getDatabase(db).getTable(tb).getColumnCount() ; cl++){
									if(cl == 0){
										String status = arrList.get(sv).getDatabase(db).getTable(tb).getColumn(cl).getStatus();
										String name = arrList.get(sv).getDatabase(db).getTable(tb).getColumn(cl).getName();
										String type = arrList.get(sv).getDatabase(db).getTable(tb).getColumn(cl).getType();
										int length = arrList.get(sv).getDatabase(db).getTable(tb).getColumn(cl).getLength();
										int scale = arrList.get(sv).getDatabase(db).getTable(tb).getColumn(cl).getScale();
										
										sb.append("<td align=center>");
										sb.append(status);
										sb.append("</td>\n");
										sb.append("<td>");
										sb.append(name);
										sb.append("</td>\n");
										sb.append("<td align=center>");
										sb.append(type);
										sb.append("</td>\n");
										sb.append("<td align=right>");
										sb.append(length);
										sb.append("</td>\n");
										sb.append("<td align=right>");
										sb.append(scale);
										sb.append("</td>\n");
									}
								}
								sb.append("</tr>\n");
								
								for(int cl = 0 ; cl < arrList.get(sv).getDatabase(db).getTable(tb).getColumnCount() ; cl++){
									if(cl == 0){
										continue;
									}else{
										String status = arrList.get(sv).getDatabase(db).getTable(tb).getColumn(cl).getStatus();
										String name = arrList.get(sv).getDatabase(db).getTable(tb).getColumn(cl).getName();
										String type = arrList.get(sv).getDatabase(db).getTable(tb).getColumn(cl).getType();
										int length = arrList.get(sv).getDatabase(db).getTable(tb).getColumn(cl).getLength();
										int scale = arrList.get(sv).getDatabase(db).getTable(tb).getColumn(cl).getScale();
										sb.append("<tr>");
										sb.append("<td align=center>");
										sb.append(status);
										sb.append("</td>\n");
										sb.append("<td>");
										sb.append(name);
										sb.append("</td>\n");
										sb.append("<td align=center>");
										sb.append(type);
										sb.append("</td>\n");
										sb.append("<td align=right>");
										sb.append(length);
										sb.append("</td>\n");
										sb.append("<td align=right>");
										sb.append(scale);
										sb.append("</td>\n");
										sb.append("</tr>\n");
									}
								}
							}
						}//HAVE COLUMN
						if(arrList.get(sv).getDatabase(db).getTable(tb).getStatus() != null){ //STATUS DROP TABLE
							String statusTable = arrList.get(sv).getDatabase(db).getTable(tb).getStatus();
							String sql = "select targetDatabase,targetTable "
									+ "from Replication.Mapping"
									+ " where slaveId=1 and serverName='"+serverName+"'"
									+ " and sourceDatabase='"+databaseName+"'"
									+ " and sourceTable='"+tableName+"'";
							ResultSet rs = st.executeQuery(sql);
							if(rs.next()){
								sb.append("<tr>");
								sb.append("<td>");
								sb.append(rs.getString("targetDatabase"));
								sb.append("</td>");
								sb.append("<td>");
								sb.append(rs.getString("targetTable"));
								sb.append("</td>");
								sb.append("<td align=center>");
								sb.append(statusTable);
								sb.append("</td>");
								sb.append("<td>");
								sb.append("</td>");
								sb.append("<td>");
								sb.append("</td>");
								sb.append("<td>");
								sb.append("</td>");
								sb.append("<td>");
								sb.append("</td>");
								sb.append("</tr>");
							}
						}
					}
				}
			}
			//conn.close();
		}
		
		sb.append("</tbody>\n");
		sb.append("</table>");
		sb.append("\n<p>");
		sb.append("จึงเรียนมาเพื่อทราบ<br>ขอบคุณค่ะ");
		sb.append("</p>\n");
		sb.append("</body>");
		sb.append("</html>");
		
		try {
			tlmail = new TLMail();
			tlmail.setSender(sender);
			tlmail.setSubject(subject);
			tlmail.setContent(sb.toString());
			tlmail.setAllCCReceivers(cc);
			tlmail.setAllReceivers(receiver);
			PostMan post = new PostMan("mail.thailife.com");
			post.sendMail(tlmail); 
			System.out.println("Sender : "+sender);
			System.out.println("To : "+cc.toString());
			System.out.println("Cc : "+receiver.toString());
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static String dateThai() throws Exception{
		String months[] = {"มกราคม","กุมภาพันธ์","มีนาคม","เมษายน",
							"พฤกษภาคม","มิถุนายน","กรกฎาคม","สิงหาคม",
							"กันยายน","ตุลาคม","พฤศจิกายน","ธันวาคม"};
		
		
		Calendar cal = Calendar.getInstance();
	
		cal.add(Calendar.DATE, 1);//เพิ่มวันเข้าไปอีก 1 วัน
		
		int year=0,month=0,day=0;
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DATE);
		
		return String.format("%s %s %s", day, months[month], year+543);
	} 
}
