package infoasset.replication;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * อ่าน DNSFile File เพื่อเก็บว่าแต่ละ MASIC Server run schema อะไร
 * เพื่อให้ Schema รู้ว่าต้อง Load schema อะไรบ้าง
 * @author Manisa
 * @since Sep 22, 2014
 */
class DNSFile implements DNS {
	static DNS getInstance() throws RepException, IOException {
		if (dNSFile == null) {
			dNSFile = new DNSFile(RepConfig.getInstance().getFileDNS());			
		}
		return dNSFile;
	}
	private static DNS dNSFile = null;	
	private HashMap<String,ArrayList<String>> dnsMap; // Key=IP Address  Value=Name List
	private RandomAccessFile random;
	private Pattern pattern;
	private Matcher matcher;
	private DNSFile(String fileDNS) throws IOException {
		
		dnsMap = new HashMap<>();

		random = new RandomAccessFile(fileDNS, "r");
		pattern = Pattern.compile("(\\w*)(\\s+IN\\s+A\\s+)(.*)(\\s*)");
		ArrayList<String> nameList;
		for (String line = random.readLine(); line != null; line = random.readLine()) {
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				// 0=Name 1 = IP
				String name = matcher.group(1).trim();
				String ip = matcher.group(3).trim();
				
				nameList = dnsMap.get(ip);
				if (nameList == null) {
					nameList = new ArrayList<>();
					nameList.add(name);
					
					//dnsMap.put(ip, nameList);
				} else {
					nameList.add(name);
					
				}
				//Log.getInstance().trace("add ip to " + ip + " value = " + Arrays.toString(nameList.toArray()));
				dnsMap.put(ip, nameList);
								
			}
		}	
	}
	
	@Override
   public ArrayList<String> getSchemaList(String ipAddress) {
		ArrayList<String> nameList = dnsMap.get(ipAddress);
		Log.getInstance().trace("get schema from " + ipAddress); // + " " + Arrays.toString(nameList.toArray()));
		return nameList;
	}
	
	@Override
   public void listDNS() {
		Iterator<String> ipList = dnsMap.keySet().iterator();
		while (ipList.hasNext()) {
			String ip = ipList.next();
			ArrayList<String> list = dnsMap.get(ip);
			if (list != null) {
				String name = Arrays.toString(dnsMap.get(ip).toArray());			
				Log.getInstance().trace("IP={" + ip + "} Schema={" + name + "}");
			}
		}
	}
	
}
