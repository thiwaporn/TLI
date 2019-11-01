package manit;
// **************************************
// Journal file used in masic replication
// make RandomFile more public 
// **************************************
public class JournalFile
{
	private RandomFile file;
	boolean rep;

	// --------------------------
	// open existing journal file
	// --------------------------
	public boolean open(String name, boolean rep)
	{
		this.rep = rep;
		file = Masic.randomOpen(name, Masic.UPDATE);
		return file.lastError() == 0;
	}
	// ---------------------
	// create a journal file
	// ---------------------
	public static JournalFile create(String name)
	{
		Mrecord mfile = manit.Masic.build(name);
		mfile.close();
		JournalFile file = new JournalFile();
		if ( ! file.open(name,true))
			return null;
		return file;
	}
	// ------------------
	// append random file
	// ------------------
	public boolean append(byte[] b, int len)
	{
		return file.append(b, 0, len);
	}
	// ----------------
	// read random file
	// ----------------
	public boolean read(byte[] b, int len)
	{
		int sec = 0;
		int rlen = 0;
		for (;;)
		{
			rlen = file.read(b, 0, len);
			if (rlen != 0)
				break;
			else if (!rep) break;
			if (sec < 5)
				sec++;
			try { Thread.sleep(sec*1000); }
			catch (InterruptedException e) {}
		}
		return rlen == len;
	}
	 public void close()
        {
                file.close();
        }
}
