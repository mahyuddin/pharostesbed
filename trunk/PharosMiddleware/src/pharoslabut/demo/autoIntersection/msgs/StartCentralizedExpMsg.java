package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.demo.autoIntersection.ExpType;

/**
 * This is transmitted from the ExpMgr to the AutoIntersetionClient
 * to initiate a patrol experiment.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.ExpMgr
 * @see pharoslabut.demo.autoIntersection.AutoIntersectionClient
 */
public class StartCentralizedExpMsg extends StartAdHocExpMsg {

	private static final long serialVersionUID = -1322699976908956288L;

	String serverIP;
	
	int serverPort;
	
	public StartCentralizedExpMsg(String expName, ExpType expType, String serverIP, int serverPort) {
		super(expName, expType);
		this.serverIP = serverIP;
		this.serverPort = serverPort;
	}
	
	public String getServerIP() {
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
