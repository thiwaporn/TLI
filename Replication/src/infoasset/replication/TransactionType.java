package infoasset.replication;

public enum TransactionType {
	START('S'),
	/**
	 * Shutdown server
	 */
	SHUTDOWN('E'),
	/**
	 * Build ISAM File
	 */
	BUILD('B'),
	/**
	 * Open ISAM File
	 */
	OPEN('O'),
	/**
	 * Close ISAM File
	 */
	CLOSE('C'),
	/**
	 * Purge ISAM File
	 */
	PURGE('P'),
	/**
	 * Insert New Record
	 */
	INSERT('I'),
	/**
	 * Update ISAM File
	 */
	UPDATE('U'),
	/**
	 * Delete ISAM File
	 */
	DELETE('D'),
	/**
	 * Some error
	 */
	ERROR('A'),
	/**
	 * Unknown code
	 */
	UNKNOWN(' ');

	private char code;

	private TransactionType(char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}
	public static TransactionType find(char code) {
		for (TransactionType t : values()) {
			if (t.getCode() == code) {
				return t;
			}
		}
		return TransactionType.UNKNOWN;
	}
}
