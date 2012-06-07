package pharoslabut.demo.simonsays;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

//import pharoslabut.cpsAssert.AssertionRequestMsg;
//import pharoslabut.cpsAssert.AssertionRequestThread;
//import pharoslabut.cpsAssert.AssertionThread;
//import pharoslabut.cpsAssert.AssertionThreadPosition2d;
//import pharoslabut.cpsAssert.Inequality;
//import pharoslabut.cpsAssert.SensorType;
import pharoslabut.demo.simonsays.io.RobotInstrMsg;
//import pharoslabut.io.*;
//import pharoslabut.logger.*;
//import playerclient3.structures.PlayerPoint2d;

/**
 * This class provides a GUI for entering programs to control the robot.
 * 
 * @author Chien-Liang Fok
 * @author Lifan Zhang
 * @author Kevin Boos
 */
public class ProgramEntryGUI implements ActionListener {

	private ExecInstruction instrExec;
	private ExecProgram progExec = null;
	private ProgramTextArea textArea;
	private JButton submitButton;
	private JFrame frame;
	//private JLabel statusLabel = new JLabel("Debug Mode: False");
	
	private boolean enableDebugModeInit;
	
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
	 * The constructor.
	 * 
	 * @param instrExec The instruction executor.
	 * @param enableDebugModeInit Whether to enable debug mode initially
	 */
	public ProgramEntryGUI(ExecInstruction instrExec, boolean enableDebugModeInit) {
		this.instrExec = instrExec;
		this.enableDebugModeInit = enableDebugModeInit;
		
		cppData = new CPPData();
		cppTable = new CPPTable(this, cppData);
	}
	
	/**
	 * @return The frame that contains the GUI.
	 */
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
				instrExec.stop();
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
				instrExec.stopPlayer();
			}
		});
		
		JMenu robotMenu = new JMenu("Robot");
		robotMenu.add(resetPlayerMI);
		
		// Create the "Debug" pull down menu
		debugModeMI = new JCheckBoxMenuItem("Enable Debug Mode", enableDebugModeInit);
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
		if (progExec == null || !progExec.isRunning()) {
			submitButton.setEnabled(false);
			progExec = new ExecProgram(this, instrExec);
		} else
			JOptionPane.showMessageDialog(frame, "Another program is running, please wait for it to terminate.");
	}
	
	/**
	 * This should be called whenever the program is done executing.
	 */
	public void doneExec() {
		submitButton.setEnabled(true);
	}
	
	/**
	 * @return The program.
	 */
	public String getProgramText() {
		return textArea.getText();
	}
	
	/**
	 * Performs the debugging tasks prior to execution of a command in debug mode.
	 * 
	 * @param instrType The instruction that is being executed.
	 */
	public void doDebugPre(InstructionType instrType) {
		switch(instrType) {
		case PAN:
			JOptionPane.showMessageDialog(frame, "About to pan camera, note the current pan angle.");
			break;
		case TILT:
			JOptionPane.showMessageDialog(frame, "About to tilt camera, note the current tilt angle.");
			break;
		case MOVE:
			JOptionPane.showMessageDialog(frame, "About to move robot, note the robot's current heading.");
			//Multilateration.saveCurrentLocation(); // this is the only time that saveCurrentLocation() should be called
			break;
		case TURN:
			JOptionPane.showMessageDialog(frame, "About to turn robot, note the robot's current heading.");
			break;
		}			
	}
	
	/**
	 * Performs the debugging tasks post execution of a command in debug mode.
	 * 
	 * @param msg The command that was just executed.
	 */
	public void doDebugPost(RobotInstrMsg msg) {
		switch(msg.getInstrType()) {
		case PAN:
			double actualAngle = getDouble("How many degrees did the camera pan?");
			CPP cpp = new CPP("PAN", msg.getDoubleParam(), actualAngle);
			cppData.add(cpp);
			break;
		case TILT:
			actualAngle = getDouble("How many degrees did the camera tilt?");
			cpp = new CPP("TILT", msg.getDoubleParam(), actualAngle);
			cppData.add(cpp);
			break;
		case MOVE:
			double actualDist = getDouble("How many meters did the robot move?");
//			PlayerPoint2d curLoc = Multilateration.getCurrentLocation();
//			PlayerPoint2d lastLoc = Multilateration.getLastSavedLocation(); // this is the only time that saveCurrentLocation() should be called
//			double actualDist = Math.sqrt(Math.pow(curLoc.getPx() - lastLoc.getPx(), 2) + Math.pow(curLoc.getPy() - lastLoc.getPy(), 2)); 
			cpp = new CPP("MOVE", msg.getDoubleParam(), actualDist);
			cppData.add(cpp);
		case TURN:
			actualAngle = getDouble("How many degrees did the robot turn?");
			cpp = new CPP("TURN", msg.getDoubleParam(), actualAngle);
			cppData.add(cpp);
			break;
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
	 * 
	 * @return Whether debug mode is enabled.
	 */
	public boolean inDebugMode() {
		return debugModeMI.isSelected();
	}
	
	/**
	 * This should be called with the CPPTable is closed.
	 */
	public void CPPTableClosed() {
		cppTableMI.setState(false);
	}
	
	/**
	 * 
	 * @return The breakpoints in the program.
	 */
	public Vector<Integer> getBreakpoints() {
		return textArea.getBreakpoints();
	}
	
	/**
	 * 
	 * @param line The line that is currently being executed.
	 */
	public void setExecutionLine(int line) {
		textArea.setExecutionLine(line);
	}
	
//	private void log(String msg) {
//		String result = "ProgramEntryGUI: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
}
