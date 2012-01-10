package pharoslabut.cpsAssert;

import pharoslabut.demo.simonsays.io.CmdDoneMsg;
import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.Logger;

public class AssertionRequestThread extends Thread{
	
	AssertionRequestMsg arMsg = null;
	TCPMessageSender sender = null;
	
	/**
	 * local is true if the thread should execute locally on the client side
	 */
	boolean local = false;
	
	/**
	 * the time when the AssertionRequestThread began running
	 */
	long startTime;
	
	public AssertionRequestThread(AssertionRequestMsg ar, TCPMessageSender sdr, boolean local) {
		this.arMsg = ar;
		this.sender = sdr;
		this.local = local;
	}
	
	@Override
	public void run() {
		
		startTime = System.currentTimeMillis();
		
//		if (!local && !arMsg.isBlocking()) { // non-blocking
//			CmdDoneMsg cdm = new CmdDoneMsg(true);
//			try {
//				sender.sendMessage(arMsg.getReplyAddr(), arMsg.getPort(), cdm);
//			} catch (PharosException e) {
//				e.printStackTrace();
//				Logger.logErr("Failed to send ack for AssertionRequestMsg: " + arMsg + ", error: " + e);
//			}
//		}
			
		AssertionThread at = null;
		// TODO parse data out of arMsg
		// TODO call the appropriate assertion
		switch(arMsg.getSensorType()) {
			case CRICKET: 
				try {
					at = CPSAssertSensor.AssertCricket("", (Double)arMsg.getExpectedValues()[0], arMsg.getInequality()[0], (Double)arMsg.getDeltaValues()[0], arMsg.isBlocking());
				} catch (NoNewDataException e1) {
					e1.printStackTrace();
				}
				break;
			case CAMERA_LOCALIZATION:
				try {
					at = CPSAssertSensor.AssertCameraLocalization((Double)arMsg.getExpectedValues()[0], (Double)arMsg.getExpectedValues()[1], arMsg.getInequality()[0], arMsg.getInequality()[1], (Double)arMsg.getDeltaValues()[0], (Double)arMsg.getDeltaValues()[0], arMsg.isBlocking());
				} catch (NoNewDataException e1) {
					e1.printStackTrace();
				}
				break;
				
			default: break;
		}
		
		// if it's blocking, it's already finished, and calling at.join() is pointless
		// if it's non-blocking, also wait for it to finish because an ack has already been sent back, and this is its own thread anyway.
		if (!arMsg.isBlocking()) {	
			try {
				at.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		// at this point, the assertion has finished no matter what
		// therefore, getting the resultMessage is okay
		// -- if the thread was locally executed, then the assertion result will already have been printed
//		if (!local) {
//			try {
//				AssertionResponseMsg respMsg = new AssertionResponseMsg(at.getResultMessage(), startTime, System.currentTimeMillis());
//				sender.sendMessage(arMsg.getReplyAddr(), arMsg.getPort(), respMsg);
//			} catch (PharosException e) {
//				e.printStackTrace();
//				Logger.logErr("Failed to send AssertionResponseMessage for " + arMsg + ", error=" + e);
//			}
//		}
		
		// at this point, the response message has already been sent no matter what
		// if it was non-blocking, the ack was already sent
		// if it's blocking, then send the ack after the response message has been sent (here)
//		if (!local && arMsg.isBlocking()) { 
//			CmdDoneMsg cdm = new CmdDoneMsg(true);
//			try {
//				sender.sendMessage(arMsg.getReplyAddr(), arMsg.getPort(), cdm);
//			} catch (PharosException e) {
//				e.printStackTrace();
//				Logger.logErr("Failed to send ack for AssertionRequestMsg.");
//			}
//		}
	}
	
	
	public boolean isBlocking() {
		return arMsg.isBlocking();
	}

}
