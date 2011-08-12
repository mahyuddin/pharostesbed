package pharoslabut.tests;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.io.*;
import pharoslabut.logger.*;
import pharoslabut.demo.autoIntersection.*;

/**
 * Tests the network interface abstractions.
 * 
 * @author Chien-Liang Fok
 *
 */
public class TestNetworkInterface implements MessageReceiver{
	
	private FileLogger flogger;
	private NetworkInterface ni;
	
	public TestNetworkInterface(boolean useUDP, String serverIP, int localPort, int remotePort, FileLogger flogger) {
		this.flogger = flogger;
		
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
			log("ERROR: Unable to get server address...");
			e.printStackTrace();
			System.exit(1);
		}
		
		int cntr = 0;
		
		while (true) {
			//Message m = new StartExpMsg("TestExp", "Msg-" + cntr, ExpType.FOLLOW_GPS_MOTION_SCRIPT);
			Message m = new pharoslabut.demo.autoIntersection.msgs.RequestAccessMsg(address, 0, 100, 200, new LaneSpecs());
			//Message m = new pharoslabut.demo.autoIntersection.msgs.ExitingMsg(address, 0);
			
			if (ni.sendMessage(address, remotePort, m)) {
				log("Sent message " + cntr);
				cntr++;
			} else {
				log("Failed to send message, waiting an then trying again...");
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
		log("Received message: " + msg);
	}
	
	private void log(String msg) {
		String result = "TestNetworkInterface: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.tests.TestNetworkInterface <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-protocol <type>: The protocol type, either TCP or UDP (default UDP)");
		System.err.println("\t-server <ip address>: The IP address of the remote host (default localhost)");
		System.err.println("\t-serverPort <port number>: The port on which the remote host is listening (default 8889)");
		System.err.println("\t-localPort <port number>: The local port on which to receive incoming messages (default 9000)");
		System.err.println("\t-file <file name>: name of file in which to save results (default null)");
		System.err.println("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		FileLogger flogger = null;
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
					flogger = new FileLogger(args[++i]);
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
		
//		log("Protocol: " + (useUDP ? "UDP" : "TCP"));
//		log("Server IP: " + serverIP);
//		log("Server port: " + serverPort);
		
		new TestNetworkInterface(useUDP, serverIP, serverPort, localPort, flogger);
	}
}
