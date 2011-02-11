package pharoslabut.wifi;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import pharoslabut.logger.*;

/**
 * This class listens for and transmits UDP datagram packets.
 * 
 * @author Chien-Liang Fok
 *
 */
public class UDPRxTx implements Runnable {
	
	private int port;
	private DatagramSocket socket;
	private FileLogger flogger = null;
	private Hashtable<String, Integer> serialnoTable = new Hashtable<String, Integer>();
	
	/**
	 * Whether this object's thread should remain running.
	 */
	private boolean running = true;
	
	/**
	 * The constructor.
	 * 
	 * @param port The port on which to listen for and transmit UDP datagram packets.
	 */
	public UDPRxTx(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
			log("ERROR: Unable to create datagram socket, message: " + e.getMessage());
		}
		
		// Create a thread for receiving datagram packets.
		new Thread(this).start();
	}
	
	/**
	 * Sends a UDP packet to a node with the specified address.  The packet contains
	 * a per-node serial number.
	 * 
	 * @param address The address of the destination.
	 */
	public void send(String address) {
		InetAddress addr;
		try {
			addr = InetAddress.getByName(address);
			
			int serialno = 0;
			if (serialnoTable.containsKey(address)) {
				serialno = serialnoTable.remove(address);
			}
			
			byte[] buf = ByteBuffer.allocate(4).putInt(serialno).array();
			
			DatagramPacket pkt = new DatagramPacket(buf, buf.length, addr, port);
			socket.send(pkt);
			log("Sent UDP packet to " + address + ", serial number " + serialno);
			
			// update the hashtable to remember which serial number we are using
			// for each node
			serialno++;
			serialnoTable.put(address, serialno);
			
		} catch (Exception e) {
			e.printStackTrace();
			log("ERROR sending UDP packet to " + address + ", message: " + e.getMessage());
		}
	}
	
	public void setLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
	
	public void stop() {
		running = false;
	}
	
	/**
	 * Sits in a loop receiving datagram packets.
	 */
	public void run() {
		
		while(running) {
			try {
				byte[] buf = new byte[4];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				String address = packet.getAddress().getHostAddress();
				byte[] rxData = packet.getData();
				//log("Received " + rxData.length + " bytes of data");
				if (rxData.length == 4) {
					//int serialno = ByteBuffer.allocate(4).put(packet.getData()).getInt();
					int serialno = ((rxData[0] & 0xff) << 24) | ((rxData[1] & 0xff) << 16) | ((rxData[2] & 0xff) << 8) | (rxData[3] & 0xff);
					log("Received UDP packet from " + address + ", serial number " + serialno);
				}
			} catch (IOException e) {
				e.printStackTrace();
				log("ERROR: while receiving datagram packets, message: " + e.getMessage());
			}
			
		}
		
		log("Receive thread exiting...");
		
	}
	
	private void log(String msg) {
		String result = "UDPRxTx: " + msg;
		
		// only print log text to string if in debug mode
		if (System.getProperty ("PharosMiddleware.debug") != null) {
			System.out.println(result);
		}
		
		// always log text to file if a FileLogger is present
		if (flogger != null) {
			flogger.log(result);
		}
	}
	
	public static void main(String[] args) {
		System.setProperty ("PharosMiddleware.debug", "true");
		UDPRxTx udp = new UDPRxTx(5000);
		
		String ipAddr = "10.11.12." + args[0];
		
		while (true) {
			System.out.println("Sending to " + ipAddr);
			udp.send(ipAddr);
			synchronized(udp) { 
				try {
					udp.wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
