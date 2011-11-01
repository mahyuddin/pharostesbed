package pharoslabut.demo.simonsays;

public class AssertionTuple {

	/**
	 * the actual physical starting value
	 */
	private Object startingValue;
	
	/**
	 * the expected change in value, i.e., what <b>startingValue - endingValue</b> should be
	 */
	private Object expectedDelta;
	
	/**
	 * the actual physical ending value
	 */
	private Object endingValue;

	public AssertionTuple() {}
	
	/**
	 * Creates a new AssertionTuple with the specified values
	 * @param start 
	 * @param expected
	 * @param end
	 */
	public AssertionTuple(Object start, Object expected, Object end) {
		this.startingValue = start;
		this.expectedDelta = expected;
		this.endingValue = end;
	}
	
	/**
	 * @return the startingValue
	 */
	public Object getStartingValue() {
		return startingValue;
	}

	/**
	 * @param startingValue the startingValue to set
	 */
	public void setStartingValue(Object startingValue) {
		this.startingValue = startingValue;
	}

	/**
	 * @return the expectedDelta
	 */
	public Object getExpectedDelta() {
		return expectedDelta;
	}

	/**
	 * @param expectedDelta the expectedDelta to set
	 */
	public void setExpectedDelta(Object expectedDelta) {
		this.expectedDelta = expectedDelta;
	}

	/**
	 * @return the endingValue
	 */
	public Object getEndingValue() {
		return endingValue;
	}

	/**
	 * @param endingValue the endingValue to set
	 */
	public void setEndingValue(Object endingValue) {
		this.endingValue = endingValue;
	}
}
