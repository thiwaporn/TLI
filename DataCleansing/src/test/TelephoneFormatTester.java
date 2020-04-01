package test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import fmtaddr.TelephoneFormatter;

public class TelephoneFormatTester {
	public static void main(String[] args) {
		try {
			new TelephoneFormatTester();
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<String> idList;

	private TelephoneFormatTester() throws IOException, SQLException {
		idList = new ArrayList<>();

		idList.add("0001499833");
		idList.add("0004678707");
		idList.add("0004679320");
		idList.add("0000053202");
		idList.add("0000272755");
		idList.add("0000350007");
		idList.add("0000407315");
		idList.add("0000017883");
		idList.add("0000033312");
		idList.add("0000021078");
		
		idList.add("M00300004892");
		idList.add("M00300005637");
		idList.add("M00300005644");
		idList.add("M00300005940");
		idList.add("M90100001180");
		idList.add("M00300011935");
		idList.add("LE0100057114");
		idList.add("M00100035794");
		idList.add("LE0500095166");
		idList.add("F90200000379");
		

		TelephoneFormatter fmt = new TelephoneFormatter();
		fmt.formatPhone("085-912-4178","");
		fmt.formatPhone("0-92173045,O7161333#425-6", "");
		String url2 = "jdbc:postgresql://10.102.61.64:5432/tlserver2";
		String url8 = "jdbc:postgresql://10.102.61.64:5432/tlserver8";
		Connection conn2 = DriverManager.getConnection(url2, "thiwaporn.kha", "kha191134");
		Connection conn8 = DriverManager.getConnection(url8, "thiwaporn.kha", "kha191134");
		PreparedStatement pst2 = conn2.prepareStatement("SELECT * FROM mstperson.telephone where addressid = ?");
		PreparedStatement pst8 = conn8.prepareStatement("SELECT * FROM mortgage.detcert where policyno||certno = ?");
		for (String addressID : idList) {
			if (addressID.length() == 10) {
				pst2.setString(1, addressID);
				ResultSet rst = pst2.executeQuery();
				while (rst.next()) {
					String phone = rst.getString("phoneno");
					String ext = rst.getString("extension");
			
					ArrayList<String[]> arrPhone = fmt.formatPhone(phone, ext);					
				}
			} else {
				pst8.setString(1, addressID);
				ResultSet rst = pst8.executeQuery();
				if (rst.next()) {
					String phone = rst.getString("telephoneno");
					String ext = "";
			
					ArrayList<String[]> arrPhone = fmt.formatPhone(phone, ext);					
				}
			}

		}
	}

}
