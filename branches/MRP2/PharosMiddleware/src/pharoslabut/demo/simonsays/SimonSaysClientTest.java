package pharoslabut.demo.simonsays;

import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.AfterClass;
import org.junit.Test;

public class SimonSaysClientTest {
	static String args[] = {"-testing", "-server", "10.11.12.37", "-debug"};
	private static Vector<AssertionTuple> assertionList = new Vector<AssertionTuple>();
	
	@Test
	public void testRobotMoveStraight() {
		new Thread (
			new Runnable() {
				public void run() {
					SimonSaysClient.main(args);
				}
			}
		).start();
		System.out.println("Waiting for testing to be set...");
		while (!SimonSaysClient.testing) {} // wait for it to be set
		System.out.println(" ... testing was set.");
		
		// until the test is over
		while (SimonSaysClient.testing) {
			try {
				System.out.println("Waiting for new Assertion Tuple...");
				synchronized(assertionList) {
					assertionList.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			AssertionTuple tup = null;
			
			synchronized (assertionList) {
				tup = assertionList.lastElement();
			}
			if (tup != null) {
				System.out.println("New Assertion Tuple Received");
				assertEquals("Beacon Reading", (Double)tup.getStartingValue() + (Double)tup.getExpectedDelta(), (Double)tup.getEndingValue(), 0.05);	
			} 
			else {
				System.out.println("No Assertion Tuple obtained.");
			}
		}
		
	}
	
	@Test
	public void numTest() {
		assertEquals(2, 1+1);
	}

	
	@AfterClass
	public static void cleanUp() {
		System.out.println("\nTests Complete.");
//		System.exit(0);
	}
	
	public static synchronized void startAssertion(AssertionTuple tup) {
		System.out.println("starting Assertion...");
		synchronized(assertionList) {
			assertionList.add(tup);
			assertionList.notify();
		}
	}
	

}
