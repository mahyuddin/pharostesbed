package pharoslabut.sensors;

import java.util.Iterator;
import java.util.Vector;

import pharoslabut.logger.Logger;
import pharoslabut.util.SimpleTextGUI;
import playerclient3.structures.position2d.PlayerPosition2dData;
import playerclient3.structures.ranger.PlayerRangerData;

/**
 * Uses overhead markers to localize a robot along a path.  The overhead
 * markers are detected using IR sensors.
 * 
 * @author Chien-Liang Fok
 *
 */
public class PathLocalizerOverheadMarkers implements RangerListener, Position2DListener {
	
	/**
	 * Defines the possible states that the client manager can be in.
	 */
	public static enum IRPathLocalizerState {NO_MARKER, MARKER};
	
	/**
	 * The index of the relevant range data within the PlayerRangerData object.
	 */
	public static final int OVERHEAD_RANGER_INDEX = 5;
	
//	/**
//	 * The distance between overhead markers in meters.
//	 */
//	public static final double OVERHEAD_MARKER_SEPARATION_DISTANCE = 1;
	
	/**
	 * The threshold value above which the marker is assumed to not exist.
	 */
	public static final int THRESHOLD_NONEXIST_MARKER = 10000;  // it is usually 65535
	
	/**
	 * The threshold value below which the marker is assumed to exist.
	 */
	public static final int THRESHOLD_EXIST_MARKER = 1000;
	
	/**
	 * The number of consecutive no marker sensor readings before we conclude
	 * that the marker no longer exists.
	 */
	public static final int THRESHOLD_NO_MARKER = 5;
	
	/**
	 * The number of consecutive marker sensor readings before we conclude that the 
	 * marker exists.
	 */
	public static final int THRESHOLD_MARKER = 2;
	
	/**
	 * Whether the robot is currently under a marker.  It is initially assumed to not be under a marker.
	 */
	private IRPathLocalizerState currState = IRPathLocalizerState.NO_MARKER;
	
	/**
	 * Records the number of overhead markers seen by the robot.
	 */
	private volatile int numOverheadMarkers = 0;
	
	/**
	 * The total distance traveled by the robot in meters.
	 */
	//private double totalDistTraveled = 0;
	
	/**
	 * The number of consecutive sensor measurements in which the marker is not detected.
	 */
	private int countNoMarker = 0;
	
	/**
	 * The number of consecutive sensor measurements in which the marker
	 * is detected.
	 */
	private int countMarker = 0;
	
	/**
	 * For debugging purposes.
	 */
	private SimpleTextGUI gui = null;
	
	/**
	 * The distance since the last marker in millimeters
	 */
	private double distSinceMarker = 0;
	
	/**
	 * The time since the last marker was detected.
	 */
	private long lastMarkerTimestamp = System.currentTimeMillis();
	
	/**
	 * The minimum distance between markers.
	 */
	private double minDist = -1;
	
	/**
	 * Whether this component has been started.
	 */
	private boolean started = false;
	
	/** 
	 * Listeners of path localizer events.
	 */
	private Vector<PathLocalizerOverheadMarkersListener> listeners 
		= new Vector<PathLocalizerOverheadMarkersListener>();
	
	/**
	 * The constructor. No GUI displayed.
	 * 
	 * @param rangerBuffer The source of range information.
	 * @param pos2DBuffer The source of Position2D odometry data.
	 */
	public PathLocalizerOverheadMarkers(RangerDataBuffer rangerBuffer, Position2DBuffer pos2DBuffer) {
		this(rangerBuffer, pos2DBuffer, false);
	}
	
	/**
	 * The constructor.
	 * 
	 * @param rangerBuffer The source of range information.
	 * @param pos2DBuffer The source of Position2D odometry data.
	 * @param showGUI whether to show the GUI.
	 */
	public PathLocalizerOverheadMarkers(RangerDataBuffer rangerBuffer, Position2DBuffer pos2DBuffer, 
			boolean showGUI) {
		rangerBuffer.addRangeListener(this);
		pos2DBuffer.addPos2DListener(this);
		if (showGUI)
			gui = new SimpleTextGUI("Overhead Marker Detector.");
	}
	
	/**
	 * Start this running.  Prior to calling this, be sure to initialize
	 * all aspects of this class, like the minimum marker distance.
	 */
	public void start() {
		Logger.log("Resetting the timestamp of the last marker.");
		lastMarkerTimestamp = System.currentTimeMillis();
		this.started = true;
	}

	/**
	 * Sets the minimum marker distance. Anything marker detection event that occurs prior
	 * to the robot traveling this distance will be considered a false detection.
	 * 
	 * @param minDist The minimum distance between markers.
	 */
	public void setMinMarkerDist(double minDist) {
		Logger.log("Minimum marker distance set to be " + minDist + " meters.");
		this.minDist = minDist;
	}
	
