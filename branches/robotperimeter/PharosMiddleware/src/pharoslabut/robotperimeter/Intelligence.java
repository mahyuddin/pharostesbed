package pharoslabut.robotperimeter;
import java.util.List;
import java.util.Map;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import edu.utexas.ece.mpc.context.ContextHandler;
import edu.utexas.ece.mpc.context.summary.ContextSummary;
import edu.utexas.ece.mpc.context.summary.GroupContextSummary;
import edu.utexas.ece.mpc.context.summary.HashMapContextSummary;

public class Intelligence {

	public static HashMapContextSummary mySummary; // the host's version of its
	// context summary. this
	// object is the latest
	// version, and the context
	// handler must be updated
	// occasionally (by giving
	// it this object)

	public static int myId;
	public static final int collisionDistanceThreshold = 200; // max distance
	// that we
	// include in
	// collision
	// vector
	// calculations
	public static final double NeighScale = 1. / 40; // amount to linearly scale
	// the distance between
	// neighbors by before
	// taking a power in
	// collision
	// calculations
	// when distance reaches denominator value, neighbor vector starts
	// increasing exponentially
	public static final int GoalScale = 20; // amount to linearly scale distance
	// to goal in collision/goal
	// calculations; bigger = more
	// important
	public static final int goalThreshold = 100; // above this threshold, robot
	// just targets nearest
	// point on the circle,
	// rather than center of
	// Veronoi partitions.
	public static final double maximumDistance = 5000.0; // above this
	// threshold,
	// distanceBetween
	// just returns
	// threshold. (to
	// prevent NAN from
	// returning)

	ContextHandler handler;
	int radius; // radius of circle round target.

	int minRadius; // minimum radius around target
	int maxRadius; // max radius around target

	double followingThreshold; // time in s. if a robot has received an update
	// from the target (directly or indirectly) within this time, it is
	// considered to be following the target
	double maxChangeAngleBy;
	double maxSpeed;
	// current state
	double currentSpeed; // /< speed of the host
	double currentAngle; // /< angle of linear motion
	double newAngle; // angle after setdirection is called and before next

	Location current; // current robot locations=
	Location center; // center of target (location of target)
	Location goal; // point on circle to which the robot should be moving.

	TargetGroupContextSummary myTargetGroup;

	public Intelligence() {
		super();
	}

	public Intelligence(int myId, int defaultRadius, int minRadius,
			int maxRadius, double followingThreshold, double changeAngleBy,
			double speed, double angle) {
		handler = ContextHandler.getInstance();
		center = new Location(0, 0);
		this.maxSpeed = speed;
		this.currentSpeed = speed;
		this.currentAngle = angle;
		// this.parent = parent;
		this.radius = defaultRadius;
		this.minRadius = minRadius;
		this.maxRadius = maxRadius;
		this.followingThreshold = followingThreshold;
		this.maxChangeAngleBy = changeAngleBy;
		mySummary = new HashMapContextSummary();
		HashMapContextSummaryInterface.insertLocationStamp(mySummary, "ownLocation", new LocationStamp(new Location(0,0),-1));
		Intelligence.myId = mySummary.getId();

		Logger.log(String.format("Intelligence Created; id: %d, current speed: %f, current angle: %f", myId, speed, angle));
	}

	Location determineNearestPointOnCircle() {

		// radius, xCenter,yCenter, xPosition, yPosition
		/*
		 * 1) find angle from center to current position 2) go out radius
		 * distance at that position 3) that point is the current goal for the
		 * robot
		 */
		double xCenter = center.latitude(), yCenter = center.longitude();
		double xPosition = current.latitude(), yPosition = current.longitude();
		double angle = findAngle(xCenter, yCenter, xPosition, yPosition);
//		Logger.log("angle: " + angle);
		Location nearest = getDestinationLocationFromDistanceAndBearing(center,
				radius, angle);

		Logger.log(String
				.format("center of circle: %f %f, current position: %f %f, nearest: %f %f", 
						center.latitude(), center.longitude(),
						current.latitude(),
						current.longitude(), nearest.latitude(), nearest.longitude()));

		return nearest;
	}

	static double findAngle(double x1, double y1, double x2, double y2) {
		// angle from 1st to 2nd (reference in cardinal north)

		double xDif = x2 - x1;
		double yDif = y2 - y1;
		double destAngle = findAngleFromVector(xDif, yDif);
		return destAngle;
	}

	static double findAngleFromVector(double xDif, double yDif) {
		double destAngle = Math.atan(((double) yDif) / xDif) * 180 / Math.PI;

		if (xDif <= 0 && yDif <= 0)
			destAngle += 180; // due to limit of domain of atan function
		else if (xDif <= 0 && yDif >= 0)
			destAngle += 180;

		if (destAngle == Double.NaN || destAngle == Double.NEGATIVE_INFINITY
				|| destAngle == Double.POSITIVE_INFINITY)
			return 0;

		while (destAngle > 360)
			destAngle -= 360;
		while (destAngle < 0)
			destAngle += 360;

		destAngle = Math.max(0.0, destAngle); // to prevent -NAN and NAN
		destAngle = Math.min(destAngle, 360.);
		return destAngle;

	}

