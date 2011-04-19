package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.io.Message;

public class ReservationTimeMsg extends Msg implements Message {
	
	private static final long serialVersionUID = 7593577761036988454L;
	private long ETA;
	
	public ReservationTimeMsg(int robotID, long ETA) {
		super(robotID);
		this.ETA = ETA;
	}
	
	/**
     *  @return ETA
     */
    public long getETA() {
        return this.ETA;
    }
	
	/**
     *  this method sets a new ETA for the robot
     * @param eta - The estimated time of arrival
     */
    public void setETA(long eta) {
        this.ETA = eta;
//        this.ETA = ETA + new Date().getTime() - Main.startTime;
    }
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return "ReservationTimeMsg";
	}

}
