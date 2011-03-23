package pharoslabut.io;

/**
 * This is sent by the PharosClient to the PharosServer to stop
 * a robot's participation in an experiment.  It assumes that the
 * robot is waiting for a StopExpMsg message to arrive.
 * 
 * @author Chien-Liang Fok
 */
public class StopExpMsg implements Message {
	private static final long serialVersionUID = 6593878637228833693L;

	public StopExpMsg() {
		
	}
	
	@Override
	public MsgType getType() {
		return MsgType.STOPEXP;
	}
}
