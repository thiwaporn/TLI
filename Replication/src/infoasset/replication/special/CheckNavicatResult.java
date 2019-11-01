package infoasset.replication.special;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckNavicatResult {
	public static void main(String[] args) {
		String fileName = "/tmp/service.txt";
		try {
			new CheckNavicatResult(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public CheckNavicatResult(String fileName) throws IOException {
		File file = new File(fileName);
		try (RandomAccessFile rdf = new RandomAccessFile(file, "r")) {
			String line = rdf.readLine();
			boolean found = false;
			Matcher matcher = null;
			Pattern pattern = Pattern.compile("(.+Time elapsed for )(\\w+)");
			for (; line != null; line = rdf.readLine()) {

				if (line.matches(".+Insert\\[[1-9].+") || line.matches(".+Update\\[[1-9].+")
						|| line.matches(".+Delete\\[[1-9].+")) {
					System.out.println(line);
					found = true;
					continue;
				}
				if (found) {

					matcher = pattern.matcher(line);
					if (matcher.find()) {
						System.out.printf("%-30s\n", matcher.group(2));

					}
					found = false;
				}
			}
		}
	}

}
