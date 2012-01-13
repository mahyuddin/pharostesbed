// ScratchIO.java
// Andrew Davison, March 2009, ad@fivedots.coe.psu.ac.th

/* Uses remote sensor connections in Scratch v.1.3. They
   allow Java to connect to it via a TCP socket at port 42001.

   Details at:
     http://scratch.mit.edu/forums/viewtopic.php?id=9458

   The two types of messages:
      broadcast "<name-string>"
      sensor-update <name-string> <value-string>
 */
package pharoslabut.demo.simonsays.scratch;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class ScratchIO
{
    private static final int SCRATCH_PORT = 42001;
    private static final int NUM_BYTES_SIZE = 4;

    private Socket scratchSocket;
    private InputStream in = null;
    private OutputStream out = null;

    public ScratchIO()
    {
        try {
            scratchSocket = new Socket("localhost", SCRATCH_PORT);
            in = scratchSocket.getInputStream();
            out = scratchSocket.getOutputStream();
        }
        catch (UnknownHostException e) {
            System.err.println("Scratch port (" + SCRATCH_PORT + ") not found");
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("Scratch IO link could not be created");
            System.exit(1);
        }
    }  // end of ScratchIO()


    public void closeDown()
    {
        //InputStream in = null;
        //OutputStream out = null;
        try {
            scratchSocket.close();
        }
        catch (IOException e) {}
    }  // end of closeDown()



    // ------------- message sending methods --------------------


    public boolean broadcastMsg(String msg)
    // send a broadcast message
    {
        String scratchMsg = "broadcast \"" + msg + "\"";
        return sendMsg(scratchMsg);
    }  // end of broadcastMsg()


    public boolean updateMsg(String name, String value)
    // send a sensor-update message
    {
        String scratchMsg = "sensor-update " + name + " " + value;
        return sendMsg(scratchMsg);
    }  // end of updateMsg()



    private boolean sendMsg(String msg)
    /* Sends msg to Scratch using the format described in
        http://scratch.mit.edu/forums/viewtopic.php?id=9458  
     */
    {
        if (out == null) {
            System.err.println("Output stream error");
            return false;
        }

        // System.out.println("Sending: " + msg);
        try {
            byte[] sizeBytes = intToByteArray( msg.length() );
            for (int i = 0; i < NUM_BYTES_SIZE; i++)                 
                out.write(sizeBytes[i]);
            out.write(msg.getBytes());
        }
        catch (IOException e) {
            System.err.println("Couldn't send: " + msg);
            return false;
        }
        return true;
    }  // end of sendMsg()



    private byte[] intToByteArray(int value)
    // convert an integer into a 4-element byte array
    { return new byte[] { 
            (byte)(value >>> 24), (byte)(value >> 16 & 0xff), 
            (byte)(value >> 8 & 0xff), (byte)(value & 0xff) };
    }


    // ------------- message reading methods --------------------

    public LinkedList<ScratchMessage> readMsg()
    /* Receive a message from Scratch and return a LinkedList of
     * ScratchMessages:
     *   - If it is a broadcast, the linked list contains the broadcast.
     *   - If it is a sensor update, the linked list contains a sensor
     *     update ScratchMessage for each variable in the message.
     *   - If the message is empty or malformed, null is returned.
     */
    {
        if (in == null) {
            System.err.println("Input stream error");
            return null;
        }

        int msgSize = readMsgSize();
        if (msgSize > 0) {
            try {
                byte[] buf = new byte[msgSize];
                in.read(buf, 0, msgSize);
                String msg = new String(buf);

                return processMsg(msg);
            } 
            catch (IOException e) {
                System.err.println("Message read error: " + e);
                
                return null;
            }
        }
        else {
            return null;
        }
    }  // end of readMsg()

    private LinkedList<ScratchMessage> processMsg(String scratchMsg) {
        System.out.println("string: <" + scratchMsg + ">");

        LinkedList<ScratchMessage> messages = new LinkedList<ScratchMessage>();

        String[] args = StringToks.parseTokens(scratchMsg);
        int msgType;
        String msgTypeStr;
        String name;
        String val;

        for (String t: args)
            System.out.println("parse: " + t);

                if (args.length < 2) {    // 0 or 1 argument
                    System.err.println("Incorrectly formatted message");
                }
                else {
                    // assign message type
                    msgTypeStr = args[0].toLowerCase();

                    if (msgTypeStr.equals("broadcast")) {
                        msgType = ScratchMessage.BROADCAST_MSG;

                        for (int i = 1; i < args.length; ++i) {
                            if (!args[i].isEmpty()) {
                                val = extractName(args[i]);
                                messages.push(new ScratchMessage(msgType, msgTypeStr, null, val));

                                break;
                            }
                        }
                    }
                    else if (msgTypeStr.equals("sensor-update")) {
                        msgType = ScratchMessage.SENSOR_UPDATE_MSG;

                        if (args.length > 2) {
                            for (int i = 1; i < args.length - 1; i += 2) {
                                name = extractName(args[i]);
                                val = args[i + 1];

                                messages.push(new ScratchMessage(msgType, msgTypeStr, name, val));
                            }
                        }
                    }
                    else {
                        System.err.println("Unknown message type");
                        msgTypeStr = "unknown";
                        msgType = ScratchMessage.UNKNOWN_MSG;
                    }

                }

                return messages;
    }  // end of processMsg()

    private String extractName(String nm)
    // extract the name which may be in quotes, and start with "Scratch-"
    {
        if (nm.charAt(0) == '\"')
            nm = nm.substring(1, nm.length()-1);   // remove quotes

        if (nm.startsWith("Scratch-"))
            nm = nm.substring(8);    // remove "Scratch-"

        // System.out.println("Extracted name: " + nm);
        return nm;
    }  // end of extractName()

    private int readMsgSize()
    // message size is encoded as NUM_BYTES_SIZE bytes at the start of the message
    {
        int msgSize = -1;
        try {
            byte[] buf = new byte[NUM_BYTES_SIZE];
            in.read(buf, 0, NUM_BYTES_SIZE);
            msgSize = byteArrayToInt(buf);
            // System.out.println("message size: " + msgSize);
        } 
        catch (IOException e) {
            System.err.println("Header read error: " + e);
            System.exit(0);
        }
        return msgSize;
    }  // end of readMsgSize()



    private static int byteArrayToInt(byte [] b) 
    // convert a byte array into an integer
    { 
        return (b[0] << 24) + ((b[1] & 0xFF) << 16) +
                ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
    }

}  // end of ScratchIO class

