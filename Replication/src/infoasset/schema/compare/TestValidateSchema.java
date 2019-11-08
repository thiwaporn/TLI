package infoasset.schema.compare;

public class TestValidateSchema {
	
	private static String OS = System.getProperty("os.name").toLowerCase();

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		String path_prod = "";
		String path_dev = "";
		
		System.out.println(OS);
		
		if (isWindows()) {
			System.out.println("This is Windows");
			
			path_prod = "/home/viewy/TLI_workspace/sch_prod/csc.sch";
			path_dev = "/home/viewy/TLI_workspace/sch_dev/csc.sch";
			
			System.out.println(path_prod);
			System.out.println(path_dev);
			
		} else if (isMac()) {
			System.out.println("This is Mac");
			
			path_prod = "/Users/view_thiwaporn/git/TLI/sch_prod/csc.sch";
			path_dev = "/Users/view_thiwaporn/git/TLI/sch_dev/csc.sch";
			
		} else if (isUnix()) {
			System.out.println("This is Unix or Linux");
		} else if (isSolaris()) {
			System.out.println("This is Solaris");
		} else {
			System.out.println("Your OS is not support!!");
		}
		
		System.out.println("--- Test Validate Schema ---");
		ValidateSchema valid = new ValidateSchema(path_prod, path_dev, true, "/tmp", 10);
		
	}
	
	public static boolean isWindows() {

		return (OS.indexOf("win") >= 0);

	}

	public static boolean isMac() {

		return (OS.indexOf("mac") >= 0);

	}

	public static boolean isUnix() {

		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
		
	}

	public static boolean isSolaris() {

		return (OS.indexOf("sunos") >= 0);

	}

}
