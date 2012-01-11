package pharoslabut.demo.simonsays;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import pharoslabut.cpsAssert.AssertionRequestMsg;
import pharoslabut.cpsAssert.AssertionRequestThread;
import pharoslabut.cpsAssert.Inequality;
import pharoslabut.cpsAssert.SensorType;
import pharoslabut.demo.simonsays.io.RobotInstrMsg;
import pharoslabut.logger.Logger;

/**
 * Parses a SimonSays program and executes it if there are no syntax errors.
 * This runs in its own thread.
 * 
 * @author Chien-Liang Fok
 */
public class ExecProgram implements Runnable {
	
	private ProgramEntryGUI gui = null;
	private boolean isRunning = true;
	private ExecInstruction instrExec;
	
	/** 
	 * The constructor.
	 * 
	 * @param gui The GUI that contains the program.
	 * @param instrExec The component that executes an instruction.
	 */
	public ExecProgram(ProgramEntryGUI gui, ExecInstruction instrExec) {
		this.gui = gui;
		this.instrExec = instrExec;
		new Thread(this).start();
	}
	
	/**
	 * @return Whether the program is running.
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Performs the client-side operations of taking a snapshot.
	 * 
	 * @param lineno The line number.
	 * @throws ParseException When an error occurs.
	 */
	private void takeSnapshot(int lineno) throws ParseException {
		JDialog d = new JDialog(gui.getFrame(), "", false);
		d.getContentPane().add(new JLabel("Taking snapshot."));
		d.setSize(new Dimension(300,100));
		d.setLocationRelativeTo(null); // center dialog
		d.setVisible(true);
		
		try {
			BufferedImage img = instrExec.takeSnapshot(); // Communicate with robot to get snapshot
			d.setVisible(false);
			if (img == null) 
				throw new ParseException("Unable to take snapshot on line " + lineno + "...");
			else {
				// Display the image...
				SnapshotFrame sf = new SnapshotFrame(img);
				sf.waitTillClosed();
			}
		} catch(Exception e) {
			throw new ParseException("Problem while taking snapshot on line " + lineno);
		}
	}
	
	/**
	 * Parses an assertion within a program.
	 * 
	 * @param lineOfCode The actual assertion.
	 * @param lineno The line that the assertion appears on.
	 * @return parsed instruction.
	 * @throws ParseException
	 */
	public Instruction parseAssertCommand(String lineOfCode, int lineno) throws ParseException {
		// language guide: ASSERT CRICKET LESS_THAN 1.0 0.05 BLOCKING
		// language guide: ASSERT CAMERA_LOCALIZATION (EQUAL_TO,EQUAL_TO) (0.0,1.0) (0.05,0.05) BLOCKING

		//TODO handle ASSERT message being passed to Server
		System.out.println("Line of Code at line " + lineno + " is " + lineOfCode);
		Scanner scn = new Scanner(lineOfCode);
		// sanity check to make sure lineOfCode actually starts with "ASSERT"
		try {
			if (!scn.next().equalsIgnoreCase("ASSERT")) {
				throw new ParseException("Invalid syntax at line " + lineno + ".");
			}
		} catch (Exception e) {
			throw new ParseException("Invalid syntax at line " + lineno + ".");
		}
		

		SensorType sensor = null;
		try {
			sensor = SensorType.valueOf(scn.next());
		} catch(Exception e) {
			throw new ParseException("Invalid SensorType on line " + lineno + ".");
		}
		if (sensor == null)
			throw new ParseException("Invalid SensorType on line " + lineno + ".");

		
		boolean blocking = false;
		
		switch (sensor) {
//		case CRICKET:
//			Inequality ineq = null;
//			try {
//				ineq = Inequality.valueOf(scn.next());
//			} catch(Exception e) {
//				throw new ParseException("Invalid Inequality on line " + lineno + ".");
//			}
//			if (ineq == null)
//				throw new ParseException("Invalid Inequality on line " + lineno + ".");
//
//			Double dist = null;
//			Double delta = 0.0;
//			try {
//				dist = scn.nextDouble(); 
//				if (scn.hasNextDouble()) // look for delta value
//					delta = scn.nextDouble();
//			} catch(Exception e) {
//				throw new ParseException("Invalid double value on line " + lineno + ".");
//			}
//			if (dist == null)
//				throw new ParseException("Invalid double value on line " + lineno + ".");
//
//			blocking = false;
//			try {
//				if (scn.hasNext())
//					blocking = scn.next().equals("BLOCKING"); 
//			} catch(Exception e) {
//				throw new ParseException("Invalid blocking argument on line " + lineno + ".");
//			}
//
//			return new Command(new AssertionRequestMsg(sensor, new Inequality[] {ineq}, blocking, new Object[] {dist}, new Object[] {delta}), lineno);
			
		case CAMERA_LOCALIZATION: case LOCATION:
			Inequality ineqX = null, ineqY = null;
			Double expectedX = null, expectedY = null;
			Double deltaX = 0.0, deltaY = 0.0;
			blocking = false; 
			try {
				CharSequence text = scn.nextLine();
				Pattern p = Pattern.compile("\\(([^,]+),([^\\)]+)\\)"); // look for (###, ###)
				Matcher m = p.matcher(text);
				if (m.find()) {
					if (m.groupCount() >= 2) { // look for inequalities
						System.out.println("Group1: " + m.group(1) + "   Group2: " + m.group(2));
						ineqX = Inequality.valueOf(m.group(1).trim());
						ineqY = Inequality.valueOf(m.group(2).trim());
					} else {
						throw new ParseException("Invalid Inequality syntax on line " + lineno + ". Must be of the form \"(ineqX, ineqY)\".");
					}
				}
				
				if (m.find()) { // look for expected values
					if (m.groupCount() >= 2) {
						System.out.println("Group1: " + m.group(1) + "   Group2: " + m.group(2));
						expectedX = Double.parseDouble(m.group(1).trim());
						expectedY = Double.parseDouble(m.group(2).trim());
					} else {
						throw new ParseException("Invalid expected values syntax on line " + lineno + ". Must be of the form \"(expectedX, expectedY)\".");
					}
				} else {
					throw new ParseException("Invalid expected values syntax on line " + lineno + ". Must be of the form \"(expectedX, expectedY)\".");
				}
				
				if (m.find()) { // look for delta values
					if (m.groupCount() >= 2) {
						System.out.println("Group1: " + m.group(1) + "   Group2: " + m.group(2));
						deltaX = Double.parseDouble(m.group(1).trim());
						deltaY = Double.parseDouble(m.group(2).trim());
					} else {
						throw new ParseException("Invalid delta values syntax on line " + lineno + ". Must be of the form \"(deltaX, deltaY)\".");
					}
				} else 
					Logger.log("No delta values provided on line " + lineno + ", using (0.0, 0.0).");
				
				
				p = Pattern.compile("[\\s+]\\QBLOCKING\\E"); // look for BLOCKING
				m = p.matcher(text);
				blocking = m.find(); // true if BLOCKING exists
				System.out.println("BLOCKING: " + blocking);
				
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new ParseException("Invalid assertion syntax on line " + lineno + ".");
			}
			
			
			return new LocalAssertionCommand( new AssertionRequestThread(
						new AssertionRequestMsg(sensor, new Inequality[] {ineqX, ineqY}, blocking, new Object[] {expectedX, expectedY}, new Object[] {deltaX, deltaY}), 
					null, true), /*true means run locally */
				lineno);
			
			
			
		default: throw new ParseException("Unknowns invalid assert argument on line " + lineno + ".");
		} // end of switch(sensor) 		
	}
	
