package robotPerimeter;
import pharoslabut.logger.Logger;
import pharoslabut.sensors.BlobDataConsumer;
import pharoslabut.sensors.BlobDataProvider;
import playerclient3.PtzInterface;
import playerclient3.structures.blobfinder.PlayerBlobfinderBlob;
import playerclient3.structures.blobfinder.PlayerBlobfinderData;
import playerclient3.structures.ptz.PlayerPtzCmd;

/*
 * Class adapted from Linefollower2 in pharoslabut
 */

public class Camera implements BlobDataConsumer {
	/**
	 * Implements the PID controller for keeping the camera facing the line.
	 */
	private CameraPanController cameraPanController;
	/**
	 * This is the maximum area in pixels that a blob can consume before being
	 * considered a valid blob.
	 */
	public static int BLOB_AREA_MAX_THRESHOLD = 200;

	/**
	 * This is the maximum valid age of the blob data. Anything older than that
	 * is discarded.
	 */
	public static final long BLOB_MAX_VALID_AGE = 1500;

	/**
	 * Whether new blob data was received.
	 */
	private boolean newBlobData = false;

	/**
	 * The latest blob data available.
	 */
	private PlayerBlobfinderData blobData = null;
	private Vision vision;

	public Camera(BlobDataProvider provider, PtzInterface ptz) {

		Logger.log("Registering as blob data consumer.");
		provider.addBlobDataConsumer(this);

		this.cameraPanController = new CameraPanController(ptz);

		Logger.log("Ensuring the motors are initially stopped.");

		Logger.log("Resetting camera position.");
		PlayerPtzCmd ptzCmd = new PlayerPtzCmd();
		ptzCmd.setPan(0f);
		ptzCmd.setTilt(0f);
		ptz.setPTZ(ptzCmd);

		vision = new Vision();
	}
	
	public boolean done = false;

	public void CameraControl() {
		long blobDataTimeStamp = 0; // The age of the latest valid timestamp.

		while(!done) {

			// If new blob data is available, get and process it.
			synchronized(this) {
				if (newBlobData) {
					newBlobData = false;
					if (processBlobs(blobData)) {
						blobDataTimeStamp = System.currentTimeMillis(); // only update timestamp if the blob contained line data.
						//Added code to call vision interface (by Nikhil)
						//TODO modify to detect people's shirts, also change targetID (currently hardcoded to 0)
						//to reflect shirt color
						vision.detectedBlob(cameraPanController.getPanAngle(), 0);
					}
				}
			}
	}

	Logger.log("Thread exiting, ensuring robot is stopped...");
	cameraPanController.shutdown();
}

@Override
public void newBlobData(PlayerBlobfinderData blobData) {
	// Logger.log("Received new blob data.");
	synchronized (this) {
		this.blobData = blobData;
		this.newBlobData = true;
		this.notifyAll();
	}
}

/**
 * Processes the blobs being reported by the CMUCam2. It assumes that the
 * first blob represents the line to be followed.
 * 
 * @param data
 *            The blob finder data that contains information about all of
 *            the blobs in the current field of view.
 * @return whether the blob data contained a valid blob representing the
 *         line.
 */
private synchronized boolean processBlobs(PlayerBlobfinderData data) {
	// Check to make sure we have valid data...
	if (data != null) {
		int numBlobs = data.getBlobs_count();
		// Logger.log("There are " + numBlobs + " blobs...");

		if (numBlobs > 0) {
			try {
				PlayerBlobfinderBlob blob = data.getBlobs()[0];
				int midPoint = data.getWidth() / 2; // The midpoint is half
				// of the image width
				// dimension.
				if (blob.getArea() < BLOB_AREA_MAX_THRESHOLD) {

					// Adjust the camera's pan to center it on the line
					cameraPanController.adjustCameraPan(blob, midPoint);

					// Adjust the robot's steering angle and speed based on
					// the camera's pan angle
					return true;
				} else {
					Logger.logDbg("Blob area is " + blob.getArea()
							+ ", max threshold is "
							+ BLOB_AREA_MAX_THRESHOLD
							+ " pixels, ignoring blob.");
				}
			} catch (Exception e) {
				Logger.logErr("Error while fetching primary blob: "
						+ e.toString());
			}

		} else {
			Logger.logErr("No blobs present, stopping robot...");
		}
	} else {
		Logger.logErr("Blob data is null, stopping robot...");
	}
	return false;
}
}