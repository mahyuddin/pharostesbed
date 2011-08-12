package pharoslabut.tests;

import pharoslabut.navigate.motionscript.*;;

/**
 * Tests the ability to read in a motion script.
 * 
 * @author Chien-Liang Fok
 */
public class TestMotionScript {
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.tests.TestMotionScript <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-script <motion script file name>: name of motion script to evaluate (required)");
		System.err.println("\t-d or -debug: enable debug mode.");
	}
	
	public static void main(String[] args) {
		String motionScriptFileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-script")) {
					motionScriptFileName = args[++i];
				} else if (args[i].equals("-h")) {
					usage();
					System.exit(0);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}  else {
					System.err.println("Unknown option: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}		
		MotionScript script = new MotionScript(motionScriptFileName);
		System.out.println(script.toString());
	}
}
