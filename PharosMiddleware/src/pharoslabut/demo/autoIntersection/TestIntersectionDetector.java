package pharoslabut.demo.autoIntersection;

import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetectorBlobFinder;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetectorCricket;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetectorDisplay;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetectorIR;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEvent;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventListener;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
//import pharoslabut.tests.TestLineFollower;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.ProteusOpaqueData;
import pharoslabut.sensors.ProteusOpaqueInterface;
import pharoslabut.sensors.ProteusOpaqueListener;
import pharoslabut.sensors.RangerDataBuffer;
import playerclient3.BlobfinderInterface;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.PtzInterface;
import playerclient3.RangerInterface;
import playerclient3.structures.PlayerConstants;

/**
 * Evaluates the intersection detector.
 * 
 * To execute this tester:
 * $ java pharoslabut.demo.autoIntersection.TestIntersectionDetector
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector
 */
public class TestIntersectionDetector implements IntersectionEventListener, ProteusOpaqueListener {
	public static enum IntersectionDetectorType {BLOB, CRICKET, IR};

	private IntersectionDetectorDisplay display = null;
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the player server.
	 * @param serverPort The port of the player server.
	 * @param serialPort The port on which the cricket mote is listening.
	 * @param detectorType The type of intersection detector.
	 * @param showGUI Whether to show the GUI.
	 */
	public TestIntersectionDetector(String serverIP, int serverPort, String serialPort, 
			IntersectionDetectorType detectorType, boolean showGUI) 
	{
		PlayerClient client = null;
		
		// Connect to the player server.
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch (PlayerException e) { Logger.logErr("Could not connect to server."); System.exit(1); }
		Logger.log("Created robot client.");
		
		// Connect to the opaque interface and register self as listener.
		try {
			ProteusOpaqueInterface poi = (ProteusOpaqueInterface)client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
			poi.addOpaqueListener(this);
		} catch (PlayerException e) { Logger.logErr("Could not connect to opaque interface."); System.exit(1);}
		
		LineFollower lf = new LineFollower(client);
		
		Logger.logDbg("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.logDbg("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);	
		
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
			Logger.log("Testing the IR-based intersection detector.");
			
			RangerInterface ri = client.requestInterfaceRanger(0, PlayerConstants.PLAYER_OPEN_MODE);
			RangerDataBuffer rangerBuffer = new RangerDataBuffer(ri);
			rangerBuffer.start();
			Logger.log("Subscribed to the ranger proxy.");
			
			Position2DInterface p2di = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
			if (p2di == null) {
				Logger.logErr("motors is null");
				System.exit(1);
			}
			Position2DBuffer pos2DBuffer = new Position2DBuffer(p2di);
			pos2DBuffer.start();
			Logger.logDbg("Subscribed to Position2d proxy.");
			
			PathLocalizerOverheadMarkers markerDetector = new PathLocalizerOverheadMarkers(rangerBuffer, pos2DBuffer);
			IntersectionDetectorIR id = new IntersectionDetectorIR(markerDetector);
			id.addIntersectionEventListener(this);
		}
		
		if (showGUI)
			display = new IntersectionDetectorDisplay();
		
		lf.start();
	}
	
	@Override
	public void newIntersectionEvent(IntersectionEvent ie) {
		Logger.log("**** INTERSECTION EVENT: " + ie);
		if (display != null)
			display.updateText(ie.getType().toString());
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			Logger.log("MCU Message: " + s);
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
		print("\t-type <blob|cricket|ir>: The type of detector to use (default ir)");
		print("\t-gui: show the GUI.");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
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
