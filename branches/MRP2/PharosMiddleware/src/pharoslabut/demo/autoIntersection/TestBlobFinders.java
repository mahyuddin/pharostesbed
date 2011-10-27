package pharoslabut.demo.autoIntersection;

import pharoslabut.tests.TestBlobFinder;

/**
 * Test the blob finder on all of the robots being used in the autonomous intersection.
 * 
 * @author Chien-Liang Fok
 */
public class TestBlobFinders {
	String vehicles[] = {"czechvar", "shiner", "ziegen", "manny"};
		
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the robot.
	 * @param serverPort The port on which the robot is listening.
	 */
	public TestBlobFinders() {
		
		// connect to player servers
		for (int i=0; i < vehicles.length; i++) {
			final int j = i;
			new Thread(new Runnable() {
				public void run() {
					new TestBlobFinder(vehicles[j], 6665);
				}
			}).start();
		}
	}
	
//	private class BlobReceiver implements Runnable {
//		BlobFinderVisualizer visualizer;
//		BlobfinderInterface bfi;
//		
//		public BlobReceiver(BlobFinderVisualizer visualizer, BlobfinderInterface bfi) {
//			this.visualizer = visualizer;
//			this.bfi = bfi;
//			new Thread(this).start();
//		}
//		
//		public void run() {
//			boolean done = false;
//			
//			Logger.log("Thread starting.");
//			while(!done) {
//				if (visualizer != null && !visualizer.isVisible())
//					done = true;
//				if (!done && bfi.isDataReady()) {
//					PlayerBlobfinderData blobData = bfi.getData();
//					if (blobData != null) {
//						if (visualizer != null)
//							visualizer.visualizeBlobs(blobData);
//					}
//				}
//				pause(100);
//			} // end while(true)
//			
//			Logger.log("Thread exiting.");
//		}
//	}
	
//	private void log(String msg) {
//		System.out.println(msg);
//		if (flogger != null) 
//			flogger.log(msg);
//	}

//	private void pause(int duration) {
//		synchronized(this) {
//			try {
//				wait(duration);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
//	private static void usage() {
//		System.err.println("Usage: " + TestBlobFinders.class.getName() + " <options>\n");
//		System.err.println("Where <options> include:");
//		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
//		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
//		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
//	}
	
	public static void main(String[] args) {
//		String serverIP = "localhost";
//		int serverPort = 6665;
//		
//		try {
//			for (int i=0; i < args.length; i++) {
//				if (args[i].equals("-server")) {
//					serverIP = args[++i];
//				} 
//				else if (args[i].equals("-port")) {
//					serverPort = Integer.valueOf(args[++i]);
//				}
//				else if (args[i].equals("-log")) {
//					Logger.setFileLogger(new FileLogger(args[++i], false)); 
//				} else if (args[i].equals("-h")) {
//					usage();
//					System.exit(0);
//				} else {
//					System.err.println("Unknown option: " + args[i]);
//					usage();
//					System.exit(1);
//				}
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
// 
//		System.out.println("Server IP: " + serverIP);
//		System.out.println("Server port: " + serverPort);
		
		new TestBlobFinders();
	}
}