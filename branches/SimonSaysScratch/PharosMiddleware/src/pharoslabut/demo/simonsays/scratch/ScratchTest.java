package pharoslabut.demo.simonsays.scratch;

import pharoslabut.demo.simonsays.CreateRobotInterface;

public class ScratchTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("*** Starting ***");
		
		// When interfacing with CreateRobotInterface, use "localhost" on port 6665.
		//System.out.println("Creating robot interface");
		//CreateRobotInterface robot = new CreateRobotInterface("localhost", 6665);
		
		System.out.println("Creating Scratch interface");
		ScratchIO sio = new ScratchIO();
		
		while (true) {
			System.out.println("Waiting for message...");
			ScratchMessage msg = sio.readMsg();
			
			System.out.println("Received msg: " + msg);
			
			if (msg != null) {
				if (msg.getName().equals("turn")) {
					System.out.println("TURN " + msg.getValue());
				}
				else {
					System.out.println("ERROR");
				}
			}
		}
		
		//System.out.println("*** Done ***");
	}

}
