package pharoslabut.demo.irobotcam;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

/**
 * This message is sent by the Demo Server to the Demo Client after
 * it takes the snapshot.  It contains a single image taken by the camera.
 * 
 * @author Chien-Liang Fok
 */
public class CameraSnapshotMsg extends CmdDoneMsg {
	
	private static final long serialVersionUID = 5052346433108498626L;

	// Add data field for storing the image
	int[] imagePixels;
	int width, height;
	
	/**
	 * The constructor.
	 * 
	 * @param success Whether the snapshot was a success.
	 */
	public CameraSnapshotMsg(boolean success) {
		super(success);
		
	}
	
	public void setImage(Image image, int width, int height) {
		this.width = width;
		this.height = height;
		
		imagePixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, imagePixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public Image getImage() {
		MemoryImageSource mis = new MemoryImageSource(width, height, imagePixels, 0, width);
		Toolkit tk = Toolkit.getDefaultToolkit();
		return tk.createImage(mis);

	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
}
