package pharoslabut.demo.mrpatrol2.behaviors;

import java.util.Vector;

public class BehaviorDynamicAnticipated extends Behavior {
	
	private BehaviorAnticipatedUpdateBeacon nextUpdate;
	
	private BehaviorCoordination prevCoordBeh;
	
	private BehaviorAnticipatedUpdateBeacon behUpdateBeacon;
	
	public BehaviorDynamicAnticipated(String name, 
			BehaviorCoordination prevCoordBeh, BehaviorAnticipatedUpdateBeacon nextUpdate,
			BehaviorAnticipatedUpdateBeacon behUpdateBeacon) {
		super(name);
		this.nextUpdate = nextUpdate;
		this.prevCoordBeh = prevCoordBeh;
		this.behUpdateBeacon = behUpdateBeacon;
	}
	
	private long computeAheadTime(Vector<Long> latencies) {
		long aheadTime;
		
		long prevAheadTime = behUpdateBeacon.getAheadTime();
		//...
		return aheadTime;
	}

	@Override
	public void run() {
		
		// exchange latencies of previous coordination with active teammates
		
		// compute the next aheadTime
		long aheadTime = ...;
		
		nextUpdate.setAheadTime(aheadTime);

	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
