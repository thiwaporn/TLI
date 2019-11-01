package infoasset.schema;

public enum SchemaError {
	EXCEED_SCHEMA_LENGTH("Exceed schema buffer length"),
	EXCEED_KEY_LENGTH("Exceed key length (40 bytes)"),
	UNEXPECTED_END_OF_INPUT("Unexpected end of input"),
	SKIPPED_FIELD_NAME("Skipped field name"),
	INVALID_CHAR_IN_FIELD("Unexpected character in field attribute"),
	UNEXPECTED_END_OF_LINE("Unexpected end of line"),
	TOO_LARGE_PARAMTER("Parameter is too large"),
	TOO_LARGE_OCCURRENCE("Occurrence is too large"),
	UNKNOWN_FIELD_LENGTH("Unknown field length"),
	UNKNOWN("");
	private final String message;
	private SchemaError(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	/*
	 
	
	 36   "07:parameter too big",
	 37   "08:occurrence too large",
	 38   "09:unknown field type",
	 39   "10:this type needs length",
	 40   "11:invalid field length",
	 41   "12:invalid precision",
	 42   "13:text precision > 0 is invalid",
	 43   "14:decimal point too long",
	 44   "15:expect end of layout",
	 45   "16:schema line too long",
	 46   "17:invalid logical name",
	 47   "18:invalid path name",
	 48   "19:invalid field name",
	 49   "20:output creation failed",
	 50   "21:inconsistent name mask",
	 51   "22:invalid file type",
	 52   "23:expected to see @",
	 53   "24:invalid key field",
	 54   "25:expect a key flag",
	 55   "26:invalid key flag",
	 56   "27:invalid field info",
	 57   "28:segment not found",
	 58   "29:invalid temp mask"

*/
}
