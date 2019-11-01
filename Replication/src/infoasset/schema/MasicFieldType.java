package infoasset.schema;

public enum MasicFieldType {
	TEXT("TEXT", "text"), NUMBER("NUMBER", "number");
	private final String code;
	private final String script;

	private MasicFieldType(String code, String script) {
		this.code = code;
		this.script = script;
	}

	public String getCode() {
		return code;
	}

	public String getScript() {
		return script;
	}

	public static MasicFieldType matchCode(String code) {
		for (MasicFieldType type : values()) {
			if (type.getCode().equalsIgnoreCase(code)) {
				return type;
			}
		}
		return null;
	}
	   public static MasicFieldType matchScript(String script) {
	        for (MasicFieldType type : values()) {
	            if (type.getScript().equalsIgnoreCase(script)) {
	                return type;
	            }
	        }
	        return null;
	    }
}
