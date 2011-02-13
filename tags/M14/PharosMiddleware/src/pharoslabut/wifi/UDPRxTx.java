package pharoslabut.wifi;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import pharoslabut.logger.*;
import pharoslabut.radioMeter.cc2420.RadioSignalMeterException;

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
	
	private AODVDRunner aodvr;
	//private ClickRunner clickr;
	private M14Process m14p;
	
	/**
	 * Whether this object's thread should remain running.
	 */
	private boolean running = true;
	
	/**
	 * The constructor.
	 * 
	 * @param port The port on which to listen for and transmit UDP datagram packets.
	 */
	public UDPRxTx(String expName, String robotName, int port, FileLogger flogger) {
		this.port = port;
		this.flogger = flogger;
		
		try {
			socket = new DatagramSocket(port);
			
			aodvr = new AODVDRunner(expName, robotName);
			//clickr = new ClickRunner(expName, robotName);
			m14p = new M14Process();
			
			// Create a thread for receiving datagram packets.
			new Thread(this).start();
			
		} catch (SocketException e) {
			e.printStackTrace();
			log("ERROR: Unable to create datagram socket, message: " + e.getMessage());
		}
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
		aodvr.stop();
		//clickr.stop();
		m14p.stop();
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
		FileLogger flogger = new FileLogger("TestExp.log");
		UDPRxTx udp = new UDPRxTx("TestExp", "TestRobot", 55555, flogger);
		
		/*String ipAddr = "10.11.12." + args[0];
		
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
		}*/
	}
	
	private class M14Process implements Runnable {
		boolean running = true;
		int myID;
		int[] destAddrs = {19, 20, 25, 14, 36, 17, 35, 24};
		
		public M14Process() {
			try {
				myID = pharoslabut.radioMeter.cc2420.RadioSignalMeter.getMoteID();
			} catch (RadioSignalMeterException e) {
				e.printStackTrace();
			}
			new Thread(this).start();
		}
		
		public void stop() {
			running = false;
		}
		
		public void run() {
			while(running) {
				
				// send to everybody except myself.
				for (int i = 0; i < destAddrs.length; i++) {
					if (destAddrs[i] != myID) {
						send("10.11.12." + destAddrs[i]);
					}
				}
			
				synchronized(this) {
					try {
						wait(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private class AODVDRunner implements Runnable {
		
		String expName, robotName;

		Process pr;
		
		public AODVDRunner(String expName, String robotName) {
			this.expName = expName;
			this.robotName = robotName;
			new Thread(this).start();
		}
		
		public void stop() {
			log("stopping AODV process.");
			pr.destroy();
			int exitVal;
			try {
				exitVal = pr.waitFor();
				System.out.println("Exited AODV with code " + exitVal);
				log("AODV exited with code " + exitVal);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// remove the modprobe kernel module
			String cmd = "sudo modprobe -r kaodv";
			Runtime rt = Runtime.getRuntime();
			try {
				pr = rt.exec(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			 try {
		            Runtime rt = Runtime.getRuntime();
		            //String cmd = "sudo /home/ut/pcs_experiment/aodvd -i wlan0 -l -g -D -r 1 -p " + expName + "-" + robotName;
		            String cmd = "sudo /home/ut/pcs_experiment/aodvd -i wlan0 -l -g -D -r 1 -d";
		            log("AODV command: " + cmd);
		            
		            pr = rt.exec(cmd);
		            
		            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

		            String line=null;

		            while((line=input.readLine()) != null) {
		               log(line);
		            }
		            
		            int exitVal = pr.waitFor();
		            log("AODV exited with code " + exitVal);
		        } catch(Exception e) {
		        	String eMsg = "Unable to run aodvd: " + e.toString();
		            System.err.println(eMsg);
		            log(eMsg);
		            System.exit(1);
		        }
		}
	}
	
	private class ClickRunner implements Runnable {
		
		String expName, robotName;

		Process pr;
		
		public ClickRunner(String expName, String robotName) {
			this.expName = expName;
			this.robotName = robotName;
			new Thread(this).start();
		}
		
		public void stop() {
			pr.destroy();
			int exitVal;
			try {
				exitVal = pr.waitFor();
				System.out.println("Exited Click with code " + exitVal);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			 try {
		            Runtime rt = Runtime.getRuntime();
		            String cmd = "sudo /home/ut/pcs_experiment/click /home/ut/pcs_experiment/pcs.click";
		            log("Command: " + cmd);
		            
		            pr = rt.exec(cmd);
		            
		            
		            FileLogger flogger1 = new FileLogger(expName + "-" + robotName + "-click.log");
		            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

		            String line=null;

		            while((line=input.readLine()) != null) {
		               flogger1.log(line);
		            }
		            
		        } catch(Exception e) {
		        	String eMsg = "Unable to run click: " + e.toString();
		            System.err.println(eMsg);
		            log(eMsg);
		            System.exit(1);
		        }
		}
	}
}
