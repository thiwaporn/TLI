package infoasset.schema;



import org.apache.commons.lang3.StringUtils;

/**
 * MASIC Field
 * @author Manisa
 * @since Sep 18, 2014
 */
public class MasicField implements MasicInterface  {
	public static MasicField newInstance(MasicElement element) {		
		MasicField field = new MasicField();
		String[] str = element.getCode().split("[\\s,]+");
		field.setFieldName(str[0]);
		field.setType(MasicFieldType.matchCode(str[1]));
		field.setLength(Integer.valueOf(str[2]));
		field.setScale(0);
		if (str.length > 3) {
			field.setScale(Integer.valueOf(str[3]));
		}
		field.appendComment(element.getComment());
		return field;
	}
	public static MasicField newInstance(String fieldName,MasicFieldType type, int length, int scale) {
		MasicField field = new MasicField();
		field.setFieldName(fieldName);
		field.setType(type);
		field.setLength(length);
		field.setScale(scale);
		return field;
	}
	
	private String fieldName;
	private MasicFieldType type;
	private int length;
	private int scale;
	private String comment;
	private MasicField() {
	
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public MasicFieldType getType() {
		return type;
	}
	public void setType(MasicFieldType type) {
		this.type = type;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getScale() {
		return scale;
	}
	public void setScale(int scale) {
		this.scale = scale;
	}
@Override
	public String getComment() {
		return comment;
	}
@Override
	public void setComment(String comment) {
		this.comment = StringUtils.chomp(comment);
	}

	@Override
	public void appendComment(String comment) {
		if (this.comment == null) {
			setComment(comment);
		} else {
			setComment(getComment() + "\n" + comment);
		}		
	}
	@Override
	public String toString() {
		return "MasicField [fieldName=" + fieldName + ", type=" + type
				+ ", length=" + length + ", scale=" + scale + "]";
	}

	
}
