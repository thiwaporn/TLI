package infoasset.replication;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MasterJournal implements Master {
	public static MasterJournal newInstance(JournalInterface jrnFile) throws FileNotFoundException {
		return new MasterJournal(jrnFile);
	}
	private JournalInterface jrnFile;
	private byte[] header;
	private byte[] data;
	private int transactionCount;

	private MasterJournal(JournalInterface jrnFile) throws FileNotFoundException {		
		this.jrnFile = jrnFile;
		header = new byte[MasicConst.INFO_LEN + 2];
		transactionCount = 0;
	}

	@Override
	public
	Transaction nextTransaction() throws RepException, IOException {
		Transaction trans = null;
		if (!jrnFile.read(header, header.length)) {
			trans = Transaction.newInstance(TransactionType.ERROR);			
		} else {
			short dataLength = MasicConst.getShort(header, 0);
			
			
			if (!MasicConst.isValidPacket(dataLength)) {
				
				throw new RepException("Invalid Packet Length");
			}
			
			trans = Transaction.newInstance(TransactionType.UNKNOWN);
			
			trans.setTime(MasicConst.getLong8(header, MasicConst.OFFSET_TIME));
			trans.setFileId(MasicConst.getShort(header, MasicConst.OFFSET_FNUM));
			trans.setType(TransactionType
					.find((char) (header[MasicConst.OFFSET_CODE] & 0xff)));
			if (trans.getType() == TransactionType.UNKNOWN) {
				throw new RepException("Invalid Operation Code");
			}
			
			trans.setDataLength(dataLength - MasicConst.INFO_LEN + 1);
			data = new byte[dataLength - MasicConst.INFO_LEN + 1];
			
			int dLen =  data.length;
			int dIdx = 0;
			while (dLen > 0) {
				int readLen = dLen;
				if (dLen > MasicConst.BUFFER_SIZE) {
					readLen = MasicConst.BUFFER_SIZE;
				}
				byte[] tempData = new byte[readLen];
				if (!jrnFile.read(tempData, readLen)) {
					throw new RepException("Data read error at " + transactionCount);
				}
				for (int i = 0; i < readLen; i++) {
					data[dIdx + i] = tempData[i];					
				}
				dLen -= readLen;
				dIdx += readLen;
				
			}
			/*
			if (!jrnFile.read(data, data.length)) {
				throw new RepException("Data read error at " + transactionCount);
			}
			*/
			
			trans.setData(data);
			
		}

		return trans;

	}

	@Override
	public String getMasterName() {
	return jrnFile.getJournalName();
	}
	
	

}
