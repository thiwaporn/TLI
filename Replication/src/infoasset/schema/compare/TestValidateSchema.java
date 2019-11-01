package infoasset.schema.compare;

public class TestValidateSchema {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String[] path_prod = {"/home/viewy/TLI_workspace/sch_prod/csc.sch"};
		String[] path_dev = {"/home/viewy/TLI_workspace/sch_dev/csc.sch"};
		
		System.out.println("--- Test Validate Schema ---");
		ValidateSchema valid = new ValidateSchema(path_prod, path_dev);
		
		
	}

}
