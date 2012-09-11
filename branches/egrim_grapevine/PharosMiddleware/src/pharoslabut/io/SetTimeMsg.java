package pharoslabut.io;

import java.util.Calendar;

/**
 * This is sent by the PharosExpClient to the PharosExpServer to set the local
 * time on the robot.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.experiment.PharosExpServer
 * @see pharoslabut.experiment.PharosExpClient
 */
public class SetTimeMsg implements Message {

	private static final long serialVersionUID = 6302393829918939873L;
	private String timeString;
	
	/**
	 * The constructor.  Gets and saves the local time.
	 */
	public SetTimeMsg() {
		Calendar cal = Calendar.getInstance();
		int sec = cal.get(Calendar.SECOND);
		int min = cal.get(Calendar.MINUTE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		
		String monthStr = Integer.toString(month);
		if (month < 10)
			monthStr = "0" + monthStr;
		
		String dayStr = Integer.toString(day);
		if (day < 10)
			dayStr = "0" + dayStr;
		
		String hourStr = Integer.toString(hour);
		if (hour < 10)
			hourStr = "0" + hourStr;
		
		String minStr = Integer.toString(min);
		if (min < 10)
			minStr = "0" + minStr;
		
		String yearStr = Integer.toString(year);
		
		String secStr = Integer.toString(sec);
		
		timeString = monthStr + dayStr + hourStr + minStr + yearStr + "." + secStr;
	}
	
	/**
	 * @return  the time that the robot should set its local time to.
	 */
	public String getTime() {
		return timeString;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.SET_TIME;
	}
	
	public String toString() {
		return "SetTimeMsg: " + timeString;
	}

//	public static final void main(String args[] ) {
//		System.out.println(new SetTimeMsg().toString());
//	}
}
