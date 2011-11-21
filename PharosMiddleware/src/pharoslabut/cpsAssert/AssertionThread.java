package pharoslabut.cpsAssert;

public class AssertionThread extends Thread{

	String message; 
	Number expected;
	Number actual; 
	Number delta;
	Inequality ineq; 
	String resultMessage = null; 
	
	
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
			resultMessage = CPSAssertNumerical.AssertInequality(message, (Double)expected, (Double)actual, (Double)delta, ineq);
		} 
		else {
			if (message == null) 
				message = "Asserted that an undescribed actual (Integer) value was " + ineq.toString() + " the expected (Integer) value.";
			resultMessage = CPSAssertNumerical.AssertInequality(message, (Integer)expected, (Integer)actual, (Integer)delta, ineq);
		}
			
	}
	
	
	/**
	 * calls an AssertionThread's <code> start() </code> method 
	 * @param blocking whether to run the thread in the background (blocking) or in the foreground (non-blocking)
	 */
	public synchronized void runBlocking(boolean blocking) {
		this.start();
		if (blocking) {
			try {
				synchronized (this) {
					this.join();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}
	
	
	

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the expected
	 */
	public Number getExpected() {
		return expected;
	}

	/**
	 * @return the actual
	 */
	public Number getActual() {
		return actual;
	}

	/**
	 * @return the delta
	 */
	public Number getDelta() {
		return delta;
	}

	/**
	 * @return the ineq
	 */
	public Inequality getIneq() {
		return ineq;
	}

	/**
	 * @return the resultMessage
	 */
	public String getResultMessage() {
		return resultMessage;
	}

}
