package infoasset.schema;

public class MasicElement {

	private String comment;
	private Prefix prefix;
	private String code;
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Prefix getPrefix() {
		return prefix;
	}

	public void setPrefix(Prefix prefix) {
		this.prefix = prefix;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "MasicElement [prefix=" + prefix + ", code=" + code + "]";
	}
	

}
