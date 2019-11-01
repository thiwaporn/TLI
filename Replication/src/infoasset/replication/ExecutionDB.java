package infoasset.replication;

import java.io.IOException;
import java.sql.SQLException;

public class ExecutionDB {

   public ExecutionDB(String configName, String journalFileName)
         throws RepException, IOException, SQLException,
         ClassNotFoundException, InstantiationException, IllegalAccessException {
	   JournalInterface jrnFile = null;
	   if (journalFileName.indexOf("@") >= 0) {
		   JournalMasicFile jrnMasic = JournalMasicFile.newInstance(journalFileName);
		   jrnFile = jrnMasic;
	   } else {
		   JournalTextFile jrnText = JournalTextFile.newInstance(journalFileName);
		   jrnFile = jrnText;
	   }
      MasterJournal master = MasterJournal.newInstance(jrnFile);
      for (;;) {
         Transaction trans = master.nextTransaction();

         Log.getInstance().info("%s", trans);
         if (trans.isInvalid()) {

         }

         if (trans.getType().equals(TransactionType.SHUTDOWN)) {
            Configuration.getInstance().shutdown(trans);
            break;
         } else if (trans.getType().equals(TransactionType.START)) {
            Configuration.createInstance(configName, journalFileName, trans);
         } else if (trans.getType().equals(TransactionType.OPEN)) {            
            Configuration.getInstance().executeTransaction(trans);
         } else if (trans.getType().equals(TransactionType.CLOSE)) {                           
            Configuration.getInstance().executeTransaction(trans);
         } else {
            Configuration.getInstance().executeTransaction(trans);
         }
      }
     
      System.exit(0);
   }

   public static void main(String[] args) {
      String configName = args[0];
      String journalFileName = args[1];
      try {
         new ExecutionDB(configName, journalFileName);
      } catch ( IOException | SQLException
            | ClassNotFoundException | InstantiationException
            | IllegalAccessException e) {
         
         e.printStackTrace();
         System.exit(1);
      } catch (RepException e) {
        Log.getInstance().error("Fail : %s", e.getMessage());
        System.exit(1);
      }
   }

}
