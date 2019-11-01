package infoasset.schema.compare;

import infoasset.schema.MasicField;
import infoasset.schema.MasicFieldType;
import infoasset.schema.MasicKey;
import infoasset.schema.MasicSchema;
import infoasset.schema.MasicTable;
import infoasset.schema.SchemaCompiler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.text.TabExpander;

import org.apache.commons.lang3.text.StrSubstitutor;

public class compare_schema_main {
	
	ArrayList<Database> arr = null;

	public compare_schema_main(String sch_changed, String sch_original) throws IOException {
		// TODO Auto-generated constructor stub
		
		System.out.println("compare_schema_main");
		
		arr = new ArrayList<Database>();
		
		File path_sch_changed = new File(sch_changed);
		File path_sch_original = new File(sch_original);
		
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
		
		File[] path_changed = path_sch_changed.listFiles(directory);
		File[] path_original = path_sch_original.listFiles(directory);
		
		int no_database=0 ;
		
		System.out.println("path schema_changed : \t" + path_sch_changed.getPath() + " | amout file : " + path_changed.length);
		System.out.println("path schema_original : \t" + path_sch_original.getPath() + " | amout file : " + path_original.length);
		
		
		for(File listfile_changed : path_changed){
			String path_filename_changed = listfile_changed.getPath();
			String filename_changed = listfile_changed.getName();
			
			for(File listfile_original : path_original){
				String path_filename_original = listfile_original.getPath();
				String filename_original = listfile_original.getName();
				
				if(isSchema(filename_changed) && isSchema(filename_original) && isSameSchema(filename_original, filename_changed)){
					MasicSchema masic_changed = compileSchema(path_filename_changed);
					MasicSchema masic_original = compileSchema(path_filename_original);
					String db_name = masic_changed.getSchemaName();
					
					arr.add(new Database(db_name));
					
					MasicTable table_changed = null;
					MasicTable table_original = null;
					
					MasicField field_changed = null;
					MasicField field_original = null;
					
					MasicFieldType type_changed = null;
					MasicFieldType type_original = null;
					
					MasicKey pk_changed = null;
					MasicKey pk_original = null;
	
					String str_key; 
					
					//
					for(int i = 0 ; i < masic_original.getTableCount() ; i++){
						table_original = masic_original.getTableAt(i);
						table_changed = masic_changed.getTableByName(table_original.getTableName()); //COMPARE TABLE NEW & OLD
						
						if(table_changed == null){ //DROP TABLE
							arr.get(no_database).addTable(new Table(table_original.getTableName(),"DROP"));
							//arrList.get(no_server).getDatabase(no_database).addTable(new Table(oldTable.getTableName(),"DROP"));
						}
						else{ // ALTER TABLE
							//arrList.get(no_server).getDatabase(no_database).addTable(new Table(oldTable.getTableName()));
							arr.get(no_database).addTable(new Table(table_original.getTableName()));
							
							for(int x = 0 ; x < table_original.getFieldCount() ; x++){ // OLD TABLE
								field_original = table_original.getFieldAt(x);
								field_changed = table_changed.getField(field_original.getFieldName());
								if(field_changed == null){ //DROP COLUMN
									if(type_original.getCode() == "TEXT"){
										arr.get(no_database).getTable(i).addColumn(new Column("DROP", field_original.getFieldName(), "CHAR", field_original.getLength()));
										//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("DROP", oldField.getFieldName(), "CHAR", oldField.getLength()));
									}else{
										arr.get(no_database).getTable(i).addColumn(new Column("DROP", field_original.getFieldName(), "DECIMAL", field_original.getLength(), field_original.getScale()));
										//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("DROP", oldField.getFieldName(), "DECIMAL", oldField.getLength(), oldField.getScale()));
									}
								}
								else{
									type_original = field_original.getType();
									type_changed = field_changed.getType();
									if(type_changed == type_original){ //COMPARE FIELD TYPE LENGTH SCALE
										if(field_changed.getLength() == field_original.getLength()){
											if(field_changed.getScale() == field_original.getScale()){
												continue;
											}
											else{
												arr.get(no_database).getTable(i).addColumn(new Column("UPDATE", field_changed.getFieldName(), "DECIMAL", field_changed.getLength(), field_changed.getScale()));
												//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
											}
										}
										else{
											if(type_changed.getCode() == "TEXT"){
												arr.get(no_database).getTable(i).addColumn(new Column("UPDATE", field_changed.getFieldName(), "CHAR", field_changed.getLength()));
												//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "CHAR", newField.getLength()));
											}
											else if(type_changed.getCode() == "NUMBER"){
												arr.get(no_database).getTable(i).addColumn(new Column("UPDATE", field_changed.getFieldName(), "DECIMAL", field_changed.getLength(), field_changed.getScale()));
												//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));;
											}
										}
									}
									else{
										if(type_changed.getCode() == "TEXT"){
											arr.get(no_database).getTable(i).addColumn(new Column("UPDATE", field_changed.getFieldName(), "CHAR", field_changed.getLength()));
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "CHAR", newField.getLength()));
										}
										else if(type_changed.getCode() == "NUMBER"){
											arr.get(no_database).getTable(i).addColumn(new Column("UPDATE", field_changed.getFieldName(), "DECIMAL", field_changed.getLength(), field_changed.getScale()));
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
										}
									}
								}
							}
							
							for(int y = 0 ; y < table_changed.getFieldCount() ; y++){ //NEW TABLE
								field_changed = table_changed.getFieldAt(y);
								type_changed = field_changed.getType();
								field_original = table_original.getField(field_changed.getFieldName());
								if(field_original == null){ //ADD COLUMN
									if(type_changed.getCode() == "TEXT"){
										if(y == 0){
											arr.get(no_database).getTable(i).addColumn(new Column("ADD", field_changed.getFieldName(), "CHAR", field_changed.getLength()));
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "CHAR", newField.getLength()));
										}else{
											arr.get(no_database).getTable(i).addColumn(new Column("ADD", field_changed.getFieldName(), "CHAR", field_changed.getLength()));
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "CHAR", newField.getLength()));
										}
									}
									else if(type_changed.getCode() == "NUMBER"){
										if(y == 0){
											arr.get(no_database).getTable(i).addColumn(new Column("ADD", field_changed.getFieldName(), "DECIMAL", field_changed.getLength(), field_changed.getScale()));
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
										}else{
											arr.get(no_database).getTable(i).addColumn(new Column("ADD", field_changed.getFieldName(), "DECIMAL", field_changed.getLength(), field_changed.getScale()));
											//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", newField.getFieldName(), "DECIMAL", newField.getLength(), newField.getScale()));
										}
									}
								}
							}
							
