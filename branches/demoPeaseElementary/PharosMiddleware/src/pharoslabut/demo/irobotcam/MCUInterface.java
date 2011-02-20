package pharoslabut.demo.irobotcam;

import gnu.io.*;
import java.io.*;
import java.util.*;
import pharoslabut.logger.*;
/**
 * This communicates with the MCU.
 * 
 * @author Chien-Liang Fok
 *
 */
public class MCUInterface implements MCUConstants {

	private FileLogger flogger = null;
	private OutputStream os = null;
	private InputStream is = null;
	private boolean ackRcvd = false;
	private SerialReader reader = null;
	
	/**
	 * The constructor.
	 * 
	 * @param portName The comm port on which the MCU is attached.
	 */
	public MCUInterface(String portName, FileLogger flogger) {
		this.flogger = flogger;
		
		// listPorts();
		try {
			connect(portName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the camera tilt angle.
	 * 
	 * @param angle The angle in degrees.
	 */
	public void setCameraTilt(double angle) {
		double angleRadians = angle / 180 * Math.PI; // convert angle to radians
		short angleMCU = (short)(angleRadians * 10000); // convert angle to units expected by MCU (0.0001 radians)
		
		//log("Setting camera tilt angle to be " + angle + " degrees (" + angleMCU + " 0.0001 radians)");
		
		byte[] cmd = {
				PROTEUS_BEGIN,
				PROTEUS_OPCODE_CAMERA_TILT,
				(byte)(angleMCU >> 8),
				(byte)(angleMCU & 0xFF),
				PROTEUS_END
		};
		
		//log("Writing the following command: " + byteArrayToString(cmd, cmd.length));
		
		sendToMCU(cmd);

	}
	
	/**
	 * Sets the camera pan angle.
	 * 
	 * @param angle The angle in degrees.
	 */
	public void setCameraPan(double angle) {
		double angleRadians = angle / 180 * Math.PI; // convert angle to radians
		short angleMCU = (short)(angleRadians * 10000); // convert angle to units expected by MCU (0.0001 radians)
		
		//log("Setting camera pan angle to be " + angle + " degrees (" + angleMCU + " 0.0001 radians)");
		
		byte[] cmd = {
				PROTEUS_BEGIN,
				PROTEUS_OPCODE_CAMERA_PAN,
				(byte)(angleMCU >> 8),
				(byte)(angleMCU & 0xFF),
				PROTEUS_END
		};
		
		//log("Writing the following command: " + byteArrayToString(cmd, cmd.length));
		sendToMCU(cmd);
	}
	
	/**
	 * Adds escape character to any special character in the body of the command.
	 * 
	 * @param cmd The command.
	 * @return The command with escape characters in the body.
	 */
	private byte[] addEscapeChar(byte[] cmd) {
		Vector<Byte> tempVec = new Vector<Byte>();
		
		for (int i = 0; i < cmd.length; i++) {
			tempVec.add(cmd[i]);
		}
		
		// Escape any special character in the message payload.
		// The first two bytes denote the beginning of the message and its opcode.
		// The last byte denotes the end of the message.
		for (int i = 2; i < tempVec.size() - 1; i++) {
			if (tempVec.get(i) == PROTEUS_ESCAPE || tempVec.get(i) == PROTEUS_BEGIN
					|| tempVec.get(i) == PROTEUS_END) {
				tempVec.add(i, PROTEUS_ESCAPE); 
			}
		}
		
		byte[] result = new byte[tempVec.size()];
		for (int i = 0; i < tempVec.size(); i++) {
			result[i] = tempVec.get(i);
		}
		return result;
	}
	
	/**
	 * Sends a command to the MCU and waits for an ack to arrive.
	 * 
	 * @param cmd The command to send.
	 * @return whether the send was successful.
	 */
	private boolean sendToMCU(byte[] cmd) {
		
		cmd = addEscapeChar(cmd);
		
		synchronized(this) {
			ackRcvd = false;
			
			try {
				os.write(cmd);
				os.flush();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		//log("Cmd sent, waiting for ack...");
		synchronized(reader) {
			try {
				reader.wait(MCU_TIMEOUT_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (!ackRcvd)
			//log("Ack received...");
		//else
			log("No ack!");
		
		return ackRcvd;
	}
	
	private void log(String msg) {
		String result = "MCUInterface: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
    public class SerialReader implements Runnable {
        InputStream in;
        BufferedInputStream reader;
        Vector<Byte> byteBuffer = new Vector<Byte>();
        
        public SerialReader ( InputStream in ) {
            this.in = in;
            this.reader = new BufferedInputStream(in);
            new Thread(this).start();
        }
        
        private boolean readStatusPacket() {
        	if (byteBuffer.size() >= MCU_STATUS_PACKET_SIZE) {
        		short tiltAngle, panAngle;
        		
        		byteBuffer.remove(0); // remove PROTEUS_BEGIN
        		byteBuffer.remove(0); // remove PROTEUS_STATUS_PACKET
        		
        		tiltAngle = (short)(byteBuffer.remove(0) << 8);
        		tiltAngle += byteBuffer.remove(0);
        		
        		panAngle = (short)(byteBuffer.remove(0) << 8);
        		panAngle += byteBuffer.remove(0);
        		
        		log("MCU Status Msg: tilt angle = " + tiltAngle + ", panAngle = " + panAngle);
        		
        		byteBuffer.remove(0); // remove PROTEUS_END
        		return true;
        	} else
        		return false;
        }
        
        private boolean readTextMessagePacket() {
        	if (byteBuffer.size() < 3)
        		return false;
        	
        	byte length = byteBuffer.get(2); // third position is length of string
        	
        	log("MCU Msg length: " + length);
        	
        	if (byteBuffer.size() < 3 + length) {
        		log("Insufficient bytes in buffer for message");
        		return false; 
        	}
        	
        	byteBuffer.remove(0); // remove PROTEUS_BEGIN
    		byteBuffer.remove(0); // remove PROTEUS_TEXT_MESSAGE_PACKET
    		byteBuffer.remove(0); // remove length of string
    		
    		byte[] msgBytes = new byte[length];
    		
    		for (int i=0; i < msgBytes.length; i++) {
    			msgBytes[i] = byteBuffer.remove(0);
    		}
    		
    		log("MCU Msg: " + new String(msgBytes).toString());
    		return true;
        	
        }
        
        private boolean readAckPacket() {
        	if (byteBuffer.size() >= MCU_ACK_PACKET_SIZE) {
        		byteBuffer.remove(0); // remove PROTEUS_BEGIN
        		byteBuffer.remove(0); // remove PROTEUS_ACK_PACKET
        		byteBuffer.remove(0); // remove PROTEUS_END
        		
        		//log("Ack received, notifying waiting thread...");
        		ackRcvd = true;
        		synchronized(this) {
        			notifyAll();
        		}
        		return true;
        	} else
        		return false;
        }
        
        private void processIncommingMsgs() {
        	
        	//log("processIncommingMsgs called, size of byteBuffer: " + byteBuffer.size() + "...");
        	
        	// Remove extraneous bytes in the front of the buffer
        	while (byteBuffer.size() > 0 && byteBuffer.get(0) != PROTEUS_BEGIN) {
        		byte b = byteBuffer.remove(0);
        		log("Discarding extraneous byte: 0x" + Integer.toHexString(b));
        	}
        	
        	boolean done = false;
        	
        	while (!done && byteBuffer.size() >= 3) {
        		switch(byteBuffer.get(1)) {
        		case PROTEUS_STATUS_PACKET:
        			done = readStatusPacket();
        			break;
        		case PROTEUS_TEXT_MESSAGE_PACKET:
        			done = readTextMessagePacket();
        			break;
        		case PROTEUS_ACK_PACKET:
        			done = readAckPacket();
        			break;
        		default: 
        			log("Unknown message type: " + byteBuffer.get(1));
        			byteBuffer.remove(0);
        		}
        	}
        }
        
        public void run () {
        	int numBytesRead = 0;
            byte[] buff = new byte[50];
            try{
               while ((numBytesRead = reader.read(buff, 0, buff.length)) != -1) {
            	   
            	   for (int i = 0; i < numBytesRead; i++) {
            		   byteBuffer.add(buff[i]);
            	   }
            	   
            	   processIncommingMsgs();
               }
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }         
            log("SerialReader thread exiting...");
        }
    }
    
	private void connect(String portName) throws Exception {
		log("Connecting to: " + portName);
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() ){
        	log("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort ) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.FLOWCONTROL_NONE);
                
                is = serialPort.getInputStream();
                os = serialPort.getOutputStream();
                
                log("Starting serial reader thread...");
                reader = new SerialReader(is);
            } else {
            	log("Error: Only serial ports are handled by this example.");
            }
        }   
	}
	
	private String byteArrayToString(byte[] buff, int length) {
		String result = "";
		for (int i=0; i < length && i < buff.length; i++) {
			String val = Integer.toHexString(buff[i]);
			if (val.length() > 2) {
				val = val.substring(val.length()-2);
			}
			if (val.length() == 1)
				result += "0x0" + val;
			else
				result += "0x" + val;
			if (i < buff.length - 1)
				result += " ";
		}
		return result;
	}
    
    @SuppressWarnings("unchecked")
    static void listPorts()
    {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()) );
        }        
    }
    
    static String getPortTypeName ( int portType )
    {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }
}
