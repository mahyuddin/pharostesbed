package pharoslabut.demo.irobotcam;

import pharoslabut.logger.*;

/**
 * This class provides a GUI for entering programs to control the robot.
 * 
 * @author Chien-Liang Fok
 * @author Lifan Zhang
 */
public class ProgramEntryGUI {

	private CmdExec cmdExec;
	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param cmdExec The command executor.
	 */
	public ProgramEntryGUI(CmdExec cmdExec, FileLogger flogger) {
		this.cmdExec = cmdExec;
		this.flogger = flogger;
	}
	
	/**
	 * Processes a single line of code.
	 * 
	 * @param lineOfCode The line of code.
	 */
	private void LineParser(int lineno, String lineOfCode) throws ParseException {
		String[] tokens = lineOfCode.split("[\\s]+");
		String instr;
		
		// Ignore blank lines
		if (tokens.length == 0)
			return;
		else
			instr = tokens[0];
		
		if(instr.equals("DRIVE")) {
			if (tokens.length < 2)
				throw new ParseException("Missing drive argument on line " + lineno + ".");
			
			double dist = 0;
			try {
				dist = Double.parseDouble(tokens[1]);
			} catch(Exception e) {
				throw new ParseException("Invalid drive argument on line " + lineno + ": " + tokens[1]);
			}
			
			cmdExec.moveRobot(dist);
		}
		else if(instr.equals("ROTATE")) {
			if (tokens.length < 2)
				throw new ParseException("Missing rotate argument on line " + lineno + ".");

			double angle = 0;
			try {
				angle = Double.parseDouble(tokens[1]);
			} catch(Exception e) {
				throw new ParseException("Invalid rotate argument on line " + lineno + ": " + tokens[1]);
			}
		
			cmdExec.turnRobot(angle / 180 * Math.PI); // convert angle to radians
		}
		else if(instr.equals("PAN")) {  
			if (tokens.length < 2)
				throw new ParseException("Missing pan argument on line " + lineno + ".");

			double panAngle = 0;
			try {
				panAngle = Double.parseDouble(tokens[1]);
			} catch(Exception e) {
				throw new ParseException("Invalid pan argument on line " + lineno + ": " + tokens[1]);
			}
		
			if (!cmdExec.panCamera(panAngle)) 
				log("Unable to pan camera...");
		}
		else if(instr.equals("TILT")) {
			if (tokens.length < 2)
				throw new ParseException("Missing tilt argument on line " + lineno + ".");

			double tiltAngle = 0;
			try {
				tiltAngle = Double.parseDouble(tokens[1]);
			} catch(Exception e) {
				throw new ParseException("Invalid tilt argument on line " + lineno + ": " + tokens[1]);
			}
		
			if (!cmdExec.tiltCamera(tiltAngle)) 
				log("Unable to tilt camera...");
		}
		else if(instr.equals("SNAPSHOT")) {
			try {
				if (!cmdExec.takeSnapshot()) 
					log("Unable to take snapshot...");
			} catch(Exception e) {
				throw new ParseException("Problem while taking snapshot on line " + lineno);
			}
		}
		else {
			throw new ParseException("Unknown instruction \"" + tokens[0]);
		}	

	}
	
	
	private void log(String msg) {
		String result = "ProgramEntryGUI: " + msg;
		System.out.println(result);
		if (flogger != null) {
			flogger.log(result);
		}
	}
	
}
