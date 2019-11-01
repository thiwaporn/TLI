package infoasset.replication;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

public class VarConverter {
	public static int EXPORT_HOUR = 21;
	 public static Date getExportLocus(Date dataTime) {
	      Date exportTime = DateUtils.toCalendar(dataTime).getTime();
	      int hour = DateUtils.toCalendar(dataTime).get(Calendar.HOUR_OF_DAY);
	      if (hour >= EXPORT_HOUR) {
	         exportTime = DateUtils.addDays(exportTime, 1);
	      }
	      exportTime = DateUtils.setHours(exportTime, 0);
	      exportTime = DateUtils.setMinutes(exportTime, 0);
	      return exportTime;
	   }
	public static String convert(String text, String journalFileName, Calendar cal) {
		// journalFileName = masic-xx-xxxxxx-xxxx@bucket
		String jrnSeq = StringUtils.substring(journalFileName, 6, 8);
		String jrnDate = StringUtils.substring(journalFileName, 9, 15);
		String jrnTime = StringUtils.substring(journalFileName, 16, 20);
		Matcher matcher = Pattern.compile("(\\[)([\\w\\-]+)(\\])").matcher(text);
		while (matcher.find()) {
			String param = matcher.group();
			String pattern = matcher.group(2);
						
			if (pattern.equals("EXPORT_DATE")) { 
				try {
					Date exportDate = getExportLocus(DateUtils.parseDate(jrnDate + jrnTime, "yyMMddhhmm"));
					pattern = String.format("%tF", exportDate);
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			} else if (pattern.equals("JOURNAL_DATE_TIME")) {
				pattern = jrnDate +"_" + jrnTime;
			} else if (pattern.equals("JOURNAL_DATE")) {
				pattern = jrnDate;
			} else if (pattern.equals("JOURNAL_TIME")) {
				pattern = jrnTime;
			} else if (pattern.equals("JOURNAL_SEQ")) {
				pattern = jrnSeq;
			} else if (pattern.equals("JOURNAL")) {
				pattern = jrnSeq + "_" + jrnDate + "_" + jrnTime;						
			} else {
				pattern = pattern.replaceAll("yy",
						String.valueOf(cal.get(Calendar.YEAR)).substring(2));
				pattern = pattern.replaceAll("MM", StringUtils.leftPad(
						String.valueOf(cal.get(Calendar.MONTH) + 1), 2, '0'));
				pattern = pattern
						.replaceAll("dd", StringUtils.leftPad(
								String.valueOf(cal.get(Calendar.DATE)), 2, '0'));
				pattern = pattern
						.replaceAll("hh", StringUtils.leftPad(
								String.valueOf(cal.get(Calendar.HOUR)), 2, '0'));
				pattern = pattern.replaceAll("mm", StringUtils.leftPad(
						String.valueOf(cal.get(Calendar.MINUTE)), 2, '0'));
				pattern = pattern.replaceAll("ss", StringUtils.leftPad(
						String.valueOf(cal.get(Calendar.SECOND)), 2, '0'));		
			}
			text = StringUtils.replace(text, param, pattern);
		}
		return text;
	}

}