	/**
	 * Processes a single line of code.
	 * 
	 * @param lineOfCode The line of code.
	 * @return The parsed command or null if there was no command in the specified line.
	 */
	private Instruction parseLine(int lineno, String lineOfCode) throws ParseException {
		Logger.log("Parsing line " + lineno + ": " + lineOfCode);
		
		// Ignore comments
		if (lineOfCode.startsWith("//"))
			return null;
		
		// Remove trailing comments
		if (lineOfCode.contains("//")) {
			lineOfCode = lineOfCode.substring(0, lineOfCode.indexOf("//"));
			Logger.log("lineOfCode with trailing comment removed: " + lineOfCode);
		}
		
		String[] tokens = lineOfCode.split("[\\s]+");
		String instr;
		
		// Ignore blank lines
		if (tokens.length == 0)
			return null;
		else
			instr = tokens[0].toUpperCase(); // language is not case sensitive
		
		if(instr.equals("DRIVE") || instr.equals("MOVE")) {
			if (tokens.length < 2)
				throw new ParseException("Missing move argument on line " + lineno + ".");
			
			double dist = 0;
			try {
				dist = Double.parseDouble(tokens[1]);
			} catch(Exception e) {
				throw new ParseException("Invalid move argument on line " + lineno + ": " + tokens[1]);
			}
			return new Instruction(InstructionType.MOVE, dist, lineno);
		}
		else if(instr.equals("ROTATE") || instr.equals("TURN")) {
			if (tokens.length < 2)
				throw new ParseException("Missing turn argument on line " + lineno + ".");

			double angle = 0;
			try {
				angle = Double.parseDouble(tokens[1]);
			} catch(Exception e) {
				throw new ParseException("Invalid turn argument on line " + lineno + ": " + tokens[1]);
			}
		
			// Some sanity checks...
			if (angle < -360 || angle > 360) {
				throw new ParseException("Invalid turn angle of " + angle + " degrees on line " + lineno 
						+ ".\nValid range: (-360, 360)");
			}
			
			return new Instruction(InstructionType.TURN, angle, lineno);
		}
		else if(instr.equals("ASSERT")) {
			return parseAssertCommand(lineOfCode, lineno);
		}
		else if(instr.equals("PAN")) {  
			if (tokens.length < 2)
				throw new ParseException("Missing camera pan argument on line " + lineno + ".");

			double panAngle = 0;
			try {
				panAngle = Double.parseDouble(tokens[1]);
			} catch(Exception e) {
				throw new ParseException("Invalid camera pan argument on line " + lineno + ": " + tokens[1]);
			}
		
			// Some sanity checks...
			if (panAngle < -45 || panAngle > 45) {
				throw new ParseException("ERROR: Invalid camera pan angle of " + panAngle + " degrees on line " + lineno
						+ ".\nValid range: (-45, 45)");
			}
			
			return new Instruction(InstructionType.PAN, panAngle, lineno);
		}
		else if(instr.equals("TILT")) {
			if (tokens.length < 2)
				throw new ParseException("Missing camera tilt argument on line " + lineno + ".");

			double tiltAngle = 0;
			try {
				tiltAngle = Double.parseDouble(tokens[1]);
			} catch(Exception e) {
				throw new ParseException("Invalid camera tilt argument on line " + lineno + ": " + tokens[1]);
			}
		
			// Some sanity checks...
			if (tiltAngle < -20 || tiltAngle > 30) {
				throw new ParseException("ERROR: Invalid camera tilt angle of " + tiltAngle + " degrees on line " + lineno
						+ ".\nValid range: (-20, 30)");
			}
			
			return new Instruction(InstructionType.TILT, tiltAngle, lineno);
		}
		else if(instr.equals("SNAPSHOT"))
			return new Instruction(InstructionType.SNAPSHOT, lineno);
		else
			throw new ParseException("Unknown instruction \"" + instr + "\" on line " + lineno);
	}
	
