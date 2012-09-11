package pharoslabut.demo.simonsays;

import pharoslabut.cpsAssert.AssertionRequestThread;
//import pharoslabut.io.AckableMessage;

public class LocalAssertionCommand extends Instruction {
	
	private AssertionRequestThread arThr = null;
	
	
	public LocalAssertionCommand(AssertionRequestThread art, int line) {
		super(null, line);
		this.arThr = art;
	}
	
	
	public AssertionRequestThread getAssertionRequestThread() {
		return arThr;
	}
	
}
