package pharoslabut.wifi;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import pharoslabut.*;
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
	
	private PCSExpRunner pcsexpr;
	//private ClickRunner clickr;
	private DataGenerator dataGen;
	private BigDataGenerator bigDataGen;
	
	/**
	 * The last octal of each robot's IP address
	 */
	int[] destAddrs = {
			RobotIPAssignments.SHINER,
			RobotIPAssignments.MANNY,
			RobotIPAssignments.GUINNESS,
			RobotIPAssignments.ZIEGEN,
			RobotIPAssignments.WYNKOOP,
			RobotIPAssignments.SPATEN,
			RobotIPAssignments.CZECHVAR
	};
	
	/**
	 * Whether this object's thread should remain running.
	 */
	private boolean running = true;
	
	
	public UDPRxTx() {
		System.out.println("Starting the experiment.");
		//PCSExpRunner pcsExp = new PCSExpRunner("TestExp", "TestRobot");
		
		BigDataGenerator bdg = new BigDataGenerator("TestExp", "TestRobot");
		
		synchronized(this) { 
			try {
				System.out.println("Letting test run for 60 seconds...");
				this.wait(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Stopping the experiment.");
		//pcsExp.stop();
		bdg.stop();
	}
	
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
			
			pcsexpr = new PCSExpRunner(expName, robotName);
			//clickr = new ClickRunner(expName, robotName);
			dataGen = new DataGenerator();
			bigDataGen = new BigDataGenerator(expName, robotName);
			
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
		
		synchronized(this) {
			try {
				log("Waiting 5 minutes before stopping the AODV test...");
				wait(1000 * 60 * 5); // wait 5 min before stopping
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		running = false;
		pcsexpr.stop();
		//clickr.stop();
		dataGen.stop();
		bigDataGen.stop();
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
		
		new UDPRxTx();
		
		/*FileLogger flogger = new FileLogger("TestExp.log");
		UDPRxTx udp = new UDPRxTx("TestExp2", "TestRobot", 55555, flogger);
		
		
		synchronized(udp) { 
			try {
				System.out.println("Letting test run for 30 seconds...");
				udp.wait(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Stopping the UDPRxTx object.");
		udp.stop();*/
		
		//System.exit(0);
		
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
	
	/** 
	 * Periodically generates data by sending UDP packets to other robots.
	 * 
	 * @author Chien-Liang Fok
	 */
	private class DataGenerator implements Runnable {
		boolean running = true;
		int myID;
		
		public DataGenerator() {
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
						wait(10000); // 10 seconds
						//wait(5000); // 5 seconds
						//wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/** 
	 * Periodically generates data by sending UDP packets to other robots.
	 * 
	 * @author Chien-Liang Fok
	 */
	private class BigDataGenerator implements Runnable {
		boolean running = true;
		int myID;
		String missionName, expName;
		int serialno = 0;
		
		public BigDataGenerator(String missionName, String expName) {
			this.missionName = missionName;
			this.expName = expName;
			
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
		
		private void sendBigFile(String addr) {
			try {
				Runtime rt = Runtime.getRuntime();
				String cmd = "scp Big1MBFile.bin ut@" + addr + ":M15/" + missionName + "-" + expName + "-" + myID + "-" + serialno + ".bin";
				log("BigFileTx: " + cmd);
				
				serialno++;

				Process pr = rt.exec(cmd);

				BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

				String line=null;

				while((line=input.readLine()) != null) {
					log(line);
				}

				int exitVal = pr.waitFor();
				log("BigFileTx: scp exited with code " + exitVal);
			} catch(Exception e) {
				String eMsg = "Unable to run aodvd: " + e.toString();
				System.err.println(eMsg);
				log(eMsg);
				System.exit(1);
			}
		}
		
		public void run() {
			while(running) {
				
				int randIndx = (int) (Math.random() * destAddrs.length);;
				
				while (destAddrs[randIndx] == myID) {
					randIndx = (int) (Math.random() * destAddrs.length);
				}
				
				sendBigFile("10.11.12." + destAddrs[randIndx]);
			
				synchronized(this) {
					try {
						wait(1000 * 10); // 10 seconds
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public class PCSExpRunner implements Runnable {
		
		String expName, robotName;

		Process pr;
		
		public PCSExpRunner(String expName, String robotName) {
			this.expName = expName;
			this.robotName = robotName;
			new Thread(this).start();
		}
		
		public void stop() {
			log("stopping AODV process.");
//			pr.destroy();
//			int exitVal;
//			try {
//				exitVal = pr.waitFor();
//				System.out.println("Exited AODV with code " + exitVal);
//				log("AODV exited with code " + exitVal);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			
			// remove the modprobe kernel module
			//String cmd = "sudo modprobe -r kaodv";
			String cmd = "sudo /home/ut/pcs_experiment/pcs_exp_stop.pl";
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
				//String cmd = "sudo /home/ut/pcs_experiment/aodvd -i wlan0 -l -g -D -r 1 -d";
				String cmd = "sudo /home/ut/pcs_experiment/pcs_exp_start.pl " + 2 + " " + expName + "-" + robotName;
				log("AODV command: " + cmd);

				pr = rt.exec(cmd);

				BufferedReader input = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

				String line=null;

				while((line=input.readLine()) != null) {
					log(line);
				}

				int exitVal = pr.waitFor();
				log("pcs_exp_start.pl exited with code " + exitVal);
			} catch(Exception e) {
				String eMsg = "Unable to run aodvd: " + e.toString();
				System.err.println(eMsg);
				log(eMsg);
				System.exit(1);
			}
		}
	}
	
//	private class ClickRunner implements Runnable {
//		
//		String expName, robotName;
//
//		Process pr;
//		
//		public ClickRunner(String expName, String robotName) {
//			this.expName = expName;
//			this.robotName = robotName;
//			new Thread(this).start();
//		}
//		
//		public void stop() {
//			pr.destroy();
//			int exitVal;
//			try {
//				exitVal = pr.waitFor();
//				System.out.println("Exited Click with code " + exitVal);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		public void run() {
//			 try {
//		            Runtime rt = Runtime.getRuntime();
//		            String cmd = "sudo /home/ut/pcs_experiment/click /home/ut/pcs_experiment/pcs.click";
//		            log("Command: " + cmd);
//		            
//		            pr = rt.exec(cmd);
//		            
//		            
//		            FileLogger flogger1 = new FileLogger(expName + "-" + robotName + "-click.log");
//		            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//
//		            String line=null;
//
//		            while((line=input.readLine()) != null) {
//		               flogger1.log(line);
//		            }
//		            
//		        } catch(Exception e) {
//		        	String eMsg = "Unable to run click: " + e.toString();
//		            System.err.println(eMsg);
//		            log(eMsg);
//		            System.exit(1);
//		        }
//		}
//	}
}
