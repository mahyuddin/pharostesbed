package pharoslabut.cpsAssert.cpsAssertionFramework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.util.WaitingImageObserver;

import pharoslabut.demo.simonsays.InstructionType;
import pharoslabut.demo.simonsays.io.CmdDoneMsg;
import pharoslabut.demo.simonsays.io.RobotInstrMsg;
import pharoslabut.demo.simonsays.io.SimonSaysClientMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.PharosAckMsg;
import pharoslabut.io.TCPMessageReceiver;
import pharoslabut.io.TCPMessageSender;


public class CPSAssertionServer implements MessageReceiver {

	
	/**
     * The connection back to the client.
     */
    private TCPMessageSender tcpSender = TCPMessageSender.getSender();
    
    /**
     * maps a logicalReference (key) to a SensorType (value)
     */
    Map<Object, SensorType> cppMappings = null;
    
    
    
	static int port = 8890; // port is the port the server will listen to
	
		
	/**
	 * constructor
	 */
	CPSAssertionServer() {	
		
		
		cppMappings = Collections.synchronizedMap(new HashMap<Object, SensorType>());
		
		new TCPMessageReceiver(this, port);
		
		
//		new Thread(
//	            new Runnable() {
//	                public void run() {
//	                    try {
//	                        Thread.sleep(10 * 1000);
//	                    } catch (Exception e) {
//	                        e.printStackTrace();
//	                    }
//	                    
//	                    try {
//	            			try {
//	            				System.out.println("\n****** Server trying to send msg to Client....");
//								tcpSender.sendMessage(InetAddress.getLocalHost(), 8888, new RobotInstrMsg(InstructionType.START_PLAYER, InetAddress.getLocalHost(), 8888));
//								System.out.println("\n****** Server sent msg to Client.");
//	            			} catch (UnknownHostException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//	            			
//	            			
//	            		} catch (PharosException e) {
//	            			// TODO Auto-generated catch block
//	            			e.printStackTrace();
//	            		}
//	                }
//	            }).start();
		
	}
	

	
	

	public static void main (String [] args) {
		
		String pServerIP = "localhost";
		int pServerPort = 6665;
		String logFile = "CPSAssertionFrameworkServer.log";
		
		
		// process command-line args
		for (int i=0; i < args.length; i++) {
			if (args[i].equals("-pServer")) { // player server IP address
				pServerIP = args[++i];
			}
			else if (args[i].equals("-pPort")) { // player server TCP port
				pServerPort = Integer.valueOf(args[++i]);
			}
			else if (args[i].equals("-port") || args[i].equals("-p")) {
				port = Integer.valueOf(args[++i]);
			}
			else if (args[i].equals("-log")) {
				logFile = args[++i];
			}
		}
		
   
		new CPSAssertionServer();
		
	}



	@Override
	public void newMessage(Message msg) {
		if (msg instanceof CPPConfigRequestMsg) {
			handleCPPConfigRequest((CPPConfigRequestMsg) msg);
		} 
		else if (msg instanceof RobotInstrMsg) {
			System.out.println("******Server Received msg: " + (RobotInstrMsg) msg);
		}
		
	}




	private void handleCPPConfigRequest(CPPConfigRequestMsg msg) {
		Object logicalReference = msg.getLogicalReference();
		String actualSource = msg.getActualSource();
		boolean sourceExists = true;
		boolean dataTypeMatches = false; 
		
		// the client already checks for duplicate mappings of logicalReferences, no need to check again here
		
		// check to see if the source exists // this will need to be advanced text matching later
		try {
			System.out.println("logicalReference.class = " + logicalReference.getClass());
			System.out.println("Found the sensor at index: " + Sensors.keywordMatcher(actualSource));
			Entry<SensorType, Object> e = Sensors.getMatchedSensor(Sensors.keywordMatcher(actualSource)); 
			
			if (e != null) {
				System.out.println("Sensor found has type: " + e.getKey() + ", " + e.getValue());
				if (e.getValue().equals(logicalReference.getClass())) { // not sure if this comparison is correct 
					dataTypeMatches = true;
					System.out.println("data types matched!");
				} else 
					System.out.println("data types did not match.");
			}		
		} catch (IndexOutOfBoundsException ex) {
			// means it couldn't find the sensor from the actualSource 
			sourceExists = false; 
		}

		// TODO send the message
		try {
			tcpSender.sendMessage(msg.getReplyAddr(), msg.getReplyPort(), new CPPConfigResponseMsg(logicalReference, actualSource, sourceExists, dataTypeMatches));
		} catch (PharosException e) {
			e.printStackTrace();
		}
		
		
		
		
	}




}
