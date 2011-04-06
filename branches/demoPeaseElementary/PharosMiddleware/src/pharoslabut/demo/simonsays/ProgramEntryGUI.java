package pharoslabut.demo.simonsays;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
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
 */
public class ProgramEntryGUI implements ActionListener {

	private CmdExec cmdExec;
	private FileLogger flogger;
	private ProgramTextArea textArea;
	private JButton submitButton;
	private JFrame frame;
	
	private ProgramExecutor executor = null;
	
	/**
	 * The constructor initializes member variables.
	 * 
	 * @param cmdExec The command executor.
	 */
	public ProgramEntryGUI(CmdExec cmdExec, FileLogger flogger) {
		this.cmdExec = cmdExec;
		this.flogger = flogger;
	}
	
	/**
	 * Construct and display the GUI.
	 */
	public void show() {
		// ensure GUI is now shown twice
		if (textArea != null) return;
		
		textArea = new ProgramTextArea();
		submitButton = new JButton("Submit");
		submitButton.addActionListener(this);
		//JLabel instrLabel = new JLabel("Enter Program:");
		
		frame = new JFrame("Simon Says Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.getContentPane().add(instrLabel, BorderLayout.NORTH);
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
	
	private void createMenuBar() {
		// Create a menu bar
		JMenuItem resetPlayerMI = new JMenuItem("Reset Player Server");
		resetPlayerMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				cmdExec.stopPlayer();
			}
		});
		
		JMenu robotMenu = new JMenu("Robot");
		robotMenu.add(resetPlayerMI);
		
		JMenuItem resetBreakpointsMI = new JMenuItem("Clear Breakpoints");
		resetBreakpointsMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				textArea.clearBreakpoints();
			}
		});
		
		JMenu debugMenu = new JMenu("Debug");
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
	
	private class ProgramExecutor implements Runnable {
		
		boolean running = true;
		
		public ProgramExecutor() {
			new Thread(this).start();
		}
		
		public boolean isRunning() {
			return running;
		}

		private void takeSnapshot(int lineno) throws ParseException {
			JDialog d = new JDialog(frame, "Taking snapshot...", false);
			d.getContentPane().add(new JLabel("Taking snapshot..."));
			d.setSize(new Dimension(300,100));
			d.setLocationRelativeTo(null); // center dialog
			d.setVisible(true);
			
			try {
				BufferedImage img = cmdExec.takeSnapshot();
				d.setVisible(false);
				if (img == null) 
					throw new ParseException("Unable to take snapshot on line " + lineno + "...");
				else {
					// Display the image...
					SnapshotFrame sf = new SnapshotFrame(img, flogger);
					sf.waitTillClosed();
				}
			} catch(Exception e) {
				throw new ParseException("Problem while taking snapshot on line " + lineno);
			}
		}
		
		public void run() {
			log("Begin executing program...");

			Vector<Command> program = new Vector<Command>(); 

			// Read in the program and parse it for proper grammar.
			BufferedReader reader = new BufferedReader(new StringReader(textArea.getText()));
			String line;
			int linecount = 1;
			
			try {
				while ((line = reader.readLine()) != null) {
					if (!line.matches("[\\s+]"))
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
					Enumeration<Command> e = program.elements();
					boolean running = true;
					while (running && e.hasMoreElements()) {
						Command currCmd = e.nextElement();
						Message currMsg = currCmd.getMessage();

						// Snapshot instructions are handled differently since
						// they expect an image to be returned.  All other messages
						// simply get an success/fail acknowledgment.
						if (currMsg instanceof CameraTakeSnapshotMsg) {
							takeSnapshot(currCmd.getLine());
						} else {
							if (!cmdExec.sendMsg(currMsg))
								throw new ParseException("Failed to execute instruction on line " + currCmd.getLine());
						}
					}
				} catch(ParseException pe) {
					running = false;
					JOptionPane.showMessageDialog(frame, pe.getMessage());
				}
			}
			
			running = false;
			log("ProgramExecutor thread exiting...");
		}
	}
	
	/**
	 * Processes a single line of code.
	 * 
	 * @param lineOfCode The line of code.
	 * @return The parsed command or null if there was no command in the specified line.
	 */
	private Command parseLine(int lineno, String lineOfCode) throws ParseException {
		log("Parsing line " + lineno + ": " + lineOfCode);
		
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
	
	
	private void log(String msg) {
		String result = "ProgramEntryGUI: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
