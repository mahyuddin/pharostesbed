package pharoslabut.demo.autoIntersection.clientDaemons.V2VParallel;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.V2VSerialBeacon;

/**
 * This is the beacon used in the ad hoc / parallel autonomous intersection.
 * 
 * @author Chien-Liang Fok
 */
public class V2VParallelBeacon extends V2VSerialBeacon {
	
	private static final long serialVersionUID = -6304254516078221644L;

	/**
	 * The entry point.
	 */
	private String entryPointID;
	
	/**
	 * The exit point.
	 */
	private String exitPointID;
	
	/**
	 * The constructor.
	 * 
	 * @param address The address of this host.
	 * @param port The single-cast port number being used.
	 * @param entryPointID The entry point.
	 * @param exitPointID The exit point.
	 */
	public V2VParallelBeacon(InetAddress address, int port, String entryPointID, String exitPointID) {
		super(address, port);
		this.entryPointID = entryPointID;
		this.exitPointID = exitPointID;
	}
	
	/**
	 * 
	 * @return The entry point.
	 */
	public String getEntryPointID() {
		return entryPointID;
	}
	
	/**
	 * 
	 * @return The exit point.
	 */
	public String getExitPointID() {
		return exitPointID;
	}
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		return super.toString() + ", entryPointID=" + entryPointID + ", exitPointID=" + exitPointID;
	}
	
}
