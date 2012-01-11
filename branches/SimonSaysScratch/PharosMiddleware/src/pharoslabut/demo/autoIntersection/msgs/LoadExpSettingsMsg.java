package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.io.Message;

/**
 * Contains the specifications for an autonomous intersection experiment.
 * This is sent by an ExpMgr to an AutoIntersectionClient.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.AutoIntersectionExpMgr
 * @see pharoslabut.demo.autoIntersection.AutoIntersectionClient
 */
public class LoadExpSettingsMsg implements Message {

	private static final long serialVersionUID = 722133144971291375L;
	
	/**
	 * The ID of the intersection entry point.
	 */
	private String entryID;
	
	/**
	 * The ID of the intersetion's exit point.
	 */
	private String exitID;
	
	/**
	 * The constructor.
	 * 
	 * @param entryID The ID of the entry point into the intersection.
	 * @param exitID The ID of the exit point from the intersection.
	 */
	public LoadExpSettingsMsg(String entryID, String exitID) {
		this.entryID = entryID;
		this.exitID = exitID;
	}
	
	/**
	 * 
	 * @return The ID of the entry point into the intersection.
	 */
	public String getEntryID() {
		return entryID;
	}
	
	/**
	 * 
	 * @return The ID of the exit point from the intersection.
	 */
	public String getExitID() {
		return exitID;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.LOAD_SETTINGS;
	}
	
	public String toString() {
		return getClass().getName() + ": entryID=" + entryID + ", exitID=" + exitID;
	}
}