package pharoslabut.demo.simonsays.scratch;

import java.util.LinkedList;
import pharoslabut.demo.simonsays.CreateRobotInterface;

public class SimonSaysScratch {

    public static final int CREATE_PORT = 6665;
    
    private CreateRobotInterface myRobot;
    private ScratchIO myScratch;
    
    public SimonSaysScratch(String host) {
        System.out.println("Creating robot interface");
        myRobot = new CreateRobotInterface(host, CREATE_PORT);
        
        System.out.println("Creating Scratch interface");
        myScratch = new ScratchIO();
    }
    
    public void execute() {
        System.out.println("*** Starting ***");

        while (true) {
            System.out.println("Waiting for message...");
            LinkedList<ScratchMessage> messages = myScratch.readMsg();

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
                    System.out.println("B'CAST: " + msg.getValue());
                }
                else if (msg.getMessageType() == 
                         ScratchMessage.SENSOR_UPDATE_MSG) {
                    if (msg.getName().equals("turn")) {
                        double angle = Double.parseDouble(msg.getValue());
                        angle = Math.toRadians(angle);
                        
                        System.out.println("TURN " + angle + " radians");
                        
                        myRobot.turn(angle);
                    }
                    else if (msg.getName().equals("move")) {
                        double dist = Double.parseDouble(msg.getValue());
                        
                        System.out.println("MOVE " + dist + " meters");
                        
                        myRobot.move(dist);
                    }
                    else {
                        System.out.println("Unknown sensor name");
                    }
                }
            }
        }
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
