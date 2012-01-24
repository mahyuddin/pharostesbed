/**
 * - SSH to the robot
 * - Kill the player process
 * - Restart player with "player /usr/local/share/player/config/proteus-roomba.cfg"
 * - Test with "playerjoy"
 */

package pharoslabut.demo.simonsays.scratch;

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import pharoslabut.demo.simonsays.CreateRobotInterface;

public class SimonSaysScratch {
    
    public enum ScratchCmd {
        MOVE("move"),
        TURN("turn"),
        PAN("pan"),
        TILT("tilt"),
        SCREENSHOT("screenshot");
        
        private String text;
        
        ScratchCmd(String t) {
            text = t;
        }
        
        public String getText() {
            return text;
        }
        
        public static ScratchCmd fromString(String t) {
            if (t != null) {
                for (ScratchCmd s : ScratchCmd.values()) {
                    if (t.equals(s.getText())) {
                        return s;
                    }
                }
            }
            
            return null;
        }
    }
    
    public enum ScratchVirtVar {
        VAR1("var1"),
        VAR2("var2");
        
        private String text;
        
        ScratchVirtVar(String t) {
            text = t;
        }
        
        public String getText() {
            return text;
        }
    }

    public static final int CREATE_PORT = 6665;
    
    private CreateRobotInterface myRobot;
    private ScratchIO myScratch;
    private Map<ScratchVirtVar, Double> myVirtualVars;
    
    /**
     * Default constructor that connects to a robot running on the localhost.
     */
    public SimonSaysScratch() {
        this("localhost");
    }
    
    /**
     * Constructor that connects to a robot running at 'host'.
     * @param host Hostname of the machine running the robot.
     */
    public SimonSaysScratch(String host) {
        System.out.println("Creating robot interface to host: " + host);
        myRobot = new CreateRobotInterface(host, CREATE_PORT, false);
        
        System.out.println("Creating Scratch interface");
        myScratch = new ScratchIO();
        
        myVirtualVars = new HashMap<ScratchVirtVar, Double>();
        myVirtualVars.put(ScratchVirtVar.VAR1, 0.0);
        myVirtualVars.put(ScratchVirtVar.VAR2, 0.0);
    }
    
    /**
     * Notifies Scratch of any virtual variables and then processes messages
     * from Scratch as they arrive. The messages are then passed to the robot.
     */
    public void execute() {
        System.out.println("*** Starting ***");

        createVirtVars();
        
        // This call to readMsg consumes the initial messages from Scratch.
        // These messages are then dropped.
        LinkedList<ScratchMessage> messages = myScratch.readMsg();

        while (true) {
            System.out.println("Waiting for message...");
            messages = myScratch.readMsg();

            if (messages == null) {
                System.out.println("Unable to parse message.");
                continue;
            }
            
            processMsgs(messages);
        }
    }
    
    /**
     * Notifies Scratch of all virtual variables and initializes them to
     * default values.
     */
    private void createVirtVars() {
        System.out.println("Creating virtual variables");
        
        for (Map.Entry<ScratchVirtVar, Double> entry : myVirtualVars.entrySet()) {
            myScratch.updateMsg((entry.getKey()).getText(),
                                Double.toString(entry.getValue()));
        }
        
        //myScratch.updateMsg(ScratchVirtVar.VAR1.getText(), "2.0");
    }
    
    /**
     * Processes the messages from Scratch by translating them into messages
     * for the robot
     * @param msgs A LinkedList<ScratchMessage> that contains the messages
     * from Scratch.
     */
    private void processMsgs(LinkedList<ScratchMessage> msgs) {
        for (ScratchMessage msg: msgs) {
            System.out.println("Received msg: " + msg);

            if (msg != null) {
                if (msg.getMessageType() == ScratchMessage.BROADCAST_MSG) {
                    if (msg.getValue().equals(ScratchCmd.SCREENSHOT.getText())) {
                        // TODO
                    }
                    else {
                        System.out.println("ERROR: Unknown command");
                    }
                }
                else if (msg.getMessageType() == 
                         ScratchMessage.SENSOR_UPDATE_MSG) {
                    if (msg.getName().equals(ScratchCmd.TURN.getText())) {
                        double angle = Double.parseDouble(msg.getValue());
                        angle = Math.toRadians(angle);
                        
                        System.out.println("TURN " + angle + " radians");
                        
                        if (angle != 0) {
                            myRobot.turn(angle);
                        }
                    }
                    else if (msg.getName().equals(ScratchCmd.MOVE.getText())) {
                        double dist = Double.parseDouble(msg.getValue());
                        
                        System.out.println("MOVE " + dist + " meters");
                        
                        if (dist != 0) {
                            myRobot.move(dist);
                        }
                    }
                    else if (msg.getName().equals(ScratchCmd.PAN.getText())) {
                        // TODO
                    }
                    else if (msg.getName().equals(ScratchCmd.TILT.getText())) {
                        // TODO
                    }
                    else {
                        System.out.println("ERROR: Unknown command");
                    }
                }
            }
        }
        
        myScratch.broadcastMsg("refresh");
    }

    /**
     * Main method.
     * @param args Command line arguments. If there are no arguments, the robot
     * is assumed to be connected to localhost. Otherwise, the first argument is
     * the hostname of the machine running the robot.
     */
    public static void main(String[] args) {
        SimonSaysScratch s;
        
        if (args.length == 0) {
            s = new SimonSaysScratch();
        }
        else {
            s = new SimonSaysScratch(args[0]);
        }
        
        s.execute();
    }
}
