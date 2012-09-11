package pharoslabut.tests;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

/**
 * Tests the FileLogger's ability to avoid overwriting files.
 * If the FileLogger is told to create a file that already exists,
 * it adds a serial number to the end of the file.  If the end of
 * the file has an extension, it puts the serial number before the
 * extension.
 * 
 * @author Chien-Liang Fok
 *
 */
public class TestFileLogger {
	public static void main(String[] args) {
		FileLogger flogger1 = new FileLogger("TestFileLogger.txt");
		flogger1.log("Hello World!");
		
		FileLogger flogger2 = new FileLogger("TestFileLogger.txt");
		flogger2.log("Hello World 2!");
		
		FileLogger flogger3 = new FileLogger("TestFileLogger");
		flogger3.log("Hello World 3!");
		
		FileLogger flogger4 = new FileLogger("TestFileLogger");
		flogger4.log("Hello World 4!");
		
		Logger.log("Done test...");
	}
}
