package infoasset.schema;

import java.io.File;
import java.io.IOException;

public class TestCompiler {

	

		public static void main(String[] args) {
			File file = new File("/c/schema/table/commontable.sch");
			try {
				MasicSchema msSchema = SchemaCompiler.getSchema(file);
				int tableCount = msSchema.getTableCount();
				for (int i= 0; i < tableCount; i++) {
					MasicTable table = msSchema.getTableAt(i);
					System.out.println(i + " " + table.getTableName());
					if (i == 0) {
						int fcount = table.getFieldCount();
						for (int j = 0; j < fcount; j++) {
							MasicField fld = table.getFieldAt(j);
							System.out.println("\t" + j + " " + fld.getFieldName() + " " + fld.getType());
						}
					}
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

}
