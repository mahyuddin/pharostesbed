package pharoslabut.demo.simonsays.scratch;

import java.util.LinkedList;
import pharoslabut.demo.simonsays.CreateRobotInterface;

public class ScratchTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("*** Starting ***");

        // When interfacing with CreateRobotInterface, use "localhost" on port 6665.
        //System.out.println("Creating robot interface");
        //CreateRobotInterface robot = new CreateRobotInterface("localhost", 6665);

        System.out.println("Creating Scratch interface");
        ScratchIO sio = new ScratchIO();

        while (true) {
            System.out.println("Waiting for message...");
            LinkedList<ScratchMessage> messages = sio.readMsg();

            if (messages == null) {
                System.out.println("Unable to parse message.");
                continue;
            }

            for (ScratchMessage msg: messages) {
                System.out.println("Received msg: " + msg);

                if (msg != null) {
                    if (msg.getMessageType() == ScratchMessage.BROADCAST_MSG) {
                        System.out.println("B'CAST: " + msg.getValue());
                    }
                    else if (msg.getMessageType() == 
                             ScratchMessage.SENSOR_UPDATE_MSG) {
                        if (msg.getName().equals("turn")) {
                            System.out.println("TURN " + msg.getValue());
                        }
                        else if (msg.getName().equals("move")) {
                            System.out.println("MOVE " + msg.getValue());
                        }
                        else {
                            System.out.println("Unknown sensor name");
                        }
                    }
                }
            }
        }
        
        //System.out.println("*** Done ***");
    }
}
