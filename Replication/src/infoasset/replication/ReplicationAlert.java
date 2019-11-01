package infoasset.replication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import utility.mail.PostMan;
import utility.mail.TLMail;

public class ReplicationAlert {
	public static void main(String[] args) {
		String exportDate = "2015-10-27";
		if (args.length > 0) {
			exportDate = args[0];
		}
		try {
			new ReplicationAlert(exportDate);
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ReplicationAlert(String exportDate) throws Exception {
		String sqlFx = "SELECT * FROM Replication.VIEW_FIX_TABLE WHERE exportLOCUS = ? ORDER BY sourceDatabase,sourceTable";
		String sqlRpt = "SELECT * FROM INFOASSET_REPORT.Report WHERE rptCode = '02'";
		String sqlRec = "SELECT * FROM INFOASSET_REPORT.ReportRecipient WHERE rptCode = '02'";
		SimpleDateFormat dateFmt = new SimpleDateFormat("yy-MM-dd");
		Date  eDate = dateFmt.parse(exportDate);
		
		try (Connection conn = Configuration.getConnection(); 
				PreparedStatement pstFx = conn.prepareStatement(sqlFx);
				ResultSet rsRpt = conn.createStatement().executeQuery(sqlRpt);
				ResultSet rsRec = conn.createStatement().executeQuery(sqlRec)) {
			rsRpt.first();
			pstFx.setString(1, exportDate);
			String sender = rsRpt.getString("sender");
			String subject = rsRpt.getString("subject");
			String strCnt  = rsRpt.getString("content");
			
			strCnt = strCnt.replaceAll("<EXPORT_DATE>", String.format("%tF", eDate));
		
			ResultSet rsFx = pstFx.executeQuery();
			if (rsFx.first()) {
				strCnt = strCnt.replaceAll("<RESULT>", "<FONT color=RED><B>มีข้อผิดพลาด</B></FONT> ดังนี้");
				String list = "<TABLE BORDER=1 WIDTH='400'>";
				list += "<TR><TD WIDTH='40'>NO</TD><TD WIDTH='130'><B>Schema</B></TD><TD WIDTH='130'><B>Table</B></TD></TR>";
				rsFx.beforeFirst();
				int count = 0;
				while (rsFx.next()) {
					count++;
					list += "<TR>" + "<TD>" + count + "</TD>"; 
					list += "<TD>" + rsFx.getString("sourceDatabase") + "</TD>";
					list += "<TD>" + rsFx.getString("sourceTable") + "</TD></TR>";
				}
				list+= "</TABLE>";
				strCnt = strCnt.replaceAll("<ERROR_TABLE>", list);
			} else {
				strCnt = strCnt.replaceAll("<RESULT>", "<FONT color=GREEN><B>สำเร็จ</B></FONT>");
				strCnt = strCnt.replaceAll("<ERROR_TABLE>", "");
			}
			System.out.println(strCnt);
			TLMail tlmail = new TLMail();
			tlmail.setSender(sender);
			tlmail.setSubject(subject);
			while (rsRec.next()) {
				if (rsRec.getString("recipientType").equalsIgnoreCase("TO")) {
					tlmail.addReceiver(rsRec.getString("recipient"));
				} else if (rsRec.getString("recipientType").equalsIgnoreCase("CC")) {
					tlmail.addCCReceiver(rsRec.getString("recipient"));
				} else {
					tlmail.addBccReceiver(rsRec.getString("recipient"));
				}
			}
			tlmail.setContent(strCnt);
			PostMan post = new PostMan();
			post.sendGoogleMail(tlmail);
		}
	} 
	
}