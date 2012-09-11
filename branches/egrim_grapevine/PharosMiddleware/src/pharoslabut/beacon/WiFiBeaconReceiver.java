package pharoslabut.beacon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Vector;

import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.Logger;
import edu.utexas.ece.mpc.context.net.ContextShimmedMulticastSocket;

/**
 * This receives WiFi beacons. When a WiFi beacon is received, each of the WiFiBeaconListeners are
 * notified being passed a WiFiBeaconEvent containing the beacon.
 *
 * @author Chien-Liang Fok
 */
public class WiFiBeaconReceiver implements Runnable {
    /**
     * The multicast port.
     */
    private int mcastport;
    
    /**
     * The multicast group address.
     */
    private String mcastGroupAddress;
    
    /**
     * The multicast socket we're listening in on.
     */
    private MulticastSocket mSocket;
    
    /**
     * Holds the beacon listeners registered on this receiver.
     */
    private Vector<WiFiBeaconListener> bListeners;
    
    /**
     * The thread that receives beacons.
     */
    private Thread bThread;
    
    private boolean running = false;
    
    private InetSocketAddress socketAddr;
    
    private NetworkInterface ni;
    
    private String networkInterfaceName;
    
//    private FileLogger flogger;
    
    /**
     * Creates a BeaconReceiver listening to the given multicast socket.
     * The mcastGroupAddress must be a class D IP address.  These IP
     * addresses are in the range 224.0.0.0 to 239.255.255.255.
     * 
     * Note that this does not automatically start receiving beacons.
     * To start receiving beacons, call the start() method.
     *
     * @param mcastGroupAddress the multicast address to listen in on.
     * @param mcastport the multicast port to listen in on.
     * @param networkInterfaceName The name of the network interface on which to listen
     * for beacons. For example, it usually is "wlan0" on machines using the Atheros wireless
     * chipset.
     */
    public WiFiBeaconReceiver(String mcastGroupAddress, int mcastport, String networkInterfaceName) {
        this.mcastGroupAddress = mcastGroupAddress;
        this.mcastport = mcastport;
        this.networkInterfaceName = networkInterfaceName;
        bListeners = new Vector<WiFiBeaconListener>();
    }
    
//    public void setFileLogger(FileLogger flogger) {
//    	this.flogger = flogger;
//    }
    
    /**
     * Adds a beaconListener to this server.  The beacon listener
     * will be notified each time a beacon is received.
     *
     * @param beaconListener the BeaconListener to be added.
     */
    public void addBeaconListener(WiFiBeaconListener beaconListener) {
        bListeners.addElement(beaconListener);
    }
    
    /**
     * Returns the multicast group address being used by this receiver.
     *
     * @return the multicast group address being used by this receiver.
     */
//    public InetAddress getMcastAddress() {
//        return mcastGroupAddress;
//    }
    
    /**
     * Returns the multicast port being used by this receiver.
     *
     * @return the multicast port being used by this receiver.
     */
    public int getMcastPort() {
        return mcastport;
    }
    
    /**
     * Removes a beaconListener from this server.
     *
     * @param beaconListener the BeaconListener to be added.
     */
    public void removeBeaconListener(WiFiBeaconListener beaconListener) {
        bListeners.removeElement(beaconListener);
    }
    
    /**
     * Starts the BeaconReceiver.
     */
    public synchronized void start() {
        if (bThread == null) {
            try{
            	ni = NetworkInterface.getByName(networkInterfaceName);
                mSocket = new ContextShimmedMulticastSocket(mcastport);
                InetAddress group = InetAddress.getByName(mcastGroupAddress);
                socketAddr = new InetSocketAddress(group, mcastport);
                mSocket.joinGroup(socketAddr, ni);
            }
            catch(IOException ioe) {
            	Logger.logErr("Problems connecting to multicast group!");
                ioe.printStackTrace();
                System.exit(1);
            }
            
            bThread = new Thread(this);
            bThread.start();
            running = true;
        }
    }
    
