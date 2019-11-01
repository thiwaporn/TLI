package infoasset.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;

public class DateUtility {

	private static final Locale locale;
	private static final TimeZone timeZone;
	private static final DateFormat dateFormat;
	private static final DateFormat timeFormat;
	private static final DateFormat dateTimeFormat;
	private static final DateFormat dfieldFormat;
	private static final DateFormat mtfieldFormat;
	private static final int[] YMD = {Calendar.YEAR,Calendar.MONTH,Calendar.DATE};
	static {
		locale = new Locale("th", "TH");
		timeZone = TimeZone.getTimeZone("Asia/Bangkok");		
		dateFormat =  new SimpleDateFormat("dd/MM/yyyy", locale);		
		dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", locale);
		dfieldFormat = new SimpleDateFormat("yyyyMMdd", locale);
		timeFormat = new SimpleDateFormat("HH:mm", locale);
	      mtfieldFormat = new SimpleDateFormat("HHmmss", locale);
	}
	public static Locale getLocale() {
		return locale;
	}
	public static Calendar getCalendar() {
		return Calendar.getInstance(timeZone, locale);
	}

	public static Calendar getCalendar(Date date) {
		Calendar cal = getCalendar();
		cal.setTime(date);
		return cal;
	}

	public static Date getSysTime() {
		return getCalendar().getTime();
	}
public static DateFormat getDfieldFormat() {
	return dfieldFormat;
}
	public static DateFormat getDateFormat() {
		return dateFormat;
	}
	public static DateFormat getDateTimeFormat() {
		return dateTimeFormat;
	}
	public static String getDfield(Date dt) {
	   return dfieldFormat.format(dt);
	}
	public static String getMtimefield(Date dt) {
       return mtfieldFormat.format(dt);
    }
	public static String getDisplayDate(Date dt) {
	   return dateFormat.format(dt);
	}
	public static String getDisplayTime(Date dt) {
       return timeFormat.format(dt);
    }
	public static String getDisplayDateTime(Date dt) {
	   return dateTimeFormat.format(dt);
	}
	public static Date setYMD(Date src, Date dst) {
		Calendar srcCal = DateUtils.toCalendar(src);
		Calendar dstCal = DateUtils.toCalendar(dst);
		for (int field : YMD) {
			dstCal.set(field, srcCal.get(field));
		}
		return dstCal.getTime();
	}
	
		

}
