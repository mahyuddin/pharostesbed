package pharoslabut.demo.simonsays;

import pharoslabut.io.Message;

public class CameraTiltMsg extends Message {
	private static final long serialVersionUID = 3948105907393563361L;
	double tiltAngle;
	
	public CameraTiltMsg(double tiltAngle) {
		this.tiltAngle = tiltAngle;
	}
	
	/**
	 * Returns the desired tilt angle.
	 * 
	 * @return the tilt angle in degrees.
	 */
	public double getTiltAngle() {
		return tiltAngle;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}

}
