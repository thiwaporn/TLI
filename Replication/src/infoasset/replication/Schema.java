package infoasset.replication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import infoasset.schema.MasicSchema;
import infoasset.schema.SchemaCompiler;

/**
 * Load schema file (*.sch) เข้ามาเก็บไว้ใน Memory 
 * สำหรับดึง structure ของข้อมูล
 * @author Manisa
 * @since Sep 22, 2014
 */
class Schema {
	static Schema getInstance() {
		if (schema == null) {
			schema = new Schema();			
		}
		return schema;
	}
	private static Schema schema = null;
	private ArrayList<String> nameList;
	private HashMap<String, MasicSchema> schemaList;
	private Schema() {
		schemaList = new HashMap<>();
		nameList = new ArrayList<>();
	}
	public boolean addSchema(String schemaName) throws RepException, IOException {
	    File schRoot = new File(RepConfig.getInstance().getDirSchema());
	    return addSchema(schRoot, schemaName);
	}
	public boolean addSchema(File schRoot, String schemaName) throws RepException, IOException {						
		IOFileFilter filter = FileFilterUtils.nameFileFilter(schemaName + ".sch");
		Iterator<File> fileList = FileUtils.iterateFiles(schRoot,filter,TrueFileFilter.INSTANCE);
		while (fileList.hasNext()) {				
			File file = fileList.next();			
			MasicSchema schema = SchemaCompiler.getSchema(file);
			schemaList.put(schemaName, schema);
			nameList.add(schemaName);
			return true;
		}
		return false;
	}
	public List<String> getSchemaNames() {
		return nameList;
	}
	public MasicSchema getSchema(String schemaName) {
		return schemaList.get(schemaName);
	}
	public void addSchema(MasicSchema sch) {
		schemaList.put(sch.getSchemaName(), sch);
		nameList.add(sch.getSchemaName());		
	}
}
