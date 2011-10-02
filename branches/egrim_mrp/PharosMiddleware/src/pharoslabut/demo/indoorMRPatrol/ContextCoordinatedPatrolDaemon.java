package pharoslabut.demo.indoorMRPatrol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import pharoslabut.RobotIPAssignments;
import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;
import edu.utexas.ece.mpc.context.ContextHandler;
import edu.utexas.ece.mpc.context.summary.ContextSummary;
import edu.utexas.ece.mpc.context.summary.HashMapContextSummary;
import edu.utexas.ece.mpc.context.summary.WireContextSummary;

public class ContextCoordinatedPatrolDaemon extends PatrolDaemon implements Runnable, Observer {

    /**
     * The maximum time in milliseconds that can pass before a teammate is 
     * considered disconnected.
     */
    public static final long DISCONNECTION_THRESHOLD = 10000;

    private static final String NUM_MARKERS_TRAVERSED = "num markers traversed";
    private HashMapContextSummary myContext;
    private ContextHandler handler = ContextHandler.getInstance();

    /**
     * Records the local robot's perspective of the team's state. The key is the robot name, while the value is the
     * robot's state.
     */
    private HashMap<String, RobotState> teamState = new HashMap<String, RobotState>();

    public ContextCoordinatedPatrolDaemon(LoadExpSettingsMsg settings, LineFollower lineFollower,
                                          PathLocalizerOverheadMarkers pathLocalizer,
                                          int numRounds, WiFiBeaconBroadcaster wifiBeaconBroadcaster) {
        super(settings, numRounds, lineFollower, pathLocalizer);
        handler.addPostReceiveSummariesUpdateObserver(this);

        // Construct context summary
        try {
            myContext = new HashMapContextSummary(RobotIPAssignments.getID());
        } catch (PharosException e) {
            e.printStackTrace();
            Logger.logErr("Unable to get robot id.");
            System.exit(1);
        }

        Logger.log("Starting the beaconing.");
        long minPeriod = 1000;
        long maxPeriod = 15000;
        short txPower = (short) 31;
        wifiBeaconBroadcaster.start(minPeriod, maxPeriod, txPower);

        new Thread(this).start();
    }

    /**
     * This is called whenever new summaries are received.
     */
    @Override
    public void update(Observable observable, Object object) {
        @SuppressWarnings("unchecked")
        Collection<WireContextSummary> summaries = (Collection<WireContextSummary>) object;
        for (ContextSummary summary: summaries) {
            try {
                String robotName = RobotIPAssignments.getName(summary.getId());
                Logger.logDbg("Received summary from " + robotName);
                
                RobotState robotState = teamState.get(robotName);
                robotState.setLastHeardTimeStamp();
                robotState.setNumMarkersTraversed(summary.get(NUM_MARKERS_TRAVERSED));

                // Since we've gotten an update, notify all threads waiting on this change
                Logger.logDbg("Notifying all threads waiting on team state changes.");
                synchronized (teamState) {
                    teamState.notifyAll();
                }
            } catch (PharosException e) {
                Logger.logErr("While processing beacon, unable to determine robot's name based on its id (" 
                        + summary.getId() + ")");
                e.printStackTrace();
            }
        }
    }

    /**
     * Determine whether the team is synchronized. The team is synchronized when all of the team mates that are within
     * range have traversed at least the same number of markers as the local node.
     * 
     * @return true if the team is synchronized with the local robot.
     */
    private boolean isTeamSynced() {
        boolean result = true;

        StringBuffer sb = new StringBuffer("Checking whether the team is in sync, numMarkersSeen = "
                                                   + numMarkersSeen + "...");
        Iterator<RobotState> itr = teamState.values().iterator();
        while (itr.hasNext()) {
            RobotState currState = itr.next();
            sb.append("\n\tChecking robot: " + currState + "...");
            
            // Ignore the teammate if he has disconnected wirelessly.
            if (currState.getAge() > DISCONNECTION_THRESHOLD) {
                sb.append("disconnected (ignoring)");
            } else {
                if (currState.getNumMarkersTraversed() < numMarkersSeen) {
                    result = false;
                    sb.append(" not synched!");
                } else {
                    sb.append("synched!");
                }
            }
        }
        
        Logger.logDbg(sb.toString());
        return result;
    }
    
    /**
     * Forces the calling thread to wait until the team is synched.
     */
    private void waitTillLooselySynced() {
        
        // While the team is not in sync, wait for the team to become in sync.
        while (!isTeamSynced()) {
            synchronized(teamState) {
                try {
                    teamState.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Updates the local robot's beacon to contain the most recent number of markers seen.
     */
    private void updateMyContext() {
        Logger.log("Updating local context");
        myContext.put(NUM_MARKERS_TRAVERSED, numMarkersSeen);
        handler.updateLocalSummary(myContext);
        Logger.log("New summary: " + myContext);
    }
    
    @Override
    public void run() {
        Logger.logDbg("Thread starting...");
        
        if (!checkDone()) {
            Logger.logDbg("Starting the line follower.");
            lineFollower.start();
            
            while (!checkDone()) {
                boolean numMarkersSeenUpdated;
                
                synchronized(this) {
                    numMarkersSeenUpdated = this.numMarkersSeenUpdated;
                    this.numMarkersSeenUpdated = false;
                }
                
                if (numMarkersSeenUpdated) {
                    
                    Logger.log("Reached marker " + numMarkersSeen);
                    updateMyContext();
                    
                    Logger.log("Checking if all teammates are loosely synced.");
                    if (!isTeamSynced()) {
                        Logger.log("Team not synced, waiting at this marker until team is synced.");
                        lineFollower.stop();
                        waitTillLooselySynced();
                        lineFollower.start();
                    } else {
                        Logger.log("Team synched, continuing.");
                    }
                }
                
                // Wait for the next overhead marker event.
                synchronized(this) {
                    if (!this.numMarkersSeenUpdated) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            Logger.logErr("Exception while waiting: [" + e.getMessage() + "]");
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            Logger.log("Experiment completed!");  // Shall we synchronize one last time?
            lineFollower.stop();
            System.exit(0);
            
        } else {
            Logger.log("WARNING: The experiment was completed even before it started!");
            System.exit(0);
        }
    }
}
