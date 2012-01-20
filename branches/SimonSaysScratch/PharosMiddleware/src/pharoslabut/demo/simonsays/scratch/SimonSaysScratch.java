/**
 * - SSH to the robot
 * - Kill the player process
 * - Restart player with "player /usr/local/share/player/config/proteus-roomba.cfg"
 * - Test with "playerjoy"
 */

package pharoslabut.demo.simonsays.scratch;

import java.util.LinkedList;
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

    public static final int CREATE_PORT = 6665;
    
    private CreateRobotInterface myRobot;
    private ScratchIO myScratch;
    
    public SimonSaysScratch(String host) {
        System.out.println("Creating robot interface to host: " + host);
        myRobot = new CreateRobotInterface(host, CREATE_PORT, false);
        
        System.out.println("Creating Scratch interface");
        myScratch = new ScratchIO();
    }
    
    public void execute() {
        System.out.println("*** Starting ***");
        
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
     * @param args
     */
    public static void main(String[] args) {
        SimonSaysScratch s;
        
        if (args.length == 0) {
            s = new SimonSaysScratch("localhost");
        }
        else {
            s = new SimonSaysScratch(args[0]);
        }
        
        s.execute();
    }
}
