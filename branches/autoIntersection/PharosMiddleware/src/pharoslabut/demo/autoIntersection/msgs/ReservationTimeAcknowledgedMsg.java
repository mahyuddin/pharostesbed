package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.io.*;

public class ReservationTimeAcknowledgedMsg extends Msg implements Message {

	private static final long serialVersionUID = 3490049826053353698L;

	public ReservationTimeAcknowledgedMsg(int robotID) {
		super(robotID);
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return "ReservationTimeAcknowledgedMsg";
	}
	
}
