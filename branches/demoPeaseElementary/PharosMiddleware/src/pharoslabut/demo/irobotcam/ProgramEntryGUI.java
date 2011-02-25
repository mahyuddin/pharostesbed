package pharoslabut.demo.irobotcam;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
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
	private JTextArea textArea;
	private JButton submitButton;
	private JFrame frame;
	
	/**
	 * The constructor.
	 * 
	 * @param cmdExec The command executor.
	 */
	public ProgramEntryGUI(CmdExec cmdExec, FileLogger flogger) {
		this.cmdExec = cmdExec;
		this.flogger = flogger;
		createGUI();
	}
	
	private void createGUI() {
		
		textArea = new JTextArea();
		submitButton = new JButton("Submit");
		submitButton.addActionListener(this);
		JLabel instrLabel = new JLabel("Enter Program:");
		
		frame = new JFrame("iRobot Create Camera Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(instrLabel, BorderLayout.NORTH);
		frame.getContentPane().add(textArea, BorderLayout.CENTER);
		frame.getContentPane().add(submitButton, BorderLayout.SOUTH);
		frame.setSize(new Dimension(400,500));
		frame.setLocationRelativeTo(null); // center frame
		
		// Create a menu bar
		
		JMenuItem resetPlayerMI = new JMenuItem("Reset Player Server");
		resetPlayerMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				cmdExec.resetPlayer();
			}
		});
		JMenu robotMenu = new JMenu("Robot");
		robotMenu.add(resetPlayerMI);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(robotMenu);
		frame.setJMenuBar(menuBar);
		
		// Add a window close listener
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent we) {
				cmdExec.stop();
			}
		});
		
		
		//frame.pack();
		frame.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		new Thread(new Runnable() {
			public void run() {
				BufferedReader reader = new BufferedReader(new StringReader(textArea.getText()));
				String line;
				int linecount = 1;
				try {
					while ((line = reader.readLine()) != null) {
						parseLine(linecount++, line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch(ParseException pe) {
					pe.printStackTrace();
					JOptionPane.showMessageDialog(frame, pe.getMessage());
				}
			}
		}).start();
	}
	
	/**
	 * Processes a single line of code.
	 * 
	 * @param lineOfCode The line of code.
	 */
	private void parseLine(int lineno, String lineOfCode) throws ParseException {
		log("Parsing line " + lineno + ": " + lineOfCode);
		String[] tokens = lineOfCode.split("[\\s]+");
		String instr;
		
		// Ignore blank lines
		if (tokens.length == 0)
			return;
		else
			instr = tokens[0];
		
		if(instr.equals("DRIVE") || instr.equals("MOVE")) {
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
		else if(instr.equals("ROTATE") || instr.equals("TURN")) {
			if (tokens.length < 2)
				throw new ParseException("Missing rotate argument on line " + lineno + ".");

			double angle = 0;
			try {
				angle = Double.parseDouble(tokens[1]);
			} catch(Exception e) {
				throw new ParseException("Invalid rotate argument on line " + lineno + ": " + tokens[1]);
			}
		
			cmdExec.turnRobot(angle); // convert angle to radians
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
				JDialog d = new JDialog(frame, "Taking snapshot...", false);
				d.getContentPane().add(new JLabel("Taking snapshot..."));
				d.setSize(new Dimension(300,100));
				d.setLocationRelativeTo(null); // center dialog
				d.setVisible(true);
				
				Image img = cmdExec.takeSnapshot();
				d.setVisible(false);
				if (img == null) 
					throw new ParseException("Unable to take snapshot (line " + lineno + "...");
				else {
					// Display the image...
					SnapshotFrame sf = new SnapshotFrame(img);
					sf.waitTillClosed();
				}
			} catch(Exception e) {
				throw new ParseException("Problem while taking snapshot on line " + lineno);
			}
		}
		else {
			throw new ParseException("Unknown instruction \"" + instr + "\" on line " + lineno);
		}	

	}
	
	
	private void log(String msg) {
		String result = "ProgramEntryGUI: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
