package pharoslabut.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

/**
 * Logs text to a file.
 * 
 * @author Chien-Liang Fok
 *
 */
public class FileLogger {
	
	private PrintWriter pw = null;
	private boolean printTimeStamp = true;
	
	/**
	 * A constructor.
	 * 
	 * @param fileName The name of the file in which to log text.
	 * @param printTimeStamp Whether to print a time stamp on each log entry.
	 */
	public FileLogger(String fileName, boolean printTimeStamp) {
		this(fileName);
		this.printTimeStamp = printTimeStamp;
	}
	
	/**
	 * A constructor.  Includes a time stamp with every line logged.
	 * 
	 * @param fileName The name of the file in which to log text.
	 */
	public FileLogger(String fileName) {
		int indx = 1;
		
		String actualFileName = fileName;
		// If file exists, find an extension number to avoid deleting files
		File f = new File(actualFileName);
		while (f.exists()) {
			actualFileName = fileName + "-" + (indx++);
			f = new File(actualFileName);
		}

		try {
			FileWriter fw = new FileWriter(actualFileName, false /* overwrite */);
			pw = new PrintWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getUniqueNameExtension() {
		Calendar cal = Calendar.getInstance();
		int sec = cal.get(Calendar.SECOND);
		int min = cal.get(Calendar.MINUTE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		
		String secStr = Integer.toString(sec);
		if (sec < 10) secStr = "0" + secStr;
		
		String minStr = Integer.toString(min);
		if (min < 10) minStr = "0" + minStr;
		
		String hrStr = Integer.toString(hour);
		if (hour < 10) hrStr = "0" + hrStr;
		
		String dayStr = Integer.toString(day);
		if (day < 10) dayStr = "0" + dayStr;
		
		String monthStr = Integer.toString(month);
		if (month < 10) monthStr = "0" + monthStr;
		
		return year + "" + monthStr + "" + dayStr + "" + hrStr + "" + minStr + "" + secStr; 
	}
	
	public void log(String msg) {
		if (pw != null) {
			if (printTimeStamp)
				pw.println("[" + System.currentTimeMillis() + "] " + msg);
			else
				pw.println(msg);
			pw.flush();
		}
	}
}
