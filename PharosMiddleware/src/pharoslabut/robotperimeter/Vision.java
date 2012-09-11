package pharoslabut.robotperimeter;
import java.util.List;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.GPSDataBuffer;
import edu.utexas.ece.mpc.context.ContextHandler;
import edu.utexas.ece.mpc.context.group.GroupDefinition;
import edu.utexas.ece.mpc.context.group.LabeledGroupDefinition;
import edu.utexas.ece.mpc.context.summary.GroupContextSummary;
import edu.utexas.ece.mpc.context.summary.HashMapContextSummary;

public class Vision {

	private static ContextHandler handler;
	double fakeLatitude;
	double fakeLongitude;
	public Vision(double fLat, double fLong) {
		Logger.log("created vision");
		handler = ContextHandler.getInstance();
		fakeLatitude= fLat;
		fakeLongitude = fLong;
	}
	
	public void VisionControl()
	{
		Logger.log("Started vision control");
		//for now, faking target messages (from other robots)
		//later, will receive messages from target with gps location

		//TODO remove fake
	
		receivedTargetSignal (new Location (fakeLatitude, fakeLongitude),0);
	}

//	public void detectedBlob(double cameraAngle, int targetId) {
//		Location currLoc = new Location(Mobility.nav.getLocation());
//		double latitude = currLoc.latitude();
//		double longitude = currLoc.longitude();
//		double steeringHeading = Mobility.nav.getCompassHeading();
//		findTarget(steeringHeading, cameraAngle, latitude, longitude, targetId);
//	}
	
	public void receivedTargetSignal (Location targetLocation, int targetId)
	{
		//fakes camera sighting, sends data to findTarget
//		Location currLoc = Mobility.current;
//	
//		double angle = Intelligence.findAngle(currLoc.latitude(), currLoc.longitude(), targetLocation.latitude(), targetLocation.longitude());
//		findTarget (0,angle, currLoc.latitude(), currLoc.longitude(), targetId);
//		
		findTarget(targetLocation, targetId);
	}

	// upon seeing a target, converts the given parameters to a line in
	// y-intercept form.
	// angle is desired steering angle, as calculated in blob detection, that
	// should be the overall angle to the
	// target
	public void findTarget(Location location, int targetId)
	{
//		// heading - 0 degrees is north, increases clockwise
//
//		// double slope = Math.tan(ownHeading + cameraOffset);
//		double angle = ownHeading + cameraAngle;
//		double slope = Math.tan(angle * Math.PI / 180);
//		double yIntercept = ownYPosition - slope * ownXPosition;

		HashMapContextSummary mySummary = Intelligence.mySummary;
		TargetSighting newSighting = new TargetSighting();
		newSighting.targetId = targetId;
		newSighting.hostId = Intelligence.myId;
//		newSighting.slope = slope;
//		newSighting.yIntercept = yIntercept;
		newSighting.timestamp = System.currentTimeMillis();
		newSighting.targetLocation = new LocationStamp(location, newSighting.timestamp);

		HashMapContextSummaryInterface.insertTargetSighting(mySummary,
				newSighting);
		
		handler.updateLocalSummary(mySummary);

		// create target group (or add local summary if already exists)

		List<GroupContextSummary> allGroups = handler.getGroupSummaries();
		boolean myTargetGroupFound = false;
		TargetGroupContextSummary myTargetGroup = null;
		for (GroupContextSummary summ : allGroups) {
			// is target group and contains local host
			if (summ instanceof TargetGroupContextSummary
					&& ((TargetGroupContextSummary) summ).getMemberIds()
							.contains(mySummary.getId())) {
				myTargetGroupFound = true;
				myTargetGroup = (TargetGroupContextSummary) summ;
			}
		}

		if (!myTargetGroupFound) {

			GroupDefinition groupDef = new LabeledGroupDefinition<TargetGroupContextSummary>(
					TargetGroupContextSummary.class, targetId);
			handler.addGroupDefinition(groupDef);
			myTargetGroup = (TargetGroupContextSummary) handler.get(targetId);
			myTargetGroup.addLocalSummary(mySummary);
		} else
			myTargetGroup.addLocalSummary(mySummary);
	}
	
	public void findTarget(double ownHeading, double cameraAngle,
			double ownXPosition, double ownYPosition, int targetId) {
		// heading - 0 degrees is north, increases clockwise

		// double slope = Math.tan(ownHeading + cameraOffset);
		double angle = ownHeading + cameraAngle;
		double slope = Math.tan(angle * Math.PI / 180);
		double yIntercept = ownYPosition - slope * ownXPosition;

		HashMapContextSummary mySummary = Intelligence.mySummary;
		TargetSighting newSighting = new TargetSighting();
		newSighting.targetId = targetId;
		newSighting.hostId = Intelligence.myId;
		newSighting.slope = slope;
		newSighting.yIntercept = yIntercept;
		newSighting.timestamp = System.currentTimeMillis();

		HashMapContextSummaryInterface.insertTargetSighting(mySummary,
				newSighting);
		
		Logger.log(String.format("Class: %s, Method: %s\ntarget id: %d\ncurrent position: %f %f\nslope: %f\nyIntercept: %f",
				"Vision", "findTarget", targetId, ownXPosition, ownYPosition,slope,yIntercept));

		handler.updateLocalSummary(mySummary);

		// create target group (or add local summary if already exists)

		List<GroupContextSummary> allGroups = handler.getGroupSummaries();
		boolean myTargetGroupFound = false;
		TargetGroupContextSummary myTargetGroup = null;
		for (GroupContextSummary summ : allGroups) {
			// is target group and contains local host
			if (summ instanceof TargetGroupContextSummary
					&& ((TargetGroupContextSummary) summ).getMemberIds()
							.contains(mySummary.getId())) {
				myTargetGroupFound = true;
				myTargetGroup = (TargetGroupContextSummary) summ;
			}
		}

		if (!myTargetGroupFound) {

			GroupDefinition groupDef = new LabeledGroupDefinition<TargetGroupContextSummary>(
					TargetGroupContextSummary.class, targetId);
			handler.addGroupDefinition(groupDef);
			myTargetGroup = (TargetGroupContextSummary) handler.get(targetId);
			myTargetGroup.addLocalSummary(mySummary);
		} else
			myTargetGroup.addLocalSummary(mySummary);
	}
}
