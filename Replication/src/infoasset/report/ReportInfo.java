package infoasset.report;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.mail.Message.RecipientType;

import utility.database.DBException;
import utility.database.SecuredConnect;

public class ReportInfo {
	
	private String subject;
	private String sender;
	private String content;
	private HashMap<RecipientType, ArrayList<String>> recipientList;
	public ReportInfo(ReportType reportType) throws SQLException, IOException, DBException {
		System.setProperty("DB_CONFIG_PATH", "/c/resources/db");
		recipientList = new HashMap<>();
		recipientList.put(RecipientType.TO, new ArrayList<String>());
		recipientList.put(RecipientType.CC, new ArrayList<String>());
		recipientList.put(RecipientType.BCC, new ArrayList<String>());
		String sqlRpt = "SELECT * FROM INFOASSET_REPORT.Report WHERE rptCode = ?";
		String sqlRcp = "SELECT * FROM INFOASSET_REPORT.ReportRecipient WHERE rptCode = ?";
		
		try (Connection conn = SecuredConnect.createConnection("infoasset");
				PreparedStatement pstRpt = conn.prepareStatement(sqlRpt);
				PreparedStatement pstRcp = conn.prepareStatement(sqlRcp)) {
			pstRpt.setString(1, reportType.getCode());
			pstRcp.setString(1, reportType.getCode());
			ResultSet rsRpt= pstRpt.executeQuery();
			if (rsRpt.next()) {
				sender = rsRpt.getString("sender");
				subject = rsRpt.getString("subject");
				content = rsRpt.getString("content");
			}
			ResultSet rsRcp = pstRcp.executeQuery();
			while (rsRcp.next()) {
				RecipientType rcpType = null;
				if (rsRcp.getString("recipientType").equals("TO")) {
					rcpType = RecipientType.TO;
				} else if (rsRcp.getString("recipientType").equals("CC")) {
					rcpType = RecipientType.CC;
				} else if (rsRcp.getString("recipientType").equals("BCC")) {
					rcpType = RecipientType.BCC;
				}
				ArrayList<String> rList = recipientList.get(rcpType);
				if (rList == null) {
					rList = new ArrayList<String>();
					recipientList.put(rcpType, rList);
				}
				rList.add(rsRcp.getString("recipient"));
			}
			
		}
	}
	public String getSender() {
		return sender;
	}
	public String getSubject() {
		return subject;
	}
	public ArrayList<String> getTO() {
		return recipientList.get(RecipientType.TO);
	}
	public ArrayList<String> getCC() {
		return recipientList.get(RecipientType.CC);
	}
	public ArrayList<String> getBCC() {
		return recipientList.get(RecipientType.BCC);
	}
	public String getContent() {
		return content;
	}
}
