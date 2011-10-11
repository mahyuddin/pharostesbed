package pharoslabut.demo.autoIntersection.intersectionDetector;

import pharoslabut.logger.Logger;
import pharoslabut.sensors.BlobDataConsumer;
import playerclient3.structures.blobfinder.PlayerBlobfinderBlob;
import playerclient3.structures.blobfinder.PlayerBlobfinderData;

/**
 * Analyzes blob data from the CMUCam2 to detect when the robot is approaching,
 * entering, and leaving the intersection.
 * 
 * NOTE: This was determined not to work because it induced excessive latency on the CMUCam2
 * resulting in the robot loosing the line.
 * 
 * @author Chien-Liang Fok
 */
@Deprecated public class IntersectionDetectorBlobFinder extends IntersectionDetector implements BlobDataConsumer {

	/**
	 * The secondary blob is used for indicating three things: 
	 * 
	 * APPROACHING_INTERSECTION: secondary blob right of line
	 * ENTERING_INTERSECTION: secondary blob left of line
	 * EXITING_INTERSECTION: secondary blob crossing line
	 * 
	 * @param blob
	 */
	private void handleSecondaryBlob(PlayerBlobfinderBlob primary, PlayerBlobfinderBlob secondary) {
		// TODO: Add logic that determines the type of event that was detected.
		// One the type of event is determined, broadcast it to all registered listeners
		// Here's an example of how to broadcast an APPROACHING event.
		Logger.log("area=" + secondary.getArea() + ", primary x=" + primary.getX() + ", secondary x=" + secondary.getX() 
				+ ", blob width=" + (Math.abs(secondary.getLeft()-secondary.getRight())) + ", previousEventType= " + previousEventType);
		
		if ((primary.getX() < secondary.getX()) && (Math.abs(secondary.getLeft() - secondary.getRight()) < 80)) {
			if (previousEventType == null || previousEventType  != IntersectionEventType.APPROACHING) {
				genApproachingEvent();
			} else
				Logger.log("Supressing duplicate APPROACHING event");
		}
		else if ((primary.getX() > secondary.getX()) && (Math.abs(secondary.getLeft() - secondary.getRight()) < 80)) { 
			if (previousEventType == null || previousEventType  != IntersectionEventType.ENTERING) {
				genEnteringEvent();
			} else
				Logger.log("Supressing duplicate ENTERING Intersection event");
		}
		else if ((Math.abs(secondary.getLeft() - secondary.getRight()) > 100)) {
			if (previousEventType == null || previousEventType  != IntersectionEventType.EXITING) {
				genExitingEvent();
			} else
				Logger.log("Supressing duplicate EXITING Intersection event");
		}
	}

	@Override
	public void newBlobData(PlayerBlobfinderData data) {
		// Check to make sure we have valid data...
		if (data != null) {
			int numBlobs = data.getBlobs_count();
			Logger.log("There are " + numBlobs + " blobs...");

			PlayerBlobfinderBlob[] blobList = data.getBlobs();
				
			// Right now only designed for detection of blue secondary blob
			try {
				if(blobList != null && numBlobs > 1 && blobList[1] != null) {
					handleSecondaryBlob(blobList[0], blobList[1]);
				} 
				else {
					Logger.log("No secondary blob!");
					previousEventType = null;
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				// TODO Figure out why this sometimes happens.
				Logger.logErr("got an unexpected ArrayIndexOutOfBoundsException: " + e.getMessage());
				previousEventType = null;
			}
			
		} else {
			Logger.logErr("Blob data is null, not performing intersection detection...");
		}		
	}
}