	double distanceBetween(double x1, double y1, double x2, double y2) {
		double dis = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		if (dis == Double.NaN)
			dis = maximumDistance;
		return dis;
	}

	//the new position to go to
	//takes into account goal and collision control
	Location determineNewGoPosition()
	{
		Location goal = determineGoal();
		double angle = findDirection();
		double distance = current.distanceTo(goal);
		Location newGoPosition = getDestinationLocationFromDistanceAndBearing(current, distance, angle);
		return newGoPosition;
		
	}
	
	// //finds the direction (in degrees) for the robot to go next timestep.
	// This
	// includes goal and collision components
	double findDirection() {
		
		double angle = 0;

		// 1)find angle to goal, splitting into unit x and y components

//		double xCenter = center.latitude(), yCenter = center.longitude();
		double xGoal = goal.latitude(), yGoal = goal.longitude();
		double xPosition = current.latitude(), yPosition = current.longitude();

		double distanceToGoal = current.distanceTo(goal);
		double destX = 0;
		double destY = 0;
		if (distanceToGoal > .5) {
			double destAngle = findAngle(xPosition, yPosition, xGoal, yGoal);
			destX = Math.cos(destAngle * Math.PI / 180) * GoalScale;
			destY = Math.sin(destAngle * Math.PI / 180) * GoalScale;
		}

		// 2)find angle to each neighbor, split into x and y, scale to inverse
		// cube of
		// (distance to that neighbor /some constant)

		double neighX = 0; // sum of x components from neighbors
		double neighY = 0; // sum of y components from neighbors

		List<ContextSummary> rSummaries = handler.getReceivedSummaries();

		for (int it = 0; it < rSummaries.size(); it++) {
			// here, we care about all hosts that we have seen for collision
			// control

			ContextSummary summ = rSummaries.get(it);
			if (summ.getId() == myId)
				continue; // not care about own position in collision
			// calculations

			if (!(summ instanceof HashMapContextSummary))
				continue; // ignore group summaries

			HashMapContextSummary locSumm = (HashMapContextSummary) summ;
			LocationStamp loc = HashMapContextSummaryInterface
					.retrieveLocationStamp(locSumm, "ownLocation");

			if (loc.timestamp < .00001)
				continue; // if no location information (timestamp is 0)
			double dist = current.distanceTo(loc.location);
			if (dist > collisionDistanceThreshold)
				continue; // not care above threshold

			// find angle, split to x and y, scale, store the vector somehow
			// flip angle (go away from neighbor)
			double neighAngle = ((int) findAngle(xPosition, yPosition,
					loc.location.latitude(), loc.location.longitude()) + 180) % 360; // angle
																						// away
																						// from
																						// neighbor
			double disScale = Math.pow((dist * NeighScale), 4); // amount to
			// scale
			// distance by
			neighX += Math.cos(neighAngle * Math.PI / 180) / disScale;
			neighY += Math.sin(neighAngle * Math.PI / 180) / disScale;
		}

		// 3) add two vectors obtained above, scale according to speed. change
		// speed
		// depending on distance?
		double resVector_x = destX + neighX;
		double resVector_y = destY + neighY;
		
		angle = findAngleFromVector(resVector_x, resVector_y);
		Logger.log(String.format("destX: %f, destY %f, neighX: %f, neighY: %f, resVector_x: %f, resVector_y: %f, angle: %f", destX, destY, neighX, neighY, resVector_x, resVector_y, angle));

		return angle;

	}

	public Location determineGoal() {
		//TODO changed from myTargetGroup to center; check if valid
		if (center == null) {
			goal = new Location(current.latitude(), current.longitude());

			return goal;
		}
		double xNearest, yNearest; // nearest point on circle to robot
		Location nearestLocOnCircle = determineNearestPointOnCircle();

		// here, threshold should be proportional to radius of circle around
		// target
		// find distance to nearest point on circle

		double distNearest = current.distanceTo(nearestLocOnCircle);
		Logger.log(String.format("nearest: %s distance to Nearest: %f",nearestLocOnCircle.toString(), distNearest));

		// if above threshold, just go to that point (it doesn't make sense to
		// be
		// more
		// accurate at the beginning)

		if (distNearest > goalThreshold) {
			goal = nearestLocOnCircle;
			return goal;
		}
		// find angle from center to robot, keep increasing angle until either
		// not
		// the
		// nearest robot or above threshold, also keep decreasing
		// int robotIndex = parent->getIndex();

		double angleToCenter = findAngle(center.latitude(), center.longitude(),
				current.latitude(), current.longitude());
		double CWBoundary = angleToCenter;
		double CCWBoundary = angleToCenter;

		int CCWcrossed = 0; // if angle crosses 0/360 boundary
		int CWcrossed = 0;

		int angleRange = 0; // range from CWBoundary to CCWBoundary. if range
		// crosses
		// threshold, just go to nearest point and optimize later

		// while boolean statement - finding point on circle that corresponds
		// to
		// CW/CCWBoundary angle from center
		while (nearestRobot(getDestinationLocationFromDistanceAndBearing(
				center, radius, CWBoundary)) == myId) {
			CWBoundary -= 10;
			angleRange += 10;
			if (angleRange > 180) {
				break;
			}
			if (CWBoundary < 0) {
				CWBoundary += 360;
				CWcrossed = 1;
			}
		}

		while (nearestRobot(getDestinationLocationFromDistanceAndBearing(
				center, radius, CCWBoundary)) == myId) {
			CCWBoundary += 10;
			angleRange += 10;
			if (angleRange > 180) {
				break;
			}
			if (CCWBoundary >= 360) {
				CCWBoundary -= 360;
				CCWcrossed = 1;
			}
		}

		// find the average angle between the 2 endpoints

		// check for boundary cross to adjust angle values to find the average
		// in
		// the
		// proper direction

		if (CCWcrossed != 0)
			CCWBoundary += 360; // CCW crossed 0/360 boundary
		else if (CWcrossed != 0)
			CWBoundary -= 360; // CW crossed boundary

		double angle = (CCWBoundary + CWBoundary) / 2;

		// go to the point on the circle corresponding to that angle.
		goal = getDestinationLocationFromDistanceAndBearing(center, radius,
				angle);
		// new Location(center.latitude() + radius
		// * Math.cos(angle * Math.PI / 180), center.longitude() + radius
		// * Math.sin(angle * Math.PI / 180));
		
		Logger.log(String.format("Determined new target goal: %s",goal.toString()));
		
		return goal;
		
		
	}