	/**
	 * Adds a listener of overhead marker events.
	 * 
	 * @param listener The listener to add.
	 */
	public void addListener(PathLocalizerOverheadMarkersListener listener) {
		listeners.add(listener);
	}

	/**
	 * Notifies the listeners of a new marker appearing.
	 * 
	 * @param distance The distance since the last marker.
	 */
	private void notifyListeners(double distance) {
		Iterator<PathLocalizerOverheadMarkersListener> itr = listeners.iterator();
		while (itr.hasNext()) {
			itr.next().markerEvent(numOverheadMarkers, distance);
		}
	}
	
	/**
	 * This processes the raw range data from the IR sensor used to detect the overhead markers.
	 */
	private synchronized void processRangeData(double range) {
		long currTime = System.currentTimeMillis();
		long deltaTime = currTime - lastMarkerTimestamp;
		
		Logger.log("current range measurement: " + range + " dist = " + distSinceMarker + " delta time = " + deltaTime);
		
		if (range > THRESHOLD_NONEXIST_MARKER) {
			// We may no longer be under the overhead marker.
			
			countMarker = 0; // reset the "marker" counter

			if (currState != IRPathLocalizerState.NO_MARKER) {
				
				// See if we get THRESHOLD_NO_MARKER consecutive 
				// measurements greater than THRESHOLD_NONEXIST_MARKER
				if (++countNoMarker >= THRESHOLD_NO_MARKER) {
					// Conclude that we are no longer under an overhead marker
					currState = IRPathLocalizerState.NO_MARKER;
					Logger.log("STATUS CHANGE: NO_MARKER");
				} else {
					// We got a distance measurement that might indicate the absence of the 
					// marker but we need to wait till we get THRESOLD_NO_MARKER consecutive readings
					// that are greater than THRESHOLD_NONEXIST_MARKER before concluding for sure.
				}
			} else {
				// This is a duplicate "no marker" signal.  Ignore it.
			}
		} else if (range < THRESHOLD_EXIST_MARKER) {
			// We may be under a marker

			countNoMarker = 0; // reset the "no marker" counter

			if (currState == IRPathLocalizerState.NO_MARKER) {

				// First verify that we've traveled the minimum distance between markers
				if (minDist != -1 && distSinceMarker < minDist) {
					Logger.log("FALSE MARKER DETECTION: minimum distance not reached (" + distSinceMarker + " < " + minDist + ")");
				} else {
					if (++countMarker >= THRESHOLD_MARKER) {
						// Conclude that we are under a marker
						currState = IRPathLocalizerState.MARKER;
						numOverheadMarkers++;

						// Calculate the time since the last marker.
						lastMarkerTimestamp = currTime;

						Logger.log("MARKER DETECTED, Total: " + numOverheadMarkers 
								+ ", dist since last (m): " + distSinceMarker + " (" + (distSinceMarker * 39.3700787) + " in)"
								+ ", time since last (ms): " + deltaTime);
						
						double distance = distSinceMarker;
						distSinceMarker = 0;
						notifyListeners(distance);

						if (gui != null) {
							if (numOverheadMarkers == 1)
								gui.setText(numOverheadMarkers + " Marker");
							else
								gui.setText(numOverheadMarkers + " Markers");
						}
					}
				}
			} else {
				// duplicate marker event.  Ignore it.  Maybe print a debug statement to know we're here
			}
		} else {
			countMarker = 0;
			countNoMarker = 0;
		}
	}
	
	/**
	 * Converts the raw ADC reading into an actual distance
	 * in mm.  Note that this is specific to the short range
	 * IR sensor, and that it is only valid for distances
	 * between 30cm and 85cm.
	 */ 
	double calibrateShortRangeIR(double rawADC) {
	  if (rawADC < 41)
	    return 0xFFFF;
	  else if (rawADC < 117)
	    return 35469 / rawADC;
	  else
	    return 0xFFFF;
	}

	@Override
	public void newRangerData(PlayerRangerData rangeData) {
		if (started) {
			int numSensors = rangeData.getRanges_count();
			if (numSensors >= 6) {
				double[] data = rangeData.getRanges();
				double range = calibrateShortRangeIR(data[5]);

				Logger.log("Raw ADC = " + data[5] + ", Calibrated Dist = " + range + " mm");
				processRangeData(range);

			} else {
				Logger.logErr("Expected at least 6 sensors, instead got " + numSensors);
			}
		} else {
			Logger.logDbg("Received ranger data but component not started.");
		}
	}
	
	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		Logger.log("New Odometry data: " + data.getPos().getPx());
		distSinceMarker += data.getPos().getPx() / 1000.0;  // Store distance in meters. 
	}
}
