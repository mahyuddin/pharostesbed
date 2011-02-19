package pharoslabut.demo.irobotcam;

import pharoslabut.io.Message;

public class CameraTiltMsg extends Message {
	private static final long serialVersionUID = 3948105907393563361L;
	double tiltAngle;
	
	public CameraTiltMsg(double tiltAngle) {
		this.tiltAngle = tiltAngle;
	}
	
	/**
	 * Returns the desired pan angle.
	 * 
	 * @return the pan angle.
	 */
	public double getPanAngle() {
		return tiltAngle;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}

}