	/**
	 * Parses the program, then executes it.
	 */
	public void run() {
		Logger.log("Parsing program...");

		Vector<Instruction> program = new Vector<Instruction>(); 

		// Read in and parse the program into a sequence of instructions.
		BufferedReader reader = new BufferedReader(new StringReader(gui.getProgramText()));
		String line;
		int linecount = 1;
		
		try {
			while ((line = reader.readLine()) != null) {
				if (!line.equals("") && !line.matches("[\\s+]"))
					program.add(parseLine(linecount++, line));
			}
		} catch (IOException e) {
			e.printStackTrace();
			isRunning = false;
		} catch(ParseException pe) {
			pe.printStackTrace();
			isRunning = false;
			JOptionPane.showMessageDialog(gui.getFrame(), pe.getMessage());
		}

		Logger.log("Begin executing program...");
		// Only execute the program if it was successfully parsed.
		if (isRunning) {
			try {
				Vector<Integer> breakpoints = gui.getBreakpoints();
				
				Enumeration<Instruction> e = program.elements();
				boolean running = true;
				while (running && e.hasMoreElements()) {
					Instruction currCmd = e.nextElement();
					
					if (currCmd instanceof LocalAssertionCommand) {
						AssertionRequestThread arThr = ((LocalAssertionCommand)currCmd).getAssertionRequestThread();
						double startFreeMem = (double)Runtime.getRuntime().freeMemory()/(1024);
						long startTime = System.nanoTime();
						arThr.start();
						if (arThr.isBlocking())
							try {
								arThr.join();
							} catch (InterruptedException e1) {
								Logger.log("Could not join() blocking Assertion at line: " + currCmd.getLine());
								e1.printStackTrace();
							}
						
						System.out.println("*******Assertion delayed execution by " + (System.nanoTime()-startTime) + "ns");
						double endFreeMem = (double)Runtime.getRuntime().freeMemory()/(1024);
						System.out.println("*******Assertion used " + (endFreeMem-startFreeMem) + "KB of memory.");
						continue;
					}
					
					// Create the message to send to the server.
					RobotInstrMsg currMsg = new RobotInstrMsg(currCmd.getType(), currCmd.getDoubleParam(), instrExec.getLocalAddress(), instrExec.getLocalPort());
					
					// Update the GUI to note which line is being executed.
					gui.setExecutionLine(currCmd.getLine());
					
					// If the current line has a breakpoint, pause the program
					if (breakpoints.contains(new Integer(currCmd.getLine()))) {
						JOptionPane.showMessageDialog(gui.getFrame(), "Breakpoint on line " + currCmd.getLine() + "!");
					}
					
					// Snapshot instructions are handled differently since
					// they expect an image to be returned.  All other messages
					// simply get an success/fail acknowledgment.
					if (currCmd.getType() == InstructionType.SNAPSHOT) {
						takeSnapshot(currCmd.getLine());
					} else {
						
						// Do pre-execution tasks if in debug mode...
						if (gui.inDebugMode()) 
							gui.doDebugPre(currCmd.getType());

						// Execute the command...
						if (!instrExec.sendMsg(currMsg))
							throw new ParseException("Failed to execute instruction on line " + currCmd.getLine());
					
						// Do post-execution tasks if in debug mode...
						if (gui.inDebugMode())
							gui.doDebugPost(currMsg);
					}
				}
			} catch(ParseException pe) {
				isRunning = false;
				JOptionPane.showMessageDialog(gui.getFrame(), pe.getMessage());
			}
		}
		
		isRunning = false;
		Logger.log("ProgramExecutor thread exiting...");
		gui.setExecutionLine(1);
		gui.doneExec();
	}
}