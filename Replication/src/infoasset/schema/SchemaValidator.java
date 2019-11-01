package infoasset.schema;

public class SchemaValidator {
	public static boolean validateSchemaName(String schemaName) {
		return schemaName.matches("[\\w]+");
	}
	
	public static boolean validateSchemaPath(String path) {
		return path.matches("\\/(\\w+\\/)*\\w+/");
	}
	public static boolean validateTableName(String tableName) {
		return tableName.matches("\\/(\\w+\\/)*\\w+/");
	}
	public static boolean validateTablePath(String tablePath) {
		return tablePath.matches("[\\w#?\\.]+");
	}

}
