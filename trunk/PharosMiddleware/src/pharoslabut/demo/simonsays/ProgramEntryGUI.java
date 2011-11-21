package pharoslabut.demo.simonsays;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;

import pharoslabut.cpsAssert.AssertionRequestMsg;
import pharoslabut.cpsAssert.AssertionRequestThread;
import pharoslabut.cpsAssert.AssertionThread;
import pharoslabut.cpsAssert.AssertionThreadPosition2d;
import pharoslabut.cpsAssert.Inequality;
import pharoslabut.cpsAssert.SensorType;
import pharoslabut.demo.simonsays.io.CameraPanMsg;
import pharoslabut.demo.simonsays.io.CameraTakeSnapshotMsg;
import pharoslabut.demo.simonsays.io.CameraTiltMsg;
import pharoslabut.demo.simonsays.io.RobotMoveMsg;
import pharoslabut.demo.simonsays.io.RobotTurnMsg;
import pharoslabut.io.*;
import pharoslabut.logger.*;
import playerclient3.structures.PlayerPoint2d;

/**
 * This class provides a GUI for entering programs to control the robot.
 * 
 * @author Chien-Liang Fok
 * @author Lifan Zhang
 * @author Kevin Boos
 */
public class ProgramEntryGUI implements ActionListener {

	private CmdExec cmdExec;
	private ProgramTextArea textArea;
	private JButton submitButton;
	private JFrame frame;
	//private JLabel statusLabel = new JLabel("Debug Mode: False");
	
	private ProgramExecutor executor = null;
	
	/**
	 * Holds the cyber-physical data that maps between logical
	 * and physical values. 
	 */
	private CPPData cppData; // = new CPPData();
	
	/**
	 * A table for displaying the mapping between a variable's
	 * logical and physical values.
	 */
	private CPPTable cppTable; // = new CPPTable(cppData);
	
	/**
	 * A check box controlling whether the cyber-physical properties
	 * window is displayed.
	 */
	private JCheckBoxMenuItem cppTableMI;
	
	/**
	 * A check box controlling whether to run in debug mode.
	 */
	private JCheckBoxMenuItem debugModeMI;
	
	/**
	 * The constructor initializes member variables.
	 * 
	 * @param cmdExec The command executor.
	 */
	public ProgramEntryGUI(CmdExec cmdExec) {
		this.cmdExec = cmdExec;
		
		cppData = new CPPData();
		cppTable = new CPPTable(this, cppData);
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	/**
	 * Construct and display the GUI.
	 * This should only be called by the Swing event thread.
	 */
	public void show() {
		// ensure GUI is not shown twice
		if (textArea != null) return;
		
		textArea = new ProgramTextArea();
		submitButton = new JButton("Submit");
		submitButton.addActionListener(this);
		//JLabel instrLabel = new JLabel("Enter Program:");
		
		frame = new JFrame("Simon Says Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.getContentPane().add(instrLabel, BorderLayout.NORTH);
		//frame.getContentPane().add(statusLabel, BorderLayout.NORTH);
		frame.getContentPane().add(textArea, BorderLayout.CENTER);
		frame.getContentPane().add(submitButton, BorderLayout.SOUTH);
		frame.setSize(new Dimension(400,500));
		frame.setLocationRelativeTo(null); // center frame
		
		createMenuBar();
		
		// Add a window close listener
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent we) {
				cmdExec.stop();
			}
		});
		
		//frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Creates the menu bar.
	 */
	private void createMenuBar() {
		// Create the "Robot" pull down menu
		JMenuItem resetPlayerMI = new JMenuItem("Reset Player Server", KeyEvent.VK_R);
		resetPlayerMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				cmdExec.stopPlayer();
			}
		});
		
		JMenu robotMenu = new JMenu("Robot");
		robotMenu.add(resetPlayerMI);
		
		// Create the "Debug" pull down menu
		debugModeMI = new JCheckBoxMenuItem("Enable Debug Mode", true);
		debugModeMI.setMnemonic(KeyEvent.VK_S);
