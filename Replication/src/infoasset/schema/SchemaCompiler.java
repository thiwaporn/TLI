package infoasset.schema;


import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class SchemaCompiler {
	
	public static void main(String[] args) {
		File file = new File("/home/SchemaSVN/WC/DEPLOY/Search/mstpolicy.sch");
		try {
			getSchema(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static MasicSchema getSchema(File file) throws IOException {
		SchemaCompiler compiler = new SchemaCompiler();
		List<String> strList = FileUtils.readLines(file,"tis620");		
		MasicSchema schema = compiler.compile(
				FilenameUtils.getBaseName(file.getName()), strList);
		return schema;
	}

	private SchemaCompiler() {

	}

	private MasicSchema compile(String schemaName, List<String> strList)
			throws IOException {
		MasicSchema schema = MasicSchema.newInstance(schemaName);
		MasicInterface lastElem = schema;		
		MasicElement element = null;			
		Prefix lastPrefix = null;
		for (String str : strList) {
			element = getElement(lastPrefix, str);
		//	System.out.printf("%s  : LastPrefix = %b str = [%s]\n", element, lastPrefix == null,str);
			if (element == null) {
				continue;
			}			
			//if (element.getPrefix() != null && !element.getPrefix().equals(Prefix.COMMENT) ) {
			
			if (!element.getPrefix().equals(Prefix.COMMENT) ) {
				lastPrefix = element.getPrefix();			
			}
			switch (element.getPrefix()) {
			case DATA_PATH:
				schema.setDataPath(element.getCode());
				break;
			case TEMP_PATH:
				schema.setTempPath(element.getCode());
				break;
			case TABLE:
				lastElem = MasicTable.newInstance(element);
				((MasicTable) lastElem).setSchemaName(schemaName);
				schema.addTable((MasicTable)lastElem);
				break;
			case PATH:
				if (schema.getDataPath().endsWith("/")) {
					if (element.getCode().startsWith("/")) {
						schema.getLastTable().setTablePath(element.getCode().substring(1).trim());
					} else {
						schema.getLastTable().setTablePath(element.getCode().trim());
					}
				} else {
					schema.getLastTable().setTablePath(element.getCode().trim());
				}
				break;
			case FIELD:
				lastElem = MasicField.newInstance(element);				
				schema.getLastTable().addField((MasicField)lastElem);
				break;
			case KEY:
				lastElem = MasicKey.newInstance(schema.getLastTable(), element);
				schema.getLastTable().addKey((MasicKey) lastElem);
				break;
			case COMMENT:
				if (element.getComment() != null) {
					lastElem.appendComment(element.getComment());
				}
				break;
			}
		}
		return schema;
	}

	private MasicElement getElement(Prefix lastPrefix, String str) {

		if (StringUtils.isEmpty(str)) {
			return null;
		}
		
		str = str.trim();
		
		
		Prefix prefix = Prefix.findPrefix(lastPrefix, StringUtils.left(str, 1));
		if (prefix == null) {
			return null;
		}
		MasicElement element = new MasicElement();		
		String code =  StringUtils.substringBefore(str.substring(prefix.getPrefix().length()),Prefix.COMMENT.getPrefix());
	
		String comment = StringUtils.substringAfter(str, Prefix.COMMENT.getPrefix());
		if (StringUtils.isEmpty(code)) {
			prefix = Prefix.COMMENT;
		}	
		element.setPrefix(prefix);
		element.setCode(code);
		element.setComment(comment);	
		return element;
	}

}
