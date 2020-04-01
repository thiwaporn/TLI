package fmtaddr;
public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TelephoneFormatter fmt = new TelephoneFormatter();
		fmt.formatPhone("081849895@196,0894949151","196");
		fmt.formatPhone("081849895:196,0894949151","196");
		
		
		System.out.println("======================================================");
		
		/*
		String line1 = "8/3บ้านเสียว หมู่9";
		String line2 = "ต.วังชัย อ.น้ำพอง จ.ขอนแก่น 40140";
		LineAddress line = new LineAddress();
		line.setAddrLine1(line1);
		line.setAddrLine2(line2);
		FieldAddressFormatter addr = new FieldAddressFormatter();
		FieldAddress fldAddr = addr.formatLineAddress(line);
		System.out.println(fldAddr.toString());
		System.out.println(fldAddr.getFormatStr());
		*/
		
		EmailFormatter fmtEmail = new EmailFormatter();
		fmtEmail.formatemail("arnon@hotmail.com,rojweera@gmail.com");
		fmtEmail.formatemail("nawaphon-2516@hotmail.co,th");
		fmtEmail.formatemail("domehandsome4@hot,ail.com");
		fmtEmail.formatemail("psm.broker@hotmail.co,");
		fmtEmail.formatemail("orca2102@gmail.com,orcaisme.2102@gmail.com");
		fmtEmail.formatemail("ิำืิำืbenben2508@jot,ail.com");
		fmtEmail.formatemail("s_surang@hotmail.co,");
		//fmtEmail.formatemail("");
		//fmtEmail.formatemail("");
		//fmtEmail.formatemail("");
		
		
	}

}
