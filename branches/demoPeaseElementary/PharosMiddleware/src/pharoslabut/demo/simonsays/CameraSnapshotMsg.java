package pharoslabut.demo.simonsays;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * This message is sent by the Demo Server to the Demo Client after
 * it takes the snapshot.  It contains a single image taken by the camera.
 * 
 * @author Chien-Liang Fok
 */
public class CameraSnapshotMsg extends CmdDoneMsg {
	
	private static final long serialVersionUID = 5052346433108498626L;

	// Add data field for storing the image
	byte[] imagePixels;
	int width, height;
	
	/**
	 * The constructor.
	 * 
	 * @param success Whether the snapshot was a success.
	 */
	public CameraSnapshotMsg(boolean success) {
		super(success);
		
	}
	
	public int getImageSize() {
		if (imagePixels != null)
			return imagePixels.length;
		else
			return 0;
	}
	
	/**
	 * Saves the image data in this message.
	 * 
	 * @param image The image to save.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 */
	public void setImage(Image image, int width, int height) throws IOException {
		this.width = width;
		this.height = height;
		
//		imagePixels = new int[width * height];
//		PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, imagePixels, 0, width);
//		try {
//			pg.grabPixels();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		// Compress the image into a JPEG and then save its bytes into the imagePixels array.
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(toBufferedImage(image), "jpg", baos);
		byte[] bytes = baos.toByteArray();
		imagePixels = new byte[bytes.length];
		System.arraycopy(bytes, 0, imagePixels, 0, bytes.length);
	}
	
	public BufferedImage getImage() throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(imagePixels);
		return ImageIO.read(bais);
//		MemoryImageSource mis = new MemoryImageSource(width, height, imagePixels, 0, width);
//		Toolkit tk = Toolkit.getDefaultToolkit();
//		return tk.createImage(mis);

	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	// This method returns a buffered image with the contents of an image
	// Source: http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
	public static BufferedImage toBufferedImage(Image image) {
	    if (image instanceof BufferedImage) {
	        return (BufferedImage)image;
	    }

	    // This code ensures that all the pixels in the image are loaded
	    image = new ImageIcon(image).getImage();

	    // Determine if the image has transparent pixels; for this method's
	    // implementation, see Determining If an Image Has Transparent Pixels
	    boolean hasAlpha = hasAlpha(image);

	    // Create a buffered image with a format that's compatible with the screen
	    BufferedImage bimage = null;
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    try {
	        // Determine the type of transparency of the new buffered image
	        int transparency = Transparency.OPAQUE;
	        if (hasAlpha) {
	            transparency = Transparency.BITMASK;
	        }

	        // Create the buffered image
	        GraphicsDevice gs = ge.getDefaultScreenDevice();
	        GraphicsConfiguration gc = gs.getDefaultConfiguration();
	        bimage = gc.createCompatibleImage(
	            image.getWidth(null), image.getHeight(null), transparency);
	    } catch (HeadlessException e) {
	        // The system does not have a screen
	    }

	    if (bimage == null) {
	        // Create a buffered image using the default color model
	        int type = BufferedImage.TYPE_INT_RGB;
	        if (hasAlpha) {
	            type = BufferedImage.TYPE_INT_ARGB;
	        }
	        bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
	    }

	    // Copy image to buffered image
	    Graphics g = bimage.createGraphics();

	    // Paint the image onto the buffered image
	    g.drawImage(image, 0, 0, null);
	    g.dispose();

	    return bimage;
	}
	
	// This method returns true if the specified image has transparent pixels
	// Source: http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html
	public static boolean hasAlpha(Image image) {
	    // If buffered image, the color model is readily available
	    if (image instanceof BufferedImage) {
	        BufferedImage bimage = (BufferedImage)image;
	        return bimage.getColorModel().hasAlpha();
	    }

	    // Use a pixel grabber to retrieve the image's color model;
	    // grabbing a single pixel is usually sufficient
	     PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
	    try {
	        pg.grabPixels();
	    } catch (InterruptedException e) {
	    }

	    // Get the image's color model
	    ColorModel cm = pg.getColorModel();
	    return cm.hasAlpha();
	}
	
}