//		debugModeMI.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent ae) {
//				//statusLabel.setText("Debug Mode: " + (debugModeMI.isSelected() ? "True" : "False"));
//			}
//		});
		
		JMenuItem resetBreakpointsMI = new JMenuItem("Clear Breakpoints", KeyEvent.VK_C);
		resetBreakpointsMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				textArea.clearBreakpoints();
			}
		});
		
		cppTableMI = new JCheckBoxMenuItem("Show Cyber-physical Data");
		cppTableMI.setMnemonic(KeyEvent.VK_S);
		cppTableMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (cppTableMI.isSelected())
					cppTable.show();
				else
					cppTable.hide();
			}
		});
		
		JMenu debugMenu = new JMenu("Debug");
		debugMenu.add(debugModeMI);
		debugMenu.add(cppTableMI);
		debugMenu.add(resetBreakpointsMI);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(robotMenu);
		menuBar.add(debugMenu);
		
		frame.setJMenuBar(menuBar);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		// Only execute the program if the executor does not yet exist or is not running.
		if (executor == null || !executor.isRunning())
			executor = new ProgramExecutor();
		else
			JOptionPane.showMessageDialog(frame, "Another program is running, please wait for it to terminate.");
	}
	
	/**
	 * Parses the program and executes it if there are no syntax errors.
	 * This runs in its own thread.
	 */
	private class ProgramExecutor implements Runnable {
		
		boolean running = true;
		
		/** 
		 * The constructor.
		 */
		public ProgramExecutor() {
			new Thread(this).start();
		}
		
		public boolean isRunning() {
			return running;
		}

		/**
		 * Performs the client-side operations of taking a snapshot.
		 * 
		 * @param lineno The line number.
		 * @throws ParseException When an error occurs.
		 */
		private void takeSnapshot(int lineno) throws ParseException {
			JDialog d = new JDialog(frame, "Taking snapshot...", false);
			d.getContentPane().add(new JLabel("Taking snapshot..."));
			d.setSize(new Dimension(300,100));
			d.setLocationRelativeTo(null); // center dialog
			d.setVisible(true);
			
			try {
				BufferedImage img = cmdExec.takeSnapshot(); // Communicate with robot to get snapshot
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
		
		public void run() {
			Logger.log("Begin executing program...");

			Vector<Command> program = new Vector<Command>(); 

			// Read in the program and parse it for proper grammar.
			BufferedReader reader = new BufferedReader(new StringReader(textArea.getText()));
			String line;
			int linecount = 1;
			
			try {
				while ((line = reader.readLine()) != null) {
					if (!line.equals("") && !line.matches("[\\s+]"))
						program.add(parseLine(linecount++, line));
				}
			} catch (IOException e) {
				e.printStackTrace();
				running = false;
			} catch(ParseException pe) {
				pe.printStackTrace();
				running = false;
				JOptionPane.showMessageDialog(frame, pe.getMessage());
			}

			// Only execute the program if it was successfully parsed.
			if (running) {
				try {
					
					Vector<Integer> breakpoints = textArea.getBreakpoints();
					
					Enumeration<Command> e = program.elements();
					boolean running = true;
					while (running && e.hasMoreElements()) {
						Command currCmd = e.nextElement();
						
						if (currCmd instanceof LocalAssertionCommand) {
							AssertionRequestThread arThr = ((LocalAssertionCommand)currCmd).getAssertionRequestThread();
							arThr.start();
							if (arThr.isBlocking())
								try {
									arThr.join();
								} catch (InterruptedException e1) {
									Logger.log("Could not join() blocking Assertion at line: " + currCmd.getLine());
									e1.printStackTrace();
								}
							
							continue;
						}
						
						
						AckableMessage currMsg = currCmd.getMessage();
						
						// Update the GUI to note which line is being executed.
						textArea.setExecutionLine(currCmd.getLine());
						
						// If the current line has a breakpoint, pause the program
						if (breakpoints.contains(new Integer(currCmd.getLine()))) {
							JOptionPane.showMessageDialog(frame, "Breakpoint on line " + currCmd.getLine() + "!");
						}
						
						// Snapshot instructions are handled differently since
						// they expect an image to be returned.  All other messages
						// simply get an success/fail acknowledgment.
						if (currMsg instanceof CameraTakeSnapshotMsg) {
							takeSnapshot(currCmd.getLine());
						} else {
							
							// Do pre-execution tasks if in debug mode...
							if (debugModeMI.isSelected()) 
								doDebugPre(currMsg);

							// Execute the command...
							if (!cmdExec.sendMsg(currMsg))
								throw new ParseException("Failed to execute instruction on line " + currCmd.getLine());
						
							// Do post-execution tasks if in debug mode...
							if (debugModeMI.isSelected())
								doDebugPost(currMsg);
						}
					}
				} catch(ParseException pe) {
					running = false;
					JOptionPane.showMessageDialog(frame, pe.getMessage());
				}
			}
			
			running = false;
			Logger.log("ProgramExecutor thread exiting...");
			textArea.setExecutionLine(1);
		}
	}
	
	/**
	 * Performs the debugging tasks prior to execution of a command in debug mode.
	 * 
	 * @param msg The command that is being executed.
	 */
	private void doDebugPre(Message msg) {
		if (msg instanceof CameraPanMsg)
			JOptionPane.showMessageDialog(frame, "About to pan camera, note the current pan angle.");
		else if (msg instanceof CameraTiltMsg)
			JOptionPane.showMessageDialog(frame, "About to tilt camera, note the current tilt angle.");
		else if (msg instanceof RobotMoveMsg) {
			JOptionPane.showMessageDialog(frame, "About to move robot, note the robot's current position.");
//			Multilateration.saveCurrentLocation(); // this is the only time that saveCurrentLocation() should be called
		}
		else if (msg instanceof RobotTurnMsg)
			JOptionPane.showMessageDialog(frame, "About to turn robot, note the robot's current heading.");
	}
	
	/**
	 * Performs the debugging tasks post execution of a command in debug mode.
	 * 
	 * @param msg The command that was just executed.
	 */
	private void doDebugPost(Message msg) {
		if (msg instanceof CameraPanMsg) {
			double actualAngle = getDouble("How many degrees did the camera pan?");
			CPP cpp = new CPP("PAN", ((CameraPanMsg)msg).getPanAngle(), actualAngle);
			cppData.add(cpp);
		} 
		else if (msg instanceof CameraTiltMsg) {
			double actualAngle = getDouble("How many degrees did the camera tilt?");
			CPP cpp = new CPP("TILT", ((CameraTiltMsg)msg).getTiltAngle(), actualAngle);
			cppData.add(cpp);
		}
		else if (msg instanceof RobotMoveMsg) {
			double actualDist = getDouble("How many meters did the robot move?");
			// use Euclidean distance formula instead of asking the user
//			PlayerPoint2d curLoc = Multilateration.getCurrentLocation();
//			PlayerPoint2d lastLoc = Multilateration.getLastSavedLocation(); // this is the only time that saveCurrentLocation() should be called
//			double actualDist = Math.sqrt(Math.pow(curLoc.getPx() - lastLoc.getPx(), 2) + Math.pow(curLoc.getPy() - lastLoc.getPy(), 2)); 
			CPP cpp = new CPP("MOVE", ((RobotMoveMsg)msg).getDist(), actualDist);
			cppData.add(cpp);
		}
		else if (msg instanceof RobotTurnMsg) {
			double actualAngle = getDouble("How many degrees did the robot turn?");
			CPP cpp = new CPP("TURN", ((RobotTurnMsg)msg).getAngle(), actualAngle);
			cppData.add(cpp);
		}
	}
	
	/**
	 * Prompts the user for a double.  Keeps prompting until user enters valid double.
	 * 
	 * @param msg The message to display when prompting for user to enter a double.
	 * @return The double value entered by the user.
	 */
	private double getDouble(String msg) {
		boolean gotResult = false;
		double result = 0;
		
		while (!gotResult) {
			String input = JOptionPane.showInputDialog(msg);
			try {
				result = Double.valueOf(input);
				gotResult = true;
			} catch(NumberFormatException e) {
				JOptionPane.showMessageDialog(frame, "Invalid value " + input + " (must be a number)");
			}
		}
		
		return result;
	}
	
	/**
	 * Processes a single line of code.
	 * 
	 * @param lineOfCode The line of code.
	 * @return The parsed command or null if there was no command in the specified line.
	 */
	private Command parseLine(int lineno, String lineOfCode) throws ParseException {
		Logger.log("Parsing line " + lineno + ": " + lineOfCode);
		
		// Ignore comments
		if (lineOfCode.startsWith("//"))
			return null;
		
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
			return new Command(new RobotMoveMsg(dist), lineno);
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
			
			return new Command(new RobotTurnMsg(angle), lineno);
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
			
			return new Command(new CameraPanMsg(panAngle), lineno);
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
			
			return new Command(new CameraTiltMsg(tiltAngle), lineno);
		}
		else if(instr.equals("SNAPSHOT"))
			return new Command(new CameraTakeSnapshotMsg(), lineno);
		else
			throw new ParseException("Unknown instruction \"" + instr + "\" on line " + lineno);
	}
	
	
	
	public Command parseAssertCommand(String lineOfCode, int lineno) throws ParseException {
		// language guide: ASSERT CRICKET LESS_THAN 1.0 0.05 BLOCKING
		// language guide: ASSERT CAMERA_LOCALIZATION (EQUAL_TO,EQUAL_TO) (0.0,1.0) (0.05,0.05) BLOCKING

		//TODO handle ASSERT message being passed to Server

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
		case CRICKET:
			Inequality ineq = null;
			try {
				ineq = Inequality.valueOf(scn.next());
			} catch(Exception e) {
				throw new ParseException("Invalid Inequality on line " + lineno + ".");
			}
			if (ineq == null)
				throw new ParseException("Invalid Inequality on line " + lineno + ".");

			Double dist = null;
			Double delta = 0.0;
			try {
				dist = scn.nextDouble(); 
				if (scn.hasNextDouble()) // look for delta value
					delta = scn.nextDouble();
			} catch(Exception e) {
				throw new ParseException("Invalid double value on line " + lineno + ".");
			}
			if (dist == null)
				throw new ParseException("Invalid double value on line " + lineno + ".");

			blocking = false;
			try {
				if (scn.hasNext())
					blocking = scn.next().equals("BLOCKING"); 
			} catch(Exception e) {
				throw new ParseException("Invalid blocking argument on line " + lineno + ".");
			}

			return new Command(new AssertionRequestMsg(sensor, new Inequality[] {ineq}, blocking, new Object[] {dist}, new Object[] {delta}), lineno);
			
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
					if (m.groupCount() < 2) { // look for inequalities
						ineqX = Inequality.valueOf(m.group(1));
						ineqY = Inequality.valueOf(m.group(2));
					} else {
						throw new ParseException("Invalid Inequality syntax on line " + lineno + ". Must be of the form \"(ineqX, ineqY)\".");
					}
				}
				
				if (m.find()) { // look for expected values
					if (m.groupCount() < 2) {
						expectedX = Double.parseDouble(m.group(1));
						expectedY = Double.parseDouble(m.group(2));
					} else {
						throw new ParseException("Invalid expected values syntax on line " + lineno + ". Must be of the form \"(expectedX, expectedY)\".");
					}
				} else {
					throw new ParseException("Invalid expected values syntax on line " + lineno + ". Must be of the form \"(expectedX, expectedY)\".");
				}
				
				if (m.find()) { // look for delta values
					if (m.groupCount() < 2) {
						deltaX = Double.parseDouble(m.group(1));
						deltaY = Double.parseDouble(m.group(2));
					} else {
						throw new ParseException("Invalid delta values syntax on line " + lineno + ". Must be of the form \"(deltaX, deltaY)\".");
					}
				} else 
					Logger.log("No delta values provided on line " + lineno + ", using (0.0, 0.0).");
				
				
				p = Pattern.compile("[\\s+]\\QBLOCKING\\E"); // look for BLOCKING
				m = p.matcher(text);
				blocking = m.find(); // true if BLOCKING exists
				
				
			} catch (Exception e) {
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
	 * This should be called with the CPPTable is closed.
	 */
	public void CPPTableClosed() {
		cppTableMI.setState(false);
	}
	
//	private void log(String msg) {
//		String result = "ProgramEntryGUI: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
}
