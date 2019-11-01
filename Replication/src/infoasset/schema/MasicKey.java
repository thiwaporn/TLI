package infoasset.schema;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class MasicKey implements MasicInterface {
	public static MasicKey newInstance(MasicTable table, MasicElement element) {
		MasicKey key = new MasicKey();
		String[] segments = StringUtils.substringBefore(element.getCode(),")").trim().split("[\\s,]+");
		String property = StringUtils.substringAfter(element.getCode(), ")");
		key.setDuplicate(property.contains("dup"));
		key.setModify(property.contains("mod"));
		for (String seg : segments) {			
			for (int i = 0; i < table.getFieldCount(); i++) {
				if (table.getFieldAt(i).getFieldName().equals(seg)) {
					key.addSegment(table.getFieldAt(i));					
					break;
				}
			}			
		}
		key.appendComment(element.getComment());
		return key;
	}
	public static MasicKey newInstance(boolean duplicate, boolean modify) {
		MasicKey key = new MasicKey();
		key.setDuplicate(duplicate);
		key.setModify(modify);
		return key;
	}
	
	private boolean duplicate;
	private boolean modify;
	private ArrayList<MasicField> fieldList;
	private String comment;
	private MasicKey() {
		fieldList = new ArrayList<>();
	}
	public boolean isDuplicate() {
		return duplicate;
	}
	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}
	public boolean isModify() {
		return modify;
	}
	public void setModify(boolean modify) {
		this.modify = modify;
	}
	public ArrayList<MasicField> getFieldList() {
		return fieldList;
	}
	public void setFieldList(ArrayList<MasicField> fieldList) {
		this.fieldList = fieldList;
	}
	public void addSegment(MasicField field) {
		fieldList.add(field);
	}
	public int getSegmentCount() {
		return fieldList.size();
	}
	public MasicField getFieldAt(int index) {
		return fieldList.get(index);
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
	public boolean isKeyField(String fieldName) {
		for (MasicField field : fieldList) {
			if (field.getFieldName().equals(fieldName)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
	@Override
	public String toString() {
		String segment = "";
		for (int i = 0; i < fieldList.size(); i++) {
			segment += fieldList.get(i).getFieldName() + " ";
		}
		return "MasicKey [duplicate=" + duplicate + ", modify=" + modify + ", segment=" + segment + "]";
	}
	
}