    /**
     * Stops this BeaconReceiver from listening for beacons.
     */
    public synchronized void stop() {
        if (running && bThread != null) {
        	running = false;
            try {
                mSocket.leaveGroup(socketAddr, ni);
                mSocket.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
            try {
                bThread.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            bThread = null;
        }
    }
    
    /**
     * Notifies each of the listeners of the new beacon.
     */
    private void distributeBeacon(WiFiBeacon beacon) {
    	Logger.log("Received beacon: " + beacon);
    	
    	WiFiBeaconEvent be = new WiFiBeaconEvent(beacon);
        for (int i = 0; i < bListeners.size(); i++) {
            WiFiBeaconListener bl = bListeners.elementAt(i);
            bl.beaconReceived(be);
        }
    }
    
    /**
     * Sites in a loop waiting for beacons.
     */
    public void run() {
        
        try {
            while (running) {
                
                // read in data from the multicast socket
                byte[] buf = new byte[10000];
                DatagramPacket packet = new DatagramPacket(buf,buf.length);
                
                mSocket.receive(packet);
                
                // convert the data read in into an object
                ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(packet.getData()));
                
                Object value = ois.readObject();
                
                // check the object type and perform required actions
                if (value instanceof WiFiBeacon)
                    distributeBeacon((WiFiBeacon) value);
            }
        } catch(IOException ioe) {
//            if (mSocket != null && !mSocket.isClosed())
//                ioe.printStackTrace();
        }
        catch (ClassNotFoundException cnfe) {
//			if (mSocket != null && !mSocket.isClosed())
                cnfe.printStackTrace();
        }
        finally {
            try {
                mSocket.close();
            }catch(Exception oie) {}
        }
    }
    
//    private void log(String msg) {
//    	String result = "BeaconReciever: " + msg;
//        System.out.println(result);
//        if (flogger != null) {
//        	flogger.log(result);
//        }
//    }

//	/**
//	 * Find the local network interface that is connected to the Pharos wireless
//	 * ad hoc network.  It assumes that the IP address takes the form of 10.11.12.*.
//	 * 
//	 * @return The name of the local network interface with an IP address of the form
//	 * 10.11.12.*.  This is assumed to be the Pharos wireless ad hoc network.  If no
//	 * such network is found, null is returned.
//	 */
//	// ad hoc network.  
//    public static String getPharosNetworkInterface() {
//
//    	Enumeration<NetworkInterface> ifEnum;
//		try {
//			ifEnum = NetworkInterface.getNetworkInterfaces();
//			while (ifEnum.hasMoreElements()) {
//				NetworkInterface ni = ifEnum.nextElement();
//				//System.out.println("network interface name = \"" + ni.getName() + "\"");
//				Enumeration<InetAddress> ipEnum = ni.getInetAddresses();
//				while (ipEnum.hasMoreElements()) {
//					InetAddress addr = ipEnum.nextElement();
//					//System.out.println("\tip address=" + addr.getHostAddress());
//					if (addr.getHostAddress().contains("10.11.12")) {
//						String result = ni.getName();
//						//System.out.println("Found! Network interface \"" + result + "\"");
//						return result;
//					}
//					
//				}
//			}
//		} catch (SocketException e) {
//			e.printStackTrace();
//		}
//		return null;
//    }
    
    public static void main(String[] args) {
    	String mcastAddressString = "230.1.2.3";
    	int mCastPort = 6000;
    	String pharosNetworkInterfaceName;
		try {
			pharosNetworkInterfaceName = pharoslabut.RobotIPAssignments.getAdHocNetworkInterface();
			if (pharosNetworkInterfaceName != null) {
	    		WiFiBeaconReceiver br = new WiFiBeaconReceiver(mcastAddressString, mCastPort, pharosNetworkInterfaceName);
	    		br.start();
	    	} else {
	    		System.err.println("Unable to find pharos network interface.");
	    	}
		} catch (PharosException e) {
			e.printStackTrace();
		}
    }
}
