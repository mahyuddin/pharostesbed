package pharoslabut.sensors.camera.axis;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.ImageIcon;

//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

/**
 * Provides an API for accessing the Axis 211M camera.
 * 
 */
public class AxisCameraInterface {	
	
	// IMAGE RELATED OBJECTS
	//private Image cameraStream;
	//private BufferedImage bufferedCamera;

	// CAMERA AUTHENTICATION/CONNECTIVITY RELATED OBJECTS
	// Use to authenticate against a HTTP protected camera-stream.
	private MyAuthenticator authenticator;
	
	// Camera URL, name, authentication username and password.
	private URL cameraURL;
	private String cameraURLString;
	private String username;
	private String password;

	// CAMERA CONTROL FIELDS
	//private boolean error = false;
	private boolean isClosed = false;

	/**
	 * Create a new Camera object.
	 * 
	 * @param url
	 * @param username
	 * @param password
	 */
	public AxisCameraInterface(String url, String username, String password) {
		this.cameraURLString = url; // Set the camera URL.
		this.username = username; // Set the camera username.
		this.password = password; // Set the camera password.

		try {
			// Create a new URL object from the URL-string of our camera.
			cameraURL = new URL(this.cameraURLString);
		} catch (MalformedURLException m) {
			m.printStackTrace();
		}
		
		// Check if this camera requires authentication.
		// If so, then create and set the authenticator object.
		if (username != null && password != null) {
			if (!username.equals("") && !password.equals("")) {
				this.authenticator = new MyAuthenticator(this.username,
						this.password);
				Authenticator.setDefault(this.authenticator);
			}
		}
	}


	/**
	 * takes picture, sends cameraStream to the client
	 * 
	 */
	public Image getSnapshot() {

		// If the camera object is null, return.
		if (cameraURL == null) {
			Logger.logErr("cameraURL is null!");
			return null;
		}

		try {
			Logger.log("cameraURL: " + cameraURL);
			// Read the image data from the camera URL.
			return ImageIO.read(cameraURL);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets the URL of the camera.
	 * 
	 * @return
	 */
	public String getCameraURL() {
		return cameraURLString;
	}

	/**
	 * Sets the URL of the camera.
	 * 
	 * @param url
	 */
	public void setCameraURL(String url) {
		cameraURLString = url;
	}

	/**
	 * Gets the username used in the camera authentication process.
	 * 
	 * @return
	 */
	public String getCameraUsername() {
		return username;
	}

	/**
	 * Sets the username used in the camera authentication process.
	 * 
	 * @param username
	 */
	public void setCameraUsername(String username) {
		this.username = username;
		this.authenticator.setUsername(username);
	}

	/**
	 * Gets the password used in the camera authentication process.
	 * 
	 * @return
	 */
	public String getCameraPassword() {
		return this.password;
	}

	/**
	 * Returns true or false if the camera requires authentication.
	 * 
	 * @return
	 */
	public boolean requiresAutentication() {
		return !this.password.equals("") && !this.username.equals("");
	}

	/**
	 * Sets the password used in the camera authentication process.
	 * 
	 * @param password
	 */
	public void setCameraPassword(String password) {
		this.password = password;
		this.authenticator.setPassword(password);
	}

	/**
	 * Used to close the camera. True = closed. False = not closed.
	 * 
	 * @param state
	 */
	public void setClosedState(boolean state) {
		this.isClosed = state;
	}
	
	/**
	 * Get camera close state. True = closed. False = not closed.
	 * 
	 * @return
	 */
	public boolean getCLosedState() {
		return this.isClosed;
	}

	/**
	 * Converts a standard Java Image to a BufferedImage.
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage toBufferedImage(Image image) {

		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded.
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels.
		boolean hasAlpha = false;

		// Create a buffered image with a format that's compatible with the
		// screen.
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();

		try {

			// Determine the type of transparency of the new buffered image.
			int transparency = Transparency.OPAQUE;

			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image...
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();

			bimage = gc.createCompatibleImage(image.getWidth(null), image
					.getHeight(null), transparency);

		} catch (HeadlessException e) {
		}

		if (bimage == null) {

			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image
					.getHeight(null), type);

		} // end if bimage == null

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image and dispose of the graphics.
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;

	} // end public static BufferedImage toBufferedImage

	/**
	 * Returns a string representation of this camera to be used in a properties
	 * file.
	 * 
	 * @return
	 */
	@Override
	public String toString() {

		return cameraURLString + "," + username + "," + password;

	}
	
//	private void log(String msg) {
//		String result = "AxisCameraInterface: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
	
//	public static void main(String[] args) {
//		System.out.println("Creating camera...");
//		AxisCameraInterface camera = new AxisCameraInterface("192.168.0.20", "root", "longhorn", null);
//		Image i = camera.getSnapshot();
//		
//	}
}
