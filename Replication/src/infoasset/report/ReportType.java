package infoasset.report;

public enum ReportType {
LOCUS_TABLE_STRUCTURE_CHANGE("01"),
LOCUS_DAILY_RESULT("02"),
LOCUS_DAILY_DETAIL("03"),
TEST_SVN("04");
	private String code;
	private  ReportType(String code) {
		this.code = code;
	}
	public String getCode() {
		return code;
	}
}
