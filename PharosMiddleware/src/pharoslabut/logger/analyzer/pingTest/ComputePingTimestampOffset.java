package pharoslabut.logger.analyzer.pingTest;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * This program computes the offset in milliseconds between the ping timestamp 
 * and the java timstamp.  
 * 
 * When the ping was performed on April 25, 2012 GMT, the offset is 1335312000000 ms.
 * When the ping was performed on May 24, 2012 GMT, the offset is 1337817600000 ms.
 * When the ping was performed on May 25, 2012 GMT, the offset is 1337904000000 ms.
 * When the ping was performed on May 30, 2012 GMT, the offset is 1338336000000 ms.
 * When the ping was performed on May 31, 2012 GMT, the offset is 1338422400000 ms.
 * When the ping was performed on June 1, 2012 GMT, the offset is 1338508800000 ms.
 * 
 * @author Chien-Liang Fok
 */
public class ComputePingTimestampOffset {

	public ComputePingTimestampOffset() {
//		String[] ids = TimeZone.getAvailableIDs();
//		for (int i=0; i < ids.length; i++) {
//			System.out.println(ids[i]);
//		}
		TimeZone tz = TimeZone.getTimeZone("GMT");
		GregorianCalendar cal = new GregorianCalendar(tz);
		//cal.setTimeInMillis(0);
		
		cal.set(1970, GregorianCalendar.JANUARY, 1, 0, 0);
		Date date1 = cal.getTime();
		
		//cal.set(2012, GregorianCalendar.APRIL, 25, 0, 0);
		//cal.set(2012, GregorianCalendar.MAY, 30, 0, 0);
		cal.set(2012, GregorianCalendar.JUNE, 1, 0, 0);
		Date date2 = cal.getTime();
		
		//long startDate = cal.getTime();
		//cal.set(2012, GregorianCalendar.APRIL, 25, 0, 0);
//		System.out.println("" + cal.getTimeInMillis());
		DateFormat dateFormat = DateFormat.getDateInstance();
		dateFormat.setTimeZone(tz);
		System.out.println("date1: " + dateFormat.format(date1) + ", date2: " + dateFormat.format(date2)
				+ " delta: " + (date2.getTime() - date1.getTime()));
	}
	
	public static void main(String[] args) {
		new ComputePingTimestampOffset();
	}
}
