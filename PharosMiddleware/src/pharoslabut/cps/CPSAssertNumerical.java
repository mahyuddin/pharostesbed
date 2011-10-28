
package pharoslabut.cps;

//import static org.junit.Assert.*;
//import org.junit.AfterClass;
//import org.junit.Test;

import java.util.Calendar;




/**
 * @author Kevin Boos
 * Numerical-level (mathematical) assertions. <br>
 * Is used by the CPSAssert class 
 */
public class CPSAssertNumerical {
	
	/**
	 * Formats the current time into a String.
	 * @return the current time as a String, formatted as: HH:MM:SS.mil, such as 22:45:06.934
	 */
	private static String getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + 
				":" + cal.get(Calendar.SECOND) + "." + cal.get(Calendar.MILLISECOND);
	}
	

	
	// AssertEquals methods for type Double
	public static void AssertEquals(Double expected, Double actual) {
		AssertEquals("", expected, actual, 0.0);
	}
	
	public static void AssertEquals(String message, Double expected, Double actual) {
		AssertEquals(message, expected, actual, 0.0);
	}
	
	public static void AssertEquals(Double expected, Double actual, Double delta) {
		AssertEquals("", expected, actual, delta);
	}
	
	public static void AssertEquals(String message, Double expected, Double actual, Double delta) {
		AssertInequality(message, expected, actual, delta, Inequality.EQUAL);
	}
		
	// AssertEquals methods for type Integer
	public static void AssertEquals(Integer expected, Integer actual) {
		AssertEquals("", expected, actual, 0);
	}

	public static void AssertEquals(String message, Integer expected, Integer actual) {
		AssertEquals(message, expected, actual, 0);
	}
	
	public static void AssertEquals(Integer expected, Integer actual, Integer delta) {
		AssertEquals("", expected, actual, delta);
	}
	
	public static void AssertEquals(String message, Integer expected, Integer actual, Integer delta) {
		AssertInequality(message, expected, actual, delta, Inequality.EQUAL);
	}	
	
	
	
	// AssertInequality methods for type Double
	public static void AssertInequality(Double expected, Double actual, Inequality type) {
		AssertInequality("", expected, actual, 0.0, type);
	}

	public static void AssertInequality(String message, Double expected, Double actual, Inequality type) {
		AssertInequality(message, expected, actual, 0.0, type);
	}

	public static void AssertInequality(Double expected, Double actual, Double delta, Inequality type) {
		AssertInequality("", expected, actual, delta, type);
	}

	public static void AssertInequality(String message, Double expected, Double actual, Double delta, Inequality type) {
		Double marginOfError = Math.abs(delta);
		String ineq = "";
		boolean condition = false;
		switch (type) {
			case LESS_THAN: condition = expected < actual + marginOfError; ineq = "<"; break;
			case LESS_THAN_EQUAL_TO: condition = expected <= actual + marginOfError; ineq = "<="; break;
			case EQUAL: condition = (expected >= actual - marginOfError) && (expected <= actual + marginOfError); ineq = "=="; break;
			case GREATER_THAN: condition = expected > actual - marginOfError; ineq = ">"; break;
			case GREATER_THAN_EQUAL_TO: condition = expected >= actual - marginOfError; ineq = ">="; break;
		}
		System.out.println(getCurrentTime() + " -- Assertion " + (condition ? "Passed." : "Failed.") +
				(!(message == "" || message == null) ? ("   Message: " + message) : ""));
		System.out.println("             -- (Expected: " + expected + ") " + ineq + " (Actual: " + actual + "), Error Margin = " + delta + "\n");
	}


	// AssertInequality methods for type Integer
	public static void AssertInequality(Integer expected, Integer actual, Inequality type) {
		AssertInequality("", expected, actual, 0, type);
	}

	public static void AssertInequality(String message, Integer expected, Integer actual, Inequality type) {
		AssertInequality(message, expected, actual, 0, type);
	}

	public static void AssertInequality(Integer expected, Integer actual, Integer delta, Inequality type) {
		AssertInequality("", expected, actual, delta, type);
	}

	public static void AssertInequality(String message, Integer expected, Integer actual, Integer delta, Inequality type) {
		Integer marginOfError = Math.abs(delta);
		String ineq = "";
		boolean condition = false;
		switch (type) {
			case LESS_THAN: condition = expected < actual + marginOfError; ineq = "<"; break;
			case LESS_THAN_EQUAL_TO: condition = expected <= actual + marginOfError; ineq = "<="; break;
			case EQUAL: condition = (expected >= actual - marginOfError) && (expected <= actual + marginOfError); ineq = "=="; break;
			case GREATER_THAN: condition = expected > actual - marginOfError; ineq = ">"; break;
			case GREATER_THAN_EQUAL_TO: condition = expected >= actual - marginOfError; ineq = ">="; break;
		}
		System.out.println(getCurrentTime() + " -- Assertion " + (condition ? "Passed." : "Failed.") +
				(!(message == "" || message == null) ? ("   Message: " + message) : ""));
		System.out.println("             -- (Expected: " + expected + ") " + ineq + " (Actual: " + actual + "), Error Margin = " + delta + "\n");
	}

	

	// AssertTrue methods
	public static void AssertTrue(boolean condition) {
		AssertTrue("", condition);
	}

	public static void AssertTrue(String message, boolean condition) {
		System.out.println(getCurrentTime() + " -- Assertion True " + (condition ? "Passed." : "Failed.") +
				(!(message == "" || message == null) ? ("   Message: " + message) : "") + "\n");
	}

	
	// AssertFalse methods
	public static void AssertFalse(boolean condition) {
		AssertFalse("", condition);
	}

	public static void AssertFalse(String message, boolean condition) {
		System.out.println(getCurrentTime() + " -- Assertion False " + (!condition ? "Passed." : "Failed.") +
				(!(message == "" || message == null) ? ("   Message: " + message) : "") + "\n");
	}





	public static void main (String []args) {
		AssertEquals(5.0, 5.0);
		AssertEquals(2,0,1);
		AssertEquals(2d,1.5,0.0);
		AssertEquals("testMsg", 3d, 2d, 0.2);
		AssertTrue(3>2);
		AssertFalse(2>3);
		AssertInequality("testMsg2.", 3.5, 4.6, 2.0, Inequality.LESS_THAN_EQUAL_TO);
		AssertInequality("testMsgInteger.", 4, 8, 2, Inequality.LESS_THAN);
	}	

}
