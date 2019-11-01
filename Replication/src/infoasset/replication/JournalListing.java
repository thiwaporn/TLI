package infoasset.replication;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;

import infoasset.utils.DateUtility;

public class JournalListing {
   public static void main(String[] args) {
      try {
         String dateStr = "";
         String serverSeq = args[0];
         Date exportDate = DateUtility.getSysTime();
         if (args.length > 1) {            
            dateStr = args[1];
            exportDate = DateUtils.parseDate(dateStr, "yyMMdd");
         } else {
            
            if (DateUtils.toCalendar(exportDate).get(Calendar.HOUR_OF_DAY) < VarConverter.EXPORT_HOUR) {
             exportDate =   DateUtils.addDays(exportDate, -1);
            }            
            dateStr = toDateStr(exportDate);            
         }         
         new JournalListing(serverSeq, exportDate);
         System.exit(0);
      } catch (ParseException | IOException | InterruptedException | RepException e) {
         System.err.println(e.getMessage());
         System.exit(1);
      } finally {
         
      }
   }

   public JournalListing(String serverSeq, Date exportDate) throws IOException, InterruptedException, RepException     {
      System.out.println("List " + exportDate);
    String user = System.getenv("JRN_USER");
    String host = System.getenv("JRN_HOST");
    String path = System.getenv("JRN_PATH");
   
    String cmd = "ssh " + host + " -l " + user + " ls " + path + "masic-"+serverSeq +"-";
    //String cmd = "ssh " + host + " -l " + user + " ls " + path + "masic-03-";
    String yesterday = toDateStr(DateUtils.truncate(DateUtils.addDays(exportDate, -1), Calendar.HOUR));
    
    String exportDay = toDateStr(DateUtils.truncate(DateUtils.addDays(exportDate, 0), Calendar.HOUR));
 
    String nextDay = toDateStr(DateUtils.truncate(DateUtils.addDays(exportDate, +1), Calendar.HOUR));
    String[] cmdStr = ArrayUtils.EMPTY_STRING_ARRAY;
    cmdStr = ArrayUtils.add(cmdStr, cmd + exportDay + "-" + "2[1234]*.txt");
    cmdStr = ArrayUtils.add(cmdStr, cmd + nextDay + "-*.txt");
    cmdStr = ArrayUtils.add(cmdStr, cmd + yesterday + "-" +  "2[1234]*.txt");
    cmdStr = ArrayUtils.add(cmdStr, cmd + exportDay + "-" + "[01]*.txt");
    cmdStr = ArrayUtils.add(cmdStr, cmd + exportDay + "-" + "20*.txt");

    Process restart = getRuntimeProcess(cmdStr[0]);
    
    if (restart.exitValue() == 2) {
       restart = getRuntimeProcess(cmdStr[1]);
       if (restart.exitValue() != 0) {
          throw new RepException("MASIC has not been restarted", exportDay);
       }
    } else if (restart.exitValue() != 0) {       
       throw new RepException(getString(restart.getErrorStream()));
    }
  
    for (int i = 2; i < cmdStr.length ; i++) {
       Process masic = getRuntimeProcess(cmdStr[i]);
       if (masic.exitValue() == 0) {          
          String str = getString(masic.getInputStream());
          str = str.replaceAll(path, "");
          str = str.replaceAll(".txt", "@" + host);
          System.out.println(str);
       }
    }   
    
   }
   private String getString(InputStream strm) throws IOException {
      byte[] read = new byte[strm.available()];
      strm.read(read);
      String str = new String(read);
      return str;
   }
   private Process getRuntimeProcess(String command) throws IOException, InterruptedException {
      System.out.printf("Exec %s\n", command);
      Runtime rt = Runtime.getRuntime();
      Process pcs = rt.exec(command);
      pcs.waitFor();
      return pcs;
   }
   private static String toDateStr(Date date) {
      Calendar cal = DateUtils.toCalendar(date);
      String str = String.format("%02d%02d%02d", cal.get(Calendar.YEAR) % 100, cal.get(Calendar.MONTH)+1, cal.get(Calendar.DATE));
      return str;
   }
}
