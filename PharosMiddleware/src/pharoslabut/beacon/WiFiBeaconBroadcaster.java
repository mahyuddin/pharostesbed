package pharoslabut.beacon;

import java.net.*;
import java.util.Enumeration;
import java.io.*;

import pharoslabut.logger.FileLogger;

/**
 * The BeaconBroadcaster Periodically broadcasts beacons.  It is used in conjunction
 * with a BeaconReceiver.  Note that when a BeaconBroadcaster is created, it does not immediately
 * begin broadcasting beacons.  To start the beaconing, the start() method must be called.
 *
 * @author Chien-Liang Fok
 * @see BeaconReceiver
 * @see Beacon
 */
public class WiFiBeaconBroadcaster extends BeaconBroadcaster { 
    private Beacon beacon = null;
    private InetAddress mCastAddr;
    private int mCastPort;
    private MulticastSocket mSocket = null;    
    
    /**
     * Creates a BeaconBroadcaster.  Note that it does not automatically start beaconing.
     * To start beaconing, call the start() method.
     *
     * @param mCastAddr the multicast group address to use.
     * @param interfaceIPAddr The IP address of the local interface on which to broadcast beacons.
     * @param mCastPort the multicast port to use.
     * @param beacon the initial beacon to broadcast.
     */
    public WiFiBeaconBroadcaster(InetAddress mCastAddr, String interfaceIPAddr, int mCastPort, Beacon beacon) {
    	
        this.mCastAddr = mCastAddr;
        this.mCastPort = mCastPort;
        this.beacon = beacon;
        
        try{
        	InetSocketAddress ina = new InetSocketAddress(interfaceIPAddr, mCastPort);
            mSocket = new MulticastSocket(ina);
        } catch(IOException ioe) {
        	System.err.println("Error creating multicast socket for broadcasting beacons!");
            ioe.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Sets the beacon that is being broadcasted.
     * 
     * @param beacon The beacon to broadcast.
     */
    public void setBeacon(Beacon beacon) {
    	this.beacon = beacon;
    }
    
    /**
     * Returns the beacon being broadcasted.
     *
     * @return the beacon being broadcasted.
     */
    public Beacon getBeacon() {
    	return beacon;
    }
    

    
    /**
     * Stops the transmission of WiFi beacons.
     */
    public void stop() {
    	running = false;
    }
    
//    /**
//     * Changes the period of beacon broadcasting.
//     *
//     * @param minPeriod The minimum beaconing period in milliseconds
//     * @param maxPeriod The maximum beaconing period in milliseconds
//     */
//    public void setPeriod(long minPeriod, long maxPeriod) {
//       
//    }
    
    /**
     * This is called by the super-class each time a beacon should be sent.
     */
    void sendBeacon() {
    	if (mSocket != null && beacon != null){
			byte[] beaconBytes;

			try{
				ByteArrayOutputStream bs = new ByteArrayOutputStream();
				ObjectOutputStream os = new ObjectOutputStream(bs);
				os.writeObject(beacon);
				os.flush();
				os.close();
				bs.close();
				beaconBytes = bs.toByteArray();

				// package up beacon in DatagramPacket
				DatagramPacket beaconPacket
				= new DatagramPacket(beaconBytes,  beaconBytes.length, mCastAddr, mCastPort);

				log("Broadcasting Beacon: " + beacon);

				// broadcast the beacon
				mSocket.send(beaconPacket);
				beacon.incSeqNum();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
    }

	/**
	 * Find the IP address of the local device within the Pharos wireless
	 * ad hoc network.  It assumes that the IP address takes the form of 10.11.12.*.
	 * 
	 * @return The IP address of the local machine in the Pharos wireless ad hoc network. If no
	 * such network is found, null is returned.
	 */
	// ad hoc network.  
    public static String getPharosIP() {

    	Enumeration<NetworkInterface> ifEnum;
		try {
			ifEnum = NetworkInterface.getNetworkInterfaces();
			while (ifEnum.hasMoreElements()) {
				NetworkInterface ni = ifEnum.nextElement();
				//System.out.println("network interface name = \"" + ni.getName() + "\"");
				Enumeration<InetAddress> ipEnum = ni.getInetAddresses();
				while (ipEnum.hasMoreElements()) {
					InetAddress addr = ipEnum.nextElement();
					//System.out.println("\tip address=" + addr.getHostAddress());
					if (addr.getHostAddress().contains("10.11.12")) {
						String result = addr.getHostAddress();
						//System.out.println("Found! Network interface \"" + result + "\"");
						return result;
					}
					
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
    }
    
    void log(String msg) {
    	String result = "BeaconBroadcaster: " + msg;
    	System.out.println(result);
    	if (flogger != null) {
    		flogger.log(result);
    	}
    }
    
    public static final void main(String[] args) {
    	String interfaceIP = getPharosIP();
    	if (interfaceIP == null) {
    		System.err.println("ERROR: Unable to get Pharos Network IP address");
    		System.exit(1);
    	}
    	String mcastAddressString = "230.1.2.3";
    	int mCastPort = 6000;
    	
		InetAddress mCastGroupAddress = null;
		try {
			mCastGroupAddress = InetAddress.getByName(mcastAddressString);
		} catch (UnknownHostException e) {
			System.err.println("Problems getting multicast address!");
			e.printStackTrace();
			System.exit(1);
		}
		
		int port = 7776;
		Beacon beacon = null;
		try {
			beacon = new Beacon(InetAddress.getByName(interfaceIP), port);
		} catch (UnknownHostException e) {
			System.err.println("Problems creating beacon!");
			e.printStackTrace();
			System.exit(1);
		}
		
		long minPeriod = 1000;
		long maxPeriod = 2000;
		WiFiBeaconBroadcaster bb = new WiFiBeaconBroadcaster(mCastGroupAddress, interfaceIP, mCastPort, beacon);
		bb.start(minPeriod, maxPeriod);
    }
}
