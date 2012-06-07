package pharoslabut.demo.mrpatrol2.behaviors;

/**
 * Tests BehaviorBeacon.
 * 
 * @author Chien-Liang Fok
 */
public class TestBehaviorBeacon {

	public TestBehaviorBeacon() {
		String name = "Behavior_Beacon_Tester";
		String mCastAddress = "230.1.2.3";
		int mCastPort = 6000;
		int serverPort = 7776;
		
		BehaviorBeacon bb = new BehaviorBeacon(name, mCastAddress, mCastPort, serverPort);
		
		System.out.println("Behavior: " + bb);
		
		System.out.println("Starting behavior.");
		bb.start();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0)
			System.setProperty ("PharosMiddleware.debug", "true"); // enable debug mode.
		new TestBehaviorBeacon();
	}

}
