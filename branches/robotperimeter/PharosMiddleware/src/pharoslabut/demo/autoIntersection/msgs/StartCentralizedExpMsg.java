package pharoslabut.demo.autoIntersection.msgs;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.ExpType;

/**
 * This is transmitted from the ExpMgr to the AutoIntersetionClient
 * to initiate a patrol experiment.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.AutoIntersectionExpMgr
 * @see pharoslabut.demo.autoIntersection.AutoIntersectionClient
 */
public class StartCentralizedExpMsg extends StartExpMsg {

	private static final long serialVersionUID = -1322699976908956288L;

	InetAddress serverIP;
	
	int serverPort;
	
	public StartCentralizedExpMsg(String expName, ExpType expType, InetAddress serverIP, int serverPort) {
		super(expName, expType);
		this.serverIP = serverIP;
		this.serverPort = serverPort;
	}
	
	public InetAddress getServerIP() {
		return serverIP;
	}
	
	public int getServerPort() {
		return serverPort;
	}

	public String toString() {
		return getClass().getName() + ": expName = " + getExpName() + ", expType = " + getExpType() 
			+ ", serverIP = " + serverIP + ", serverPort=" + serverPort;
	}
}