	public void updateLocationKnowledge(Location pos) {
		Logger.log("updating location knowledge");
		List<GroupContextSummary> groups = handler.getGroupSummaries();
		
		Logger.log(""+groups.size());
		
		for (GroupContextSummary g : groups) {
			Logger.log(g.getClass().getName());
			Logger.log(g.getMemberIds().toString());
			Logger.log("my id:" + myId);
			if (g instanceof TargetGroupContextSummary
					&& g.getMemberIds().contains(myId))
				myTargetGroup = (TargetGroupContextSummary) g;
		}

		current = pos;

		LocationStamp ownPosition = new LocationStamp();
		ownPosition.location = current;
		ownPosition.timestamp = System.currentTimeMillis();

		// TODO double check consistency of keys given
		HashMapContextSummaryInterface.insertLocationStamp(mySummary,
				"ownLocation", ownPosition);
		handler.updateLocalSummary(mySummary);
		
		Logger.log("target group: " + myTargetGroup.toString());

		LocationStamp targetLocation = HashMapContextSummaryInterface
				.retrieveLocationStamp(myTargetGroup, "target");
		
//		Logger.log(myTargetGroup.toString());
		
		if (targetLocation == null)
		{
			center = null;
			radius = 0;
		}
		else 
		{
			center = targetLocation.location;
			radius = myTargetGroup.get("groupRadius");
		}
//		//TODO remove fake
//		fakeTargetLocation(new Location (30.286838, -97.73659));


		Logger.log(String.format("updated data in intelligence; target center: %s, group radius: %d, ownLocation: %s", center==null?null:center.toString(), radius, ownPosition.toString()));
	}
	
	//TODO remove hardcoded locations
//	public void fakeTargetLocation (Location centerfake)
//	{		
//		center = centerfake;
//		radius = 1;
//	}

	// returns id of nearest Robot to given point
	public int nearestRobot(Location locationOnCircle) {
		// in all uses of this function, only care about hosts following the
		// same
		// target as this host
		int id = -1;
		double minDistance = 500;
		// conversion)
		
		if (myTargetGroup==null) return myId;

		Map<Integer, LocationStamp> allGroupLocations = myTargetGroup
				.retrieveAllLocalLocations();
		for (Integer in : allGroupLocations.keySet()) {
			LocationStamp loc = allGroupLocations.get(in);
			double robDistance = loc.location.distanceTo(locationOnCircle);
			if (robDistance < minDistance) {
				minDistance = robDistance;
				id = in.intValue();
			}
		}
		return id;
	}

	public double setSpeed() {
		double distance = current.distanceTo(goal);
		if (distance < .5)
			distance = 0;
		currentSpeed = Math.min(distance, maxSpeed);
		return currentSpeed;
	}

	public void moveTo(Location loc) {
		goal = loc;
	}

	private Location getDestinationLocationFromDistanceAndBearing(
			Location start, double distance, double angle) {

		double dist = distance / 1000 / 6378.1;
		double bearing = Math.toRadians(angle);
		double lat1 = Math.toRadians(start.latitude());//convert to radians
		double lon1 = Math.toRadians(start.longitude());

//		Logger.log(String.format("distance: %f, angle: %f, bearing: %f",dist,angle, bearing));
		
		
		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist)
				+ Math.cos(lat1) * Math.sin(dist) * Math.cos(bearing));
		double lon2 = lon1 + Math.atan2(
				Math.sin(bearing) * Math.sin(dist) * Math.cos(lat1),
				Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
		return new Location(Math.toDegrees(lat2), Math.toDegrees(lon2));
				
	}
}
