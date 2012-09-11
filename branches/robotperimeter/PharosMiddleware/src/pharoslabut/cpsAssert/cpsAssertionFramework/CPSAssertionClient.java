package pharoslabut.cpsAssert.cpsAssertionFramework;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


import pharoslabut.cpsAssert.AssertionRequestMsg;
import pharoslabut.demo.simonsays.InstructionType;
import pharoslabut.demo.simonsays.io.CmdDoneMsg;
import pharoslabut.demo.simonsays.io.RobotInstrMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.TCPMessageReceiver;
import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.Logger;
import playerclient3.structures.PlayerPoint2d;


public class CPSAssertionClient implements MessageReceiver {

	private InetAddress serverIPaddr;
	private int serverPort = 8890;
	private InetAddress localIPaddr;
	private int localPort = 8888;
	private TCPMessageSender tcpSender;
	private TCPMessageReceiver tcpReceiver;
	private Message rcvMsg = null;
	
	Map<Object, String> cppMappings = null;
	
	
	
	/**
	 * constructor 
	 * @param ip the IP address of the server
	 * @param p the port that the server is listening to 
	 */
	CPSAssertionClient(InetAddress ip, int p) {
		this.serverIPaddr = ip;
		this.serverPort = p;
		try {
			localIPaddr = InetAddress.getLocalHost();
			localPort = 8888;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		tcpReceiver = new TCPMessageReceiver(this, localPort);
		tcpSender = TCPMessageSender.getSender();
		
		cppMappings = Collections.synchronizedMap(new HashMap<Object, String>());
	}
	
	
	
	@SuppressWarnings("rawtypes") 
	public void configCPPMapping(Object logicalReference, String actualSource) throws CPPMappingException {
		
		if (logicalReference == null) {
			throw new CPPMappingException("Error: the logicalReference \"" + logicalReference + "\" is null. Its value must be initialized to non-null.");
		}
		
		if (cppMappings.containsKey(logicalReference)) { // a source was already mapped to this logicalReference
			throw new CPPMappingException("The logicalReference \"" + logicalReference + "\" is already mapped to actualSource \"" + cppMappings.get(logicalReference) + "\".");
		}
		
		try {
			this.tcpSender.sendMessage(serverIPaddr, serverPort, new CPPConfigRequestMsg(logicalReference, actualSource, localIPaddr, localPort));
		} catch (PharosException e) {
			e.printStackTrace();
		}
		
		CPPConfigResponseMsg respMsg = null;
		
		// wait to receive a CPPConfigResponseMsg back from the server
		synchronized (this) {
			while(!(rcvMsg instanceof CPPConfigResponseMsg)) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			respMsg = (CPPConfigResponseMsg) rcvMsg;
			System.out.println("******Client received CPPConfigResponseMsg: " + respMsg);
		}
		
		if (respMsg.doesSourceExist() && respMsg.doesDataTypeMatch() && logicalReference.equals(logicalReference) && actualSource.equals(respMsg.getActualSource())) { // successful
			System.out.println("Successfully mapped logical reference to actual source " + actualSource);
			cppMappings.put(logicalReference, actualSource);
		}
		else if (!respMsg.doesSourceExist()) {
			throw new CPPMappingException("The actualSource \"" + actualSource + "\" could not be found on the CPSAssertionServer.");
		}
		else if (!respMsg.doesDataTypeMatch()) {
			throw new CPPMappingException("The actualSource \"" + actualSource + " does not match the data type of \"" + logicalReference + "\".");
		}
	
	}
	
	
	
	
	
	public void assertAsync(String assertion) {
		
	}
	
	
	
	
	public void addContinuousAssertion() {
		
	}
	
	


	
	
	

	@Override
	public void newMessage(Message msg) {
		synchronized(this) {
			this.rcvMsg = msg;
		
			if (this.rcvMsg instanceof CmdDoneMsg) {
				this.notifyAll();
			} 
			else if (this.rcvMsg instanceof CPPConfigResponseMsg) {
				this.notifyAll();
			}
		}
		
		System.out.println(" ****** Client received msg: " + msg);
	}
	
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		
		InetAddress serverIPaddr = null, localIPaddr = null; 
		int serverPort = 8890;
		int localPort = 8888;
		CPSAssertionClient cpsClient = null;
		
		try {
			localIPaddr = serverIPaddr = InetAddress.getLocalHost(); // a test scenario for when the client & server run on the same machine
			cpsClient = new CPSAssertionClient(serverIPaddr, serverPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			cpsClient.tcpSender.sendMessage(serverIPaddr, serverPort, new RobotInstrMsg(InstructionType.START_PLAYER, serverIPaddr, localPort));
			
		} catch (PharosException e) {
			e.printStackTrace();
		}
		
		
		PlayerPoint2d robotLoc = new PlayerPoint2d(0, 0);
		
		try {
			cpsClient.configCPPMapping(robotLoc, "cameraLocalization");
		} catch (CPPMappingException ce) {
			System.out.println(ce.getMessage());
		}
		
		try {
			cpsClient.configCPPMapping(robotLoc, "cameraLocalization");
		} catch (CPPMappingException ce) {
			System.out.println(ce.getMessage());
		}
			
	}



}
