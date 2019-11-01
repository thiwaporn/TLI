package infoasset.schema.compare;

import java.util.ArrayList;

public class Test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		SchemaDiff2 compare = new SchemaDiff2("m");

		@SuppressWarnings("unused")
		String sender = "thiwaporn.kha@thailife.com";
		
		ArrayList<String> receiver = new ArrayList<>();
		receiver.add("manisa@thailife.com");
		receiver.add("thiwaporn.kha@thailife.com");
		
		ArrayList<String> ccReceiver = new ArrayList<>();
		ccReceiver.add("thiwaporn.kha@thailife.com");
		
		//@SuppressWarnings("unused")
		//SchemaDiff2 mail = new SchemaDiff2("m");
		//mail.getSentMail(sender, receiver, ccReceiver);
		
		//SchemaDiff2 script = new SchemaDiff2("s");

	}
}
