package pharoslabut.behavior.management;

/**
 * The state of a node in the world.
 * 
 * @author Chien-Liang Fok
 * @author Noa Agmon
 * @see pharoslabut.demo.mrpatrol2.context.WorldModel
 */
public class RobotBehaviorState {
	private String robotIP;
	private int robotPort;
	private int behaviorID;
	private String behaviorName;
	
	/**
	 * The age of the current behavior.  This is to discriminate between the
	 * newer and older behavior states and to ensure that the behavior states
	 * are always updated to newer ones.
	 */
	private long age;

	/**
	 * The constructor.
	 * 
	 * @param robotIP
	 * @param robotPort
	 * @param behaveID
	 */
	public RobotBehaviorState(String robotIP, int robotPort) {
		this.robotIP = robotIP;
		this.robotPort = robotPort;
	}
	
	public void setBehaviorID(int behaviorID) {
		this.behaviorID = behaviorID;
	}
	
	public int getBehaviorID() {
		return behaviorID;
	}
	
	public void setBehaviorName(String behaviorName) {
		this.behaviorName = behaviorName;
	}
	public String getBehaviorName() {
		return behaviorName;
	}
	
	public void setAge(long age) {
		this.age = age;
	}
	
	public long getAge() {
		return age;
	}
	
	public String toString() {
		return "robotIP=" + robotIP + ", robotPort=" + robotPort 
		+ ", behaviorID=" + behaviorID + ", behaviorName=" + behaviorName
		+ ", agee=" + age;
	}
}
