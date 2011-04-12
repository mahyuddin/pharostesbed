package aim;

import java.io.Serializable;
import java.util.Date;

/**
 * Creates a new Object Robot
 * @author Michael Hanna
 */
public class Robot implements Serializable {
    private int id;
    private String laneSpecs;
    private long ETA;
    private long ETC;
    private float velocity;         // might not be needed
    private boolean enqueued;
    private boolean allowed;
    private long stoppageTime;

    /**
     * Class constructor
     * @param id Robot's ID (IP address)
     * @param laneSpecs Robot's lane specification (direction, path, lane Number)
     * @param ETA Robot's estimated time of arrival (at the intersection)
     * @param ETC Robot's estimated time of clearance (after crossing the intersection)
     * @param velocity Robot's velocity (when sending the request)
     */
    public Robot(int id, String laneSpecs, long ETA, long ETC, float velocity) {
        this.id = id;
        this.laneSpecs = laneSpecs;
        this.ETA = new Date().getTime() - Main.startTime + ETA;
        this.ETC = new Date().getTime() - Main.startTime + ETC;
        this.velocity = velocity;
        enqueued = false;
        allowed = false;
        stoppageTime = 0;
    }


    /**
     *  @return id
     */
    public int getID() {
        return this.id;
    }

    /**
     *  @return laneSpecs
     */
    public String getLaneSpecs() {
        return this.laneSpecs;
    }

    /**
     *  @return ETA
     */
    public long getETA() {
        return this.ETA;
    }

    /**
     *  this method sets a new ETA for the robot
     * @param eta The estimated time of arrival
     */
    public void setETA(long eta) {
        this.ETA = eta + new Date().getTime() - Main.startTime;
    }

    /**
     *  @return ETC
     */
    public long getETC() {
        return this.ETC;
    }

    /**
     *  this method sets a new ETC for the robot
     * @param etc The estimated time of clearance
     */
    public void setETC(long etc) {
        this.ETC = etc + new Date().getTime() - Main.startTime;
    }

    /**
     *  @return velocity
     */
    public float getVelociy() {
        return this.velocity;
    }

    /**
     * This method checks if the robot request is on the queue
     *  @return whether true if the robot request is on the queue, false otherwise
     */
    public boolean isEnqueued() {
        return this.enqueued;
    }

    /**
     *  This method sets the boolean variable enqueued to b
     * @param b
     */
    public void setEnqueued(boolean b) {
        this.enqueued = b;
    }

    /**
     * This method checks if the robot is allowed to pass through the intersection
     * It is being used as a second safety check
     *  @return whether true if the robot request is on the queue, false otherwise
     */
    public boolean isAllowed() {
        return this.allowed;
    }

    /**
     *  This method sets the boolean variable allowed to b
     * @param b
     */
    public void setAllowed(boolean b) {
        this.allowed = b;
    }

    /**
     * This method returns the time that the robot should stop at the intersection, in order to avoid collision
     *  @return stoppageTime
     */
    public long getStoppageTime() {
        return this.stoppageTime;
    }

    /**
     *  This method sets the stoppageTime for the robot
     * @param st
     */
    public void setStoppageTime(long st) {
        this.stoppageTime = st;
    }

    /**
     * This method overrides the toString() method
     * @return String with all the robot attributes
     */
    @Override
    public String toString()
    {
        String output = "id : " + id + "\n";
        output += "laneSpecs : " + laneSpecs + "\n";
        output += "ETA : " + ETA + "\n";
        output += "ETC : " + ETC + "\n";
        output += "velocity : " + velocity + "\n";
        output += "enqueued : " + enqueued + "\n";
        return output;
    }

}
