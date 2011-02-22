package pharoslabut.demo.irobotcam;

import pharoslabut.io.Message;

/**
 * This message is sent by the Demo Client to the Demo Server telling
 * it to pan the camera to a particular angle.
 * 
 * @author Chien-Liang Fok
 */
public class CameraPanMsg extends Message {
	private static final long serialVersionUID = -6339855504088553235L;
	double panAngle;
	
	public CameraPanMsg(double panAngle) {
		this.panAngle = panAngle;
	}
	
	/**
	 * Returns the desired pan angle.
	 * 
	 * @return the pan angle in degrees.
	 */
	public double getPanAngle() {
		return panAngle;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
}
