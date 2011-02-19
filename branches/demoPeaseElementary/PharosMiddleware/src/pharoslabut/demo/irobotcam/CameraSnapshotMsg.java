package pharoslabut.demo.irobotcam;


/**
 * This message is sent by the Demo Server to the Demo Client after
 * it takes the snapshot.  It contains a single image taken by the camera.
 * 
 * @author Chien-Liang Fok
 */
public class CameraSnapshotMsg extends CmdDoneMsg {
	
	private static final long serialVersionUID = 5052346433108498626L;

	// Add data field for storing the image
	
	/**
	 * The constructor.
	 * 
	 * @param success Whether the snapshot was a success.
	 */
	public CameraSnapshotMsg(boolean success) {
		super(success);
		
	}
	
	// Add access to image data
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
}
