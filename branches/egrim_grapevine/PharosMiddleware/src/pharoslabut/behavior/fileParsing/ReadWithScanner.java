package pharoslabut.behavior.fileParsing;

import java.io.*;
import java.util.Scanner;

import pharoslabut.behavior.management.WorldModel;
import pharoslabut.behavior.*;

/**
 * This class is currently not used!
 * 
 * @author Noa Agmon
 */
public class ReadWithScanner {

	public static void main(String[] aArgs) throws FileNotFoundException {
//		ReadWithScanner parser = new ReadWithScanner("robot.cfg");
		// System.out.println(parser.getParameterValue("teamSize"));
		//parser.getTeamIpPort(2);

		// String teamSize;
		// parser.processLineByLine();
		// log("Done.");
	}

	public ReadWithScanner(String aFileName) {
		fFile = new File(aFileName);
	}

	public String getParameterValue(String parameter)
			throws FileNotFoundException {
		Scanner scanner = new Scanner(new FileReader(fFile));
		String line;
		String value;
		try {
			// first use a Scanner to get each line

			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				Scanner lineScanner = new Scanner(line);
				try {
					lineScanner.useDelimiter("=");
					if (lineScanner.hasNext()) {
						value = lineScanner.next();
						value = value.trim();
						if (value.equals(parameter)) {
							value = (lineScanner.next()).trim();
							return value;
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void getLatitudeLongitude(MissionData md[], int numCoord) throws FileNotFoundException{
		String line;
		String value;
		double latitude;
		double longitude;
		double velocity;
		Scanner scanner = new Scanner(new FileReader(fFile));
		try {
			// first use a Scanner to get each line

			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				Scanner lineScanner = new Scanner(line);
				Scanner lineScannerTmp = new Scanner(line);
				try {
					lineScanner.useDelimiter(";");
					lineScannerTmp.useDelimiter(";");
					// This loop will throw exception when the format is not as
					// we want
					for (int i = 0; i < 3; i++)
						lineScannerTmp.next();

					for (int i = 0; i < numCoord; i++) {
						value = lineScanner.next().trim();
						latitude = Double.parseDouble(value);
						value = lineScanner.next().trim();
						longitude = Double.parseDouble(value);
						value = lineScanner.next().trim();
						velocity = Double.parseDouble(value);
						md[i] = new MissionData(latitude, longitude, velocity);
					}

				
					System.out.println();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getTeamIpPort(WorldModel wm) throws FileNotFoundException {
		Scanner scanner = new Scanner(new FileReader(fFile));
		String line;
		String value;
		int index;
		int teamSize = wm.getTeamSize();
		try {
			// first use a Scanner to get each line

			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				Scanner lineScanner = new Scanner(line);
				Scanner lineScannerTmp = new Scanner(line);
				try {
					lineScanner.useDelimiter(";");
					lineScannerTmp.useDelimiter(";");
					// This loop will throw exception when the format is not as
					// we want
					for (int i = 0; i < 3; i++)
						lineScannerTmp.next();

					for (int i = 0; i < teamSize; i++) {
						value = lineScanner.next().trim();
						index = Integer.parseInt(value);
						value = lineScanner.next().trim();
						wm.setIp(index,value);
						value = lineScanner.next().trim();
						wm.setPort(index, Integer.parseInt(value));
					}

				
					System.out.println();

				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/** Template method that calls {@link #processLine(String)}. */
	public final void processLineByLine() throws FileNotFoundException {
		// Note that FileReader is used, not File, since File is not Closeable
		Scanner scanner = new Scanner(new FileReader(fFile));
		try {
			// first use a Scanner to get each line
			while (scanner.hasNextLine()) {
				processLine(scanner.nextLine());
			}
		} finally {
			// ensure the underlying stream is always closed
			// this only has any effect if the item passed to the Scanner
			// constructor implements Closeable (which it does in this case).
			scanner.close();
		}
	}

	/**
	 * Overridable method for processing lines in different ways.
	 * 
	 * <P>
	 * This simple default implementation expects simple name-value pairs,
	 * separated by an '=' sign. Examples of valid input :
	 * <tt>height = 167cm</tt> <tt>mass =  65kg</tt>
	 * <tt>disposition =  "grumpy"</tt>
	 * <tt>this is the name = this is the value</tt>
	 */
	void processLine(String aLine) {
		// use a second Scanner to parse the content of each line
		Scanner scanner = new Scanner(aLine);
		scanner.useDelimiter("=");
		if (scanner.hasNext()) {
			String name = scanner.next();
			String value = scanner.next();
			log("Name is : " + quote(name.trim()) + ", and Value is : "
					+ quote(value.trim()));
		} else {
			log("Empty or invalid line. Unable to process.");
		}
		// no need to call scanner.close(), since the source is a String
	}

	/*
	 * protected void processLine(String aLine){ //use a second Scanner to parse
	 * the content of each line Scanner scanner = new Scanner(aLine);
	 * scanner.useDelimiter("="); if ( scanner.hasNext() ){ String name =
	 * scanner.next(); String value = scanner.next(); log("Name is : " +
	 * quote(name.trim()) + ", and Value is : " + quote(value.trim()) ); } else
	 * { log("Empty or invalid line. Unable to process."); } //no need to call
	 * scanner.close(), since the source is a String }
	 */

	// PRIVATE
	private final File fFile;

	private static void log(Object aObject) {
		//System.out.println(String.valueOf(aObject));
		System.out.println("ReadWithScanner: " + aObject.toString());
	}

	private String quote(String aText) {
		return "'" + aText + "'";
	}
}
