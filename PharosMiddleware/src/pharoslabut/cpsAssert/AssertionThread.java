package pharoslabut.cpsAssert;

public class AssertionThread extends Thread{

	String message; 
	Number expected;
	Number actual; 
	Number delta;
	Inequality ineq; 
	
	
	public AssertionThread(String message, Number expectedValue, Number actualValue, Number delta, Inequality ineq) {
		this.message = message;
		this.expected = expectedValue;
		this.actual = actualValue;
		this.delta = delta;
		this.ineq = ineq;
	}
	
	@Override
	public void run() {
		
		// could sleep, wait, etc
		
		if (expected instanceof Double || actual instanceof Double) {
			if (message == null) 
				message = "Asserted that an undescribed actual (Double) value was " + ineq.toString() + " the expected (Double) value.";
			CPSAssertNumerical.AssertInequality(message, (Double)expected, (Double)actual, (Double)delta, ineq);
		} 
		else {
			if (message == null) 
				message = "Asserted that an undescribed actual (Integer) value was " + ineq.toString() + " the expected (Integer) value.";
			CPSAssertNumerical.AssertInequality(message, (Integer)expected, (Integer)actual, (Integer)delta, ineq);
		}
			
	}

}
