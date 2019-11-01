package infoasset.replication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import infoasset.schema.MasicSchema;
import infoasset.schema.MasicTable;
import infoasset.schema.SchemaCompiler;



public class FixReplicate {
public static void main(String[] args) {
	String exportDate = "2015-10-27";
	if (args.length > 0) {
		exportDate = args[0];
	}
	try {
		new FixReplicate(exportDate);
		System.out.println("FINISH");
	} catch (SQLException | IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		System.exit(1);
	}
}
private FixReplicate (String exportDate) throws SQLException, IOException {
	System.out.println("1");
	String sqlFx = "SELECT * FROM Replication.VIEW_FIX_TABLE WHERE exportLOCUS = ? ORDER BY sourceDatabase";
	String sqlSc = "SELECT S.serverName  FROM Configuration.SchemaConfig S JOIN Configuration.Server V ON V.serverName = S.serverName WHERE S.schemaName = ? AND V.phaseId = 4";
	String sqlScPath = "SELECT schemaPath FROM Replication.ConfigFile WHERE configName = 'LOCUS'";
	
	String loadFormat = "bash/FULL_LOAD.sh %s %s %s %s %s\n";
	FileOutputStream fixFile = null;
	
	try (Connection conn = Configuration.getConnection();
			ResultSet rsScPath = conn.createStatement().executeQuery(sqlScPath);
			PreparedStatement pstFx = conn.prepareStatement(sqlFx);
			PreparedStatement pstSc = conn.prepareStatement(sqlSc)) {
		if (!rsScPath.next()) {
			System.out.println("Schema path not found");
			System.exit(1);
		}
		String lastSourceDb = null;
		String schemaPath = rsScPath.getString("schemaPath");
		pstFx.setString(1, exportDate);
		ResultSet rsFx = pstFx.executeQuery();
		while (rsFx.next()) {
			
			String sourceDb = rsFx.getString("sourceDatabase");
			String sourceTb = rsFx.getString("sourceTable");
			String targetDb = StringUtils.substringAfter(rsFx.getString("fullTargetTable"),"@");
			
			pstSc.setString(1,  sourceDb);
			ResultSet rsSc = pstSc.executeQuery();
			if (!rsSc.next()) {
				System.out.printf("Schema not found in config [%s@%s]\n", sourceTb, sourceDb);
				continue;
			}
			if (!StringUtils.equals(sourceDb, lastSourceDb)) {
				fixFile = new FileOutputStream("/home/mysql/replication/fix/" + exportDate + "/FIX_" + sourceDb + ".sh", false);
				lastSourceDb = sourceDb;
			}
			String serverName = rsSc.getString("serverName");
			String schemaFileName = schemaPath + serverName + "/" + sourceDb + ".sch";
			File schemaFile = new File(schemaFileName);
			MasicSchema schema = SchemaCompiler.getSchema(schemaFile);
			MasicTable table = schema.getTableByName(sourceTb);
			
			String schemaRootPath = schema.getDataPath();
			String masicFile = (schemaRootPath + table.getTablePath()).replaceAll("////", "//");
			String tableName = table.getTableName();
		//	System.out.printf("Source = %s   Target = %s\n",sourceTb,targetTb);
		//	System.out.printf("MASIC FILE = %s -- tableName = %s\n", masicFile,tableName);
			for (int i = 0; i < tableName.length(); i++) {
				
				if (tableName.charAt(i) == '#' || tableName.charAt(i) == '?') {
				//	System.out.printf("Replace %2d  %c %c\n", i, tableName.charAt(i), sourceTb.charAt(i));
					masicFile = masicFile.replaceFirst("\\" + tableName.substring(i, i+1), sourceTb.substring(i, i + 1));
					
				}
			}
			String textName = sourceDb + ":" + sourceTb + ":" + targetDb + ".txt";
			
			
			String command = String.format(loadFormat, masicFile, schemaFileName, serverName, targetDb, textName);
			fixFile.write(command.getBytes());
			
		}
				
	}
	

}
}