							pk_changed = table_changed.getPrimaryKey();
							pk_original = table_original.getPrimaryKey();
							
							ArrayList<MasicField> listNewPK = null;
							ArrayList<MasicField> listOldPK = null;
							
							if(pk_changed == null){
								if(pk_original == null){
									continue;
								}else{ //DROP PRIMARY KEY
									arr.get(no_database).getTable(i).addColumn(new Column("DROP"));
									//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("DROP"));
								}
							}
							else{ //NEW PK NOT NULL
								if(pk_original == null){ //ADD PRIMARY KEY
									str_key="";
									listNewPK = pk_changed.getFieldList();
									for(int z = 0 ; z < listNewPK.size() ; z++){
										if(z == (listNewPK.size()-1)){
											str_key+=listNewPK.get(z).getFieldName();
										}else{
											str_key+=listNewPK.get(z).getFieldName()+",";
										}
									}
									arr.get(no_database).getTable(i).addColumn(new Column("ADD", str_key));
									//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("ADD", strKey));
								}else{ //OLD PK NOT NULL
									if(pk_changed.getSegmentCount() == pk_original.getSegmentCount()){
										listNewPK = pk_changed.getFieldList();
										listOldPK = pk_original.getFieldList();
										for(int z = 0 ; z < listNewPK.size() ; z++){ //CHECK SEGMENT NAME
											if(listOldPK.get(z).getFieldName().equals(listNewPK.get(z).getFieldName())){
												continue;
											}
											else{ //PK NAME NOT SAME
												str_key="";
												for(int t = 0 ; t < listNewPK.size() ; t++){
													if(t == (listNewPK.size()-1)){
														str_key+=listNewPK.get(t).getFieldName();
													}else{
														str_key+=listNewPK.get(t).getFieldName()+",";
													}
												}
												arr.get(no_database).getTable(i).addColumn(new Column("UPDATE", str_key));
												//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", strKey));
												break;
											}
										}
									}
									else{
										str_key="";
										listNewPK = pk_changed.getFieldList();
										for(int z = 0 ; z < listNewPK.size() ; z++){
											if(z == (listNewPK.size()-1)){
												str_key+=listNewPK.get(z).getFieldName();
											}else{
												str_key+=listNewPK.get(z).getFieldName()+",";
											}
										}
										arr.get(no_database).getTable(i).addColumn(new Column("UPDATE", str_key));
										//arrList.get(no_server).getDatabase(no_database).getTable(i).addColumn(new Column("UPDATE", strKey));
									}
								}
							}
						}
					}//
					
				} 
			}
		}
	}
	
	
	public void get_data_changed(){
		System.out.println("--- Get Data Changed---");
		for(int d = 0 ; d < arr.size() ; d++){
			Database db = arr.get(d);
			String db_name = db.getDatabase();
			
			System.out.println("--- Database Name : " + db_name);
			int tb_count = arr.get(d).getTableCount();
			System.out.println("--- Amount changed table : " + tb_count);
			
			for(int t = 0 ; t < tb_count ; t++){
				Table tb = arr.get(d).getTable(t);
				
				String tb_name = tb.getTableName();
				int col_count = tb.getColumnCount();
				
				if(col_count > 0){
					System.out.println("Changed Column");
					System.out.println("--- Table Name : " + tb_name + " ---- Amount changed column  : " + col_count);
					for(int c = 0 ; c < col_count ; c++){
						Column col = tb.getColumn(c);
						String col_status = col.getStatus();
						String col_name = col.getName();
						String col_type = col.getType();
						int col_length = col.getLength();
						int col_scale = col.getScale();
						
						String toString = "[";
						toString += "db_name : " + db_name + ", ";
						toString += "tb_name : " + tb_name + ", ";
						toString += "col_status : " + col.getStatus() + ", ";
						toString += "col_name : " + col_name + ", ";
						toString += "col_type : " + col_type + ", ";
						toString += "col_length : " + col_length + "," + col_scale;
						toString += "]";
						
						System.out.println(toString);
					}
				}
			}
		}
	}
	
	public MasicSchema compileSchema(String filename) throws IOException{
		
		System.out.println("Compile Schema : "+filename);
		
		File file = new File(filename);
		MasicSchema compile = SchemaCompiler.getSchema(file);
		return compile;
	}
	
	public boolean isSameSchema(String filename1, String filename2){
		if(filename1.compareTo(filename2) == 0) 
			return true;
		return false;
	}
		
	public boolean isSchema(String filename){
		return filename.endsWith(".sch");
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String sch_changed = "", sch_original = ""; 
		
		sch_changed = "/home/mysql/replication/compare_schema/schema_changed/";
		sch_original = "/home/mysql/replication/compare_schema/schema_original/";
		
		compare_schema_main compare = new compare_schema_main(sch_changed,sch_original);
		compare.get_data_changed();
		//getSentMail();
	}
	
	

}
