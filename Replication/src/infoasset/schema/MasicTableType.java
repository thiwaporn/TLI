package infoasset.schema;

public enum MasicTableType {
ISAM("isam"), RANDOM("random");
private String code;
private MasicTableType(String code) {
	this.code = code;
}
public String getCode() {
	return code;
}

public static MasicTableType matchCode(String code) {
	for (MasicTableType type : values()) {
		if (type.getCode().equalsIgnoreCase(code)) {
			return type;
		}
	}
	return null;
}
}
