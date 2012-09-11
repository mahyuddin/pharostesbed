package pharoslabut.cpsAssert;

public class AssertionThreadPosition2d extends AssertionThread {

	Number expectedY;
	Number actualY; 
	Number deltaY;
	Inequality ineqY; 
	String resultMessageY;
	
	
	public AssertionThreadPosition2d(String message, Number expectedX, Number expectedY, 
			Number actualX, Number actualY, Number deltaX, Number deltaY, Inequality ineqX, Inequality ineqY) {
		
		super(message, expectedX, actualX, deltaX, ineqX);
		this.expectedY = expectedY;
		this.actualY = actualY;
		this.deltaY = deltaY;
		this.ineqY = ineqY;
	}

	@Override
	public void run() {
		if (expected instanceof Double || actual instanceof Double) {
				resultMessage = CPSAssertNumerical.AssertInequality("Asserted that the current " + message + " x-value was " + ineq.toString() + " the expected value.",
						(Double)expected, (Double)actual, (Double)delta, ineq);		
				resultMessageY = CPSAssertNumerical.AssertInequality("Asserted that the current " + message + " y-value was " + ineq.toString() + " the expected value.",
						(Double)expectedY, (Double)actualY, (Double)deltaY, ineqY);
		} 
		else {
			resultMessage = CPSAssertNumerical.AssertInequality("Asserted that the current " + message + " x-value was " + ineq.toString() + " the expected value.",
					(Integer)expected, (Integer)actual, (Integer)delta, ineq);		
			resultMessageY = CPSAssertNumerical.AssertInequality("Asserted that the current " + message + " y-value was " + ineq.toString() + " the expected value.",
					(Integer)expectedY, (Integer)actualY, (Integer)deltaY, ineqY);
		}
				
	}
	

}
