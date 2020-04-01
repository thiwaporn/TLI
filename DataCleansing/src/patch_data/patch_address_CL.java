package patch_data;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.Properties;

import fmtaddr.*;

public class patch_address_CL {

	String url = "";
	String user = "";
	String pass = "";
	Properties props = null;
	
	public patch_address_CL() throws Exception {
		
		// TODO Auto-generated constructor stub
		
		this.url = "jdbc:postgresql://10.102.47.35:5432/customer_prep";
		this.user= "architect.informatica";
		this.pass = "Architect@2018";
		
		this.props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", pass);
		
	}
	
	public String select_sub_district(String province, String district, String sub_district) {
		
		String sql = "select * from lookup.sub_district where province ='" + province + "' and district = '" + district + "' and sub_district = '" + sub_district + "'";
		String sub_district_code = "";
		String zip_code = "";
		
		try(Connection conn = DriverManager.getConnection(url, props);
				PreparedStatement pstmt = conn.prepareStatement(sql)){
			
			ResultSet result = pstmt.executeQuery();
			while(result.next()) {
				sub_district_code = result.getString(1);
				zip_code = result.getString(2);
				
//				System.out.println(sub_district_code + "|" + zip_code);
			}
			
		}catch(SQLException e) {
			System.err.format("SQL State : %s\n%s", e.getSQLState(), e.getMessage());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return sub_district_code;
	}
	
	public void select_mortgage_detcert() {
		
		//String sql = "select * from mortgage.detcert where policyno = 'L034' and certno = '03336642' limit 100";
		String sql = "select * from mortgage.detcert where policyno not in ('9006', 'L005') limit 1000000";
		
		try(Connection conn = DriverManager.getConnection(url, props);
				PreparedStatement pstmt = conn.prepareStatement(sql)){
			
			ResultSet result = pstmt.executeQuery();
			
			String path = "C:\\Users\\user\\Desktop\\patch_address_CL_0_1000000.csv";
			FileWriter writer ;
			File file = new File(path);
			writer = new FileWriter(file, false);
			writer.write("policy_no|cert_no|check_place|place|moo|sub_district|district|province|sub_district_code|zipcode\n");
			while(result.next()) {
			
				String policyno = result.getString(1);
				String certno = result.getString(2);
				String address1 = result.getString(4); 
				String address2 = result.getString(5);
								
				LineAddress address = new LineAddress();
				address.setAddrLine1(address1);
				address.setAddrLine2(address2);
				
				FieldAddressFormatter fmt_addr = new FieldAddressFormatter();
				FieldAddress field_addr = fmt_addr.formatLineAddress(address);
				
				String policy_no = policyno.trim();
				String cert_no = certno.trim();
				String toString = field_addr.toString().trim();
				String check_place = field_addr.get("check_place").trim();
				String place = field_addr.get("place").trim();
				String moo = field_addr.get("moo").trim();
				String province = field_addr.get("province").trim();
				String district = field_addr.get("district").trim();
				String sub_district = field_addr.get("sub_district").trim();
				String zipcode = field_addr.get("zipcode").trim();
				String sub_district_code = "";
				String pattern = "";
				
				if(province != "" && district != "" && sub_district != "") {sub_district_code = select_sub_district(province, district, sub_district);}
				if(sub_district_code != "") {
					
					pattern = policy_no + "|" + cert_no + "|" + check_place + "|" + place + "|" + moo + "|" + sub_district + "|" + district + "|" + province + "|" + sub_district_code + "|" + zipcode;
					System.out.println(pattern);
					writer.write(pattern + "\n");
				}
			}
			writer.close();
			System.out.println("Write success!");
			
		}catch(SQLException e) {
			System.err.format("SQL State : %s\n%s", e.getSQLState(), e.getMessage());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		patch_address_CL patch = new patch_address_CL();
		patch.select_mortgage_detcert();
	}
}
