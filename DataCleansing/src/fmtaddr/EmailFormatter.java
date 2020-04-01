package fmtaddr;

import java.util.ArrayList;

public class EmailFormatter {

	public EmailFormatter() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<String> formatemail(String email){
		email = email == null ? "" : email.trim();
		email = email.replaceAll(",com", ".com");
		email = email.replaceAll("@com", ".com");
		email = email.replaceAll("hot,ail.com ", "hotmail.com");
		email = email.replaceAll("hotmail.co,", "hotmail.com");
		email = email.replaceAll("hot,mail.com", "hotmail.com");
		email = email.replaceAll("hot,ail.com", "hotmail.com");
		email = email.replaceAll("jot,ail.com", "hotmail.com");
		email = email.replaceAll("gmai,ll.com", "gmail.com");
		email = email.replaceAll("g,ail.com", "gmail.com");
		email = email.replaceAll("tot.co,th", "tot.co.th");
		email = email.replaceAll("mistine,co.th", "mistine.co.th");
		email = email.replaceAll(".co,th", ".co.th");
		email = email.replaceAll("[ก-ฮ ิำืิำืฦ]+", "");
		
		ArrayList<String> emailList = new ArrayList<>();
		String[] arrEmail =  email.split("\\,");
		for (String strEmail : arrEmail) {
			String data = strEmail;
			
			emailList.add(data);
		}
		
		printResult(emailList);
		return emailList;
	}
	
	private void printResult(ArrayList<String> emailList) {
		
		for (int i = 0; i < emailList.size(); i++) {
			String fmtEmail = emailList.get(i);
			System.out.println((i+1) + "\t" + fmtEmail);
		}

	}
}
