package pharoslabut.tests;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.io.*;
import pharoslabut.logger.*;
import pharoslabut.navigate.Heading;
import pharoslabut.navigate.Location;
import pharoslabut.demo.autoIntersection.*;
import pharoslabut.demo.autoIntersection.intersectionSpecs.EntryPoint;
import pharoslabut.demo.autoIntersection.intersectionSpecs.ExitPoint;

/**
 * Tests the network interface abstractions.
 * 
 * @author Chien-Liang Fok
 *
 */
public class TestNetworkInterface implements MessageReceiver{
	
	private NetworkInterface ni;
	
	public TestNetworkInterface(boolean useUDP, String serverIP, int localPort, int remotePort) {
		
		if (useUDP) {
			ni = new UDPNetworkInterface(localPort);
		} else {
			ni = new TCPNetworkInterface(localPort);
		}
		
		ni.registerMsgListener(this);
		
		InetAddress address = null;
		try {
			address = InetAddress.getByName(serverIP);
		} catch (UnknownHostException e) {
			Logger.logErr("Unable to get server address...");
			e.printStackTrace();
			System.exit(1);
		}
		
		int cntr = 0;
		
		while (true) {
			//Message m = new StartExpMsg("TestExp", "Msg-" + cntr, ExpType.FOLLOW_GPS_MOTION_SCRIPT);
			Message m = new pharoslabut.demo.autoIntersection.clientDaemons.centralized.RequestAccessMsg(address, 0,
					"E1", "X3");
			
			//Message m = new pharoslabut.demo.autoIntersection.msgs.ExitingMsg(address, 0);
			
			if (ni.sendMessage(address, remotePort, m)) {
				Logger.log("Sent message " + cntr);
				cntr++;
			} else {
				Logger.logErr("Failed to send message, waiting an then trying again...");
			}
			
			try {
				synchronized(this) {
					wait(1000);
				}
			} catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	@Override
	public void newMessage(Message msg) {
		Logger.log("Received message: " + msg);
	}
	
	private static void usage() {
		System.err.println("Usage: " + TestNetworkInterface.class.getName() + " <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-protocol <type>: The protocol type, either TCP or UDP (default UDP)");
		System.err.println("\t-server <ip address>: The IP address of the remote host (default localhost)");
		System.err.println("\t-serverPort <port number>: The port on which the remote host is listening (default 8889)");
		System.err.println("\t-localPort <port number>: The local port on which to receive incoming messages (default 9000)");
		System.err.println("\t-file <file name>: name of file in which to save results (default null)");
		System.err.println("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		boolean useUDP = true;
		int serverPort = 8889;
		int localPort = 9000;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-protocol")) {
					useUDP = !args[++i].toUpperCase().equals("TCP");
				}
				else if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-serverPort")) {
					serverPort = Integer.valueOf(args[++i]);
				} 
				else if (args[i].equals("-localPort")) {
					localPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-file")) {
					Logger.setFileLogger(new FileLogger(args[++i]));
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else {
					System.err.println("Unknown option: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		new TestNetworkInterface(useUDP, serverIP, serverPort, localPort);
	}
}
