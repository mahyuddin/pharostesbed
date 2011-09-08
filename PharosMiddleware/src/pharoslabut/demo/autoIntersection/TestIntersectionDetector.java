package pharoslabut.demo.autoIntersection;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
//import pharoslabut.tests.TestLineFollower;

/**
 * Evaluates the intersection detector.
 * 
 * To execute this tester:
 * $ java pharoslabut.demo.autoIntersection.TestIntersectionDetector
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.IntersectionDetector
 */
public class TestIntersectionDetector implements IntersectionEventListener {
	public static enum IntersectionDetectorType {BLOB, CRICKET, IR};

	private IntesectionDetectorDisplay display = null;
	
	public TestIntersectionDetector(String serverIP, int serverPort, String serialPort, 
			IntersectionDetectorType detectorType, boolean showGUI) {
		LineFollower lf = new LineFollower(serverIP, serverPort);

		if (detectorType == IntersectionDetectorType.BLOB) {
			Logger.log("Testing the BlobFinder-based intersection detector.");
			IntersectionDetectorBlobFinder id = new IntersectionDetectorBlobFinder();
			id.addIntersectionEventListener(this);
			lf.addBlobDataConsumer(id);
		} 
		else if (detectorType == IntersectionDetectorType.CRICKET) {
			Logger.log("Testing the Cricket-based intersection detector.");
			IntersectionDetectorCricket id = new IntersectionDetectorCricket(serialPort);
			id.addIntersectionEventListener(this);
		}
		else if (detectorType == IntersectionDetectorType.IR) {
			Logger.log("Testing the IR intersection detector.");
			IntersectionDetectorIR id = new IntersectionDetectorIR(lf.getOpaqueInterface());
			id.addIntersectionEventListener(this);
		}
		
		if (showGUI)
			display = new IntesectionDetectorDisplay();
		lf.start();
	}
	
	@Override
	public void newIntersectionEvent(IntersectionEvent ie) {
		Logger.log("**** INTERSECTION EVENT: " + ie);
		if (display != null)
			display.updateText(ie.getType().toString());
	}
	
	private class IntesectionDetectorDisplay extends JLabel {
		public static final long DISPLAY_DURATION = 2000;
		
		private static final long serialVersionUID = -2931497139130826529L;

		private int tracking = 0;

		private int left_x = 1, left_y = 1, right_x = 1, right_y = 1;

		private Color left_color = Color.WHITE, right_color = Color.WHITE;

		Timer timer;
		
		public IntesectionDetectorDisplay() {
			super("No Intersection");
			setForeground(Color.black);
			setFont(getFont().deriveFont(140f));


			JFrame frame = new JFrame("Intersection Detector");
			frame.getContentPane().add(this);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setLocationRelativeTo(null); // center frame
			frame.setVisible(true);
		}
		
		public void clearText() {
			setText("            ");
		}
		
		public void updateText(String msg) {
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			setText(msg);
			timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					clearText();
					timer = null;
				}
			}, DISPLAY_DURATION);
		}

		public Dimension getPreferredSize() {
			String text = getText();
			FontMetrics fm = this.getFontMetrics(getFont());

			int w = fm.stringWidth(text);
			w += (text.length() - 1) * tracking;
			w += left_x + right_x;

			int h = fm.getHeight();
			h += left_y + right_y;

			return new Dimension(w, h);
		}

		public void paintComponent(Graphics g) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			char[] chars = getText().toCharArray();

			FontMetrics fm = this.getFontMetrics(getFont());
			int h = fm.getAscent();
			LineMetrics lm = fm.getLineMetrics(getText(), g);
			g.setFont(getFont());

			int x = 0;

			for (int i = 0; i < chars.length; i++) {
				char ch = chars[i];
				int w = fm.charWidth(ch) + tracking;

				g.setColor(left_color);
				g.drawString("" + chars[i], x - left_x, h - left_y);

				g.setColor(right_color);
				g.drawString("" + chars[i], x + right_x, h + right_y);

				g.setColor(getForeground());
				g.drawString("" + chars[i], x, h);

				x += w;
			}

		}
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + TestIntersectionDetector.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-port <port number>: The Player Server's port number (default 6665)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default null)");
		print("\t-serial <port>: The serial port to which the cricket is attached (default /dev/ttyS1)");
		print("\t-type <detector type>: The type of detector to use (blob, cricket, ir, default ir)");
		print("\t-gui: show the GUI.");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		boolean useBlobFinder = false;
		String cricketSerialPort = "/dev/ttyS1";
		IntersectionDetectorType detectorType = IntersectionDetectorType.IR;
		boolean showGUI = false;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-useBlobFinder")) {
					useBlobFinder = true;
				} else if (args[i].equals("-serial")) {
					cricketSerialPort = args[++i];
				} else if (args[i].equals("-type")) {
					String type = args[++i];
					if (type.contains("IR"))
						detectorType = IntersectionDetectorType.IR;
					else if (type.contains("BLOB"))
						detectorType = IntersectionDetectorType.BLOB;
					else if (type.contains("CRICKET"))
						detectorType = IntersectionDetectorType.CRICKET; 
				} else if (args[i].equals("-log")) {
					Logger.setFileLogger(new FileLogger(args[++i]));
				}  else if (args[i].equals("-gui")) {
					showGUI = true;
				} else if (args[i].equals("-h")) {
					usage();
					System.exit(0);
				} else {
					print("Unknown argument " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
 
		print("Server IP: " + serverIP);
		print("Server port: " + serverPort);
		
		new TestIntersectionDetector(serverIP, serverPort, cricketSerialPort, detectorType, showGUI);
	}
}
