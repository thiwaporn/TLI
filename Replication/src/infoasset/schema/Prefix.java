package infoasset.schema;

import org.apache.commons.lang3.StringUtils;

public enum Prefix {
	DATA_PATH("%"), TEMP_PATH("&"), TABLE("@"), KEY("("), COMMENT(";"),PATH(""), FIELD("");
	private String prefix;

	private Prefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	public static Prefix findPrefix(Prefix lastPrefix, String prefix) {
		
		if (StringUtils.isEmpty(prefix)) {
			return null;
		}
		for (Prefix pf : values()) {
			if (pf.getPrefix().equals(prefix)) {
				return pf;
			}
		}
		if (lastPrefix.equals(TABLE)) {
			return PATH;
		} else {
			return FIELD;
		}
		
	}
}
