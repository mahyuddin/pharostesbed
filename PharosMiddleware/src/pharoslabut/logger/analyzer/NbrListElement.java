package pharoslabut.logger.analyzer;

import java.net.InetAddress;

import pharoslabut.RobotIPAssignments;

/**
 * An element within a neighbor list.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.logger.analyzer.NodeConnectivityStats
 * @see pharoslabut.logger.analyzer.ExpConnectivityStats
 * @see pharoslabut.logger.analyzer.NbrList
 */
public class NbrListElement {
	long timeAdded;
	long timeLastUpdated;
	InetAddress ipAddress;
	int port;
	
	public NbrListElement(WiFiBeaconRx beacon) {
		this.ipAddress = beacon.getBeacon().getAddress();
		this.port = beacon.getBeacon().getPort();
		this.timeAdded = beacon.getTimestamp();
		this.timeLastUpdated = beacon.getTimestamp(); // initially the last updated time is the current time
	}
	
	public boolean ownsBeacon(WiFiBeaconRx beacon) {
		return beacon.getBeacon().getAddress().equals(ipAddress) 
			&& beacon.getBeacon().getPort() == port;
	}
	
	public int getNodeID() {
		return RobotIPAssignments.getID(ipAddress);
	}
	
	public String toString() {
		return ipAddress + ":" + port;
	}
}