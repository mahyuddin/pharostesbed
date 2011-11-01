package pharoslabut.cps;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import pharoslabut.io.*;
import pharoslabut.logger.*;


/**
 * This class provides a GUI for entering programs to control the robot.
 * 
 * @author Chien-Liang Fok
 * @author Lifan Zhang
 * @author Kevin Boos
 */
public class CPSGui implements ActionListener {

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
	public CPSGui(CmdExec cmdExec) {
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
						AckableMessage currMsg = currCmd.getMessage();
						
						// Update the GUI to note which line is being executed.
						textArea.setExecutionLine(currCmd.getLine());
						
						// If the current line has a breakpoint, pause the program
						if (breakpoints.contains(new Integer(currCmd.getLine()))) {
							JOptionPane.showMessageDialog(frame, "Breakpoint on line " + currCmd.getLine() + "!");
						}
						
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
		if (msg instanceof RobotMoveMsg) {
			JOptionPane.showMessageDialog(frame, "About to move robot, note the robot's current position.");
		}
		else if (msg instanceof RobotTurnMsg)
			JOptionPane.showMessageDialog(frame, "About to turn robot, note the robot's current heading.");
//		else if (msg instanceof ResetPlayerMsg)
//			ri.stopPlayer();
//		else if (msg instanceof PlayerControlMsg)
//			handlePlayerControlMsg((PlayerControlMsg)msg);
	}
	
	/**
	 * Performs the debugging tasks post execution of a command in debug mode.
	 * 
	 * @param msg The command that was just executed.
	 */
	private void doDebugPost(Message msg) {
		if (msg instanceof RobotMoveMsg) {
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
		// TODO else if(instr.equals("ASSERT") {
		// TODO 	create a new AssertionRequestMsg, return it inside of a new Command
		else
			throw new ParseException("Unknown instruction \"" + instr + "\" on line " + lineno);
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
