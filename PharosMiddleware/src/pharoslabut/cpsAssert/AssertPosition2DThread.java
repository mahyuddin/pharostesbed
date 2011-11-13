package pharoslabut.cpsAssert;

public class AssertPosition2DThread extends AssertionThread {

	Number expectedY;
	Number actualY; 
	Number deltaY;
	Inequality ineqY; 
	
	public AssertPosition2DThread(String message, Number expectedX, Number expectedY, 
			Number actualX, Number actualY, Number deltaX, Number deltaY, Inequality ineqX, Inequality ineqY) {
		
		super(message, expectedX, actualX, deltaX, ineqX);
		this.expectedY = expectedY;
		this.actualY = actualY;
		this.deltaY = deltaY;
		this.ineqY = ineqY;
	}

	@Override
	public void run() {
		CPSAssertNumerical.AssertInequality("Asserted that the current " + message + " x-value was " + ineq.toString() + " the expected value.",
				(Double)expected, (Double)actual, (Double)delta, ineq);		
		CPSAssertNumerical.AssertInequality("Asserted that the current " + message + " y-value was " + ineq.toString() + " the expected value.",
				(Double)expectedY, (Double)actualY, (Double)deltaY, ineqY);		
	}
	

}
