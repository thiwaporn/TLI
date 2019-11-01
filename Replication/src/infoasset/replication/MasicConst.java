package infoasset.replication;

/**
 * MASIC Constant
 * 
 * @author Manisa
 * @since Sep 18, 2014
 */
class MasicConst {
	/**
	 * Masic Server Port Number
	 */
	static final int SERVER_PORT = 2150;
	/**
	 * Accumulator Server Port Number
	 */
	static final int ACCUM_PORT = 2160;
	/**
	 * Transaction time stamp data offset
	 */
	static final int OFFSET_TIME = 2;
	/**
	 * Transaction person id data offset
	 */
	static final int OFFSET_ID = 10;
	/**
	 * Transaction file number data offset
	 */
	static final int OFFSET_FNUM = 14;
	/**
	 * Transaction operation code data offset
	 */
	static final int OFFSET_CODE = 16;
	/**
	 * Transaction information length (packet length excluded)
	 */
	static final int INFO_LEN = 15;
	/**
	 * Transaction max buffer size
	 */
	static final int BUFFER_SIZE = 2048;

	/**
	 * Check whether packet length is valid
	 * 
	 * @param dataLength
	 * @return
	 */
	static boolean isValidPacket(int dataLength) {
		//if (dataLength < INFO_LEN || BUFFER_SIZE * 2 < dataLength) {
		if (dataLength < INFO_LEN ) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * get Short value from byte array
	 * 
	 * @param pos
	 * @return
	 */
	static final short getShort(byte[] str, int pos) {
		int d = str[pos + 1] & 0xff;
		d <<= 8;
		d |= str[pos] & 0xff;
		return (short) d;
	}

	/**
	 * get Long value from byte array
	 * 
	 * @param str
	 * @param pos
	 * @return
	 */
	static final long getLong8(byte[] str, int pos) {
		long m = 0;
		for (int i = 7 + pos; i >= pos; i--) {
			m <<= 8;
			m |= str[i] & 0xff;
		}
		return m;
	}
	/**
	 * get Int value from byte array
	 * @param str 
	 * @param pos
	 * @return
	 */
	   static final int getInt(byte[] str, int pos)
	    {
	        int m = 0;
	        for (int i = 0; i < 4; i++)
	        {
	            m <<= 8;
	            m |= str[pos++] & 0xff;
	        }
	        return m;
	    }

}
