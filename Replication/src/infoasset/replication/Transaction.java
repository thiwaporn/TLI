package infoasset.replication;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import manit.M;

public class Transaction {
   public static Transaction newInstance(TransactionType type) {
      Transaction trans = new Transaction();
      trans.setType(type);
      return trans;
   }

   private TransactionType type;
   private boolean checkData = false;
   private boolean invalidData = false;
   private int dataLength;
   private int fileId;
   private long time;
   private byte[] data;
   private byte[] originalData;
   private String fullTargetTable;

   public TransactionType getType() {
      return type;
   }

   void setType(TransactionType type) {
      this.type = type;
      checkData = type.equals(TransactionType.INSERT)
            || type.equals(TransactionType.UPDATE)
            || type.equals(TransactionType.DELETE);
   }

   public int getDataLength() {
      return dataLength;
   }

   void setDataLength(int dataLength) {
      this.dataLength = dataLength;
   }

   public int getFileId() {
      return fileId;
   }

   void setFileId(int fileId) {
      this.fileId = fileId;
   }

   public long getTime() {
      return time;
   }

   public void setTime(long time) {
      this.time = time;
   }

   public byte[] getData() {
      return data;
   }

   public byte[] getOriginalData() {
      return originalData;
   }

   public String getTableName() {
      switch (type) {
         case INSERT:
         case UPDATE:
         case DELETE:
            return FileIndex.getInstance().getFileName(fileId);
         default:
            return StringUtils.trim(getDataAsString());
      }
   }

   void setData(byte[] originalData) {
      this.originalData = originalData;
      data = M.ktos(originalData);

      // data = new byte[originalData.length];

      invalidData = false;

      for (int i = 0; i < data.length && i < dataLength; i++) {
         if (isInvalidByte(data[i])) {
            invalidData = true;
            data[i] = ' ';
         }
      }

      /*
       * if (checkData && invalidData) { for (int i = 0; i < data.length; i++) {
       * if (isInvalidByte(originalData[i])) {
       * System.out.printf("%s|%s|%3d|%2d|%2d\n", getTableName(), getTimeStr(),
       * i, originalData[i], M.ktos(originalData[i])); } } // System.exit(0); }
       */

   }

   public boolean isInvalid() {
      return invalidData && checkData;
   }

   void setDataAsString(String str) {
      this.data = str.getBytes();
   }

   public String getDataAsString() {
      return StringUtils.chomp(new String(data));
   }

   private int getRecordLength() {
      if (getType().equals(TransactionType.UPDATE)) {
         return dataLength / 2;
      } else if (getType().equals(TransactionType.DELETE)
            || getType().equals(TransactionType.INSERT)) {
         return dataLength;
      } else {
         return -1;
      }
   }

   public String getOldRecord() {
      int recordLength = getRecordLength();
      if (recordLength < 0) {
         return null;
      }
      byte[] cdata = new byte[recordLength];

      for (int i = 0; i < cdata.length; i++) {
         cdata[i] = data[i];
      }
      return M.stou(cdata);

   }

   public String getNewRecord() {
      int recordLength = getRecordLength();
      if (recordLength < 0) {
         return null;
      }
      if (type.equals(TransactionType.DELETE)) {
         return null;
      }
      byte[] cdata = new byte[recordLength];
      int start = getType().equals(TransactionType.INSERT) ? 0 : recordLength;
      for (int i = 0; i < recordLength; i++) {
         cdata[i] = data[start + i];
      }
      return M.stou(cdata);

   }

   public String getTimeStr() {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(getTime());
      String dateStr = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
      return dateStr;

   }

   private boolean isInvalidByte(byte b) {

      char sc = M.stou(b);
      if (sc < 32 && sc != 10 && sc != 12 && sc != 13) {
         invalidData = true;
         return true;
      }
      return false;

   }
public void printLog(String caller) {
    
	 String log = String.format("%4$-6s %1$-10s %2$tF %2$tT %3$s\n", getType(), new Date(getTime()),  StringUtils.defaultString(getTableName(),"-"), caller);
	 System.out.print(log);
}
   @Override
   public String toString() {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(time);
      String dateStr = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
      String format = "Transaction [type=%-8s, dataLength=%04d, time=%s, fileId=%06d, fileName=%-20.20s, str=%-25.25s]";
      String text = "";
      String fileName = "";

      if (getType().equals(TransactionType.CLOSE)
            || getType().equals(TransactionType.PURGE)) {
         fileName = FileIndex.getInstance().getFileName(fileId);
      } else if (getType().equals(TransactionType.INSERT)) {
         fileName = FileIndex.getInstance().getFileName(fileId);
         text = getNewRecord();
      } else if (getType().equals(TransactionType.DELETE)) {
         fileName = FileIndex.getInstance().getFileName(fileId);
         text = getOldRecord();
      } else if (getType().equals(TransactionType.UPDATE)) {
         fileName = FileIndex.getInstance().getFileName(fileId);
         text = getOldRecord();
      } else {
         fileName = getDataAsString();
      }
      text = String.format(format, type, dataLength, dateStr, fileId, fileName,
            text);

      return text;
   }

   public String getFullTargetTable() {
      return fullTargetTable;
   }

   public void setFullTargetTable(String fullTargetTable) {
      this.fullTargetTable = fullTargetTable;
   }

}
