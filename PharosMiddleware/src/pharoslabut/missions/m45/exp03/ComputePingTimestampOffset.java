package pharoslabut.missions.m45.exp03;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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
		
		cal.set(2012, GregorianCalendar.APRIL, 25, 0, 0);
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
