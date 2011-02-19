package pharoslabut.demo.irobotcam;

import gnu.io.*;
import java.io.*;

/**
 * This provides a basic test of the servos for panning and tilting the camera.
 * 
 * @author Chien-Liang Fok
 */
public class TestServos implements MCUConstants {

	OutputStream os = null;
	InputStream is = null;
	
	public TestServos(String portName) {
		// listPorts();
		try {
			connect(portName);
			(new Thread(new SerialReader(is))).start();
			doTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println("Done performing test...");
		//System.exit(0);
	}
	
	private void doTest() {
		double MIN_PAN_ANGLE = -70;
		double MAX_PAN_ANGLE = 70;
		//double STEP_SIZE = 0.5;
		
		while (true) {
			// pan to the right...
			System.out.println("Panning to the right...");
			setCameraPan(MIN_PAN_ANGLE);
			
			
			synchronized(this) {
				try {
					this.wait(1000 * 10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			
			System.out.println("Panning to the left...");
			setCameraPan(MAX_PAN_ANGLE);
			
			synchronized(this) {
				try {
					this.wait(1000 * 10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
//		while (true) {
//			// Pan left to right...
//			for (double i = MIN_PAN_ANGLE; i < MAX_PAN_ANGLE; i+= STEP_SIZE) {
//				double currAngle = i / 180 * Math.PI; // convert angle to radians
//				short val = (short)(currAngle * 10000); // convert angle to units expected by MCU (0.0001 radians)
//				setCameraPan(val);
//
//				synchronized(this) {
//					try {
//						this.wait(100);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			
//			synchronized(this) {
//				try {
//					this.wait(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			// Pan right to left...
//			for (double i = MAX_PAN_ANGLE; i > MIN_PAN_ANGLE; i-= STEP_SIZE) {
//				double currAngle = i / 180 * Math.PI; // convert angle to radians
//				short val = (short)(currAngle * 10000); // convert angle to units expected by MCU (0.0001 radians)
//				setCameraPan(val);
//
//				synchronized(this) {
//					try {
//						this.wait(100);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			
//			synchronized(this) {
//				try {
//					this.wait(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
//		byte[] cmd = {
//				PROTEUS_BEGIN,
//				PROTEUS_OPCODE_CAMERA_PAN,
//				0x00,
//				0x0F,
//				PROTEUS_END
//		};
//		
//		System.out.println("Writing the following command: " + byteArrayToString(cmd, cmd.length));
//		
//		
//		try {
//			os.write(cmd);
//			os.flush();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * Sets the camera pan angle.
	 * 
	 * @param angle The angle in degrees.
	 */
	private void setCameraPan(double angle) {
		double angleRadians = angle / 180 * Math.PI; // convert angle to radians
		short angleMCU = (short)(angleRadians * 10000); // convert angle to units expected by MCU (0.0001 radians)
		
		System.out.println("Setting camera pan angle to be " + angle + " degrees (" + angleMCU + " 0.0001 radians)");
		
		byte[] cmd = {
				PROTEUS_BEGIN,
				PROTEUS_OPCODE_CAMERA_PAN,
				(byte)(angleMCU >> 8),
				(byte)(angleMCU & 0xFF),
				PROTEUS_END
		};
		
		System.out.println("Writing the following command: " + byteArrayToString(cmd, cmd.length));
		
		try {
			os.write(cmd);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
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

	private void connect(String portName) throws Exception {
		System.out.println("Connecting to: " + portName);
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() ){
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort ) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.FLOWCONTROL_NONE);
                
                is = serialPort.getInputStream();
                os = serialPort.getOutputStream();
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }   
	}
	
    public class SerialReader implements Runnable {
        InputStream in;
        BufferedInputStream reader;
        
        public SerialReader ( InputStream in ) {
            this.in = in;
            this.reader = new BufferedInputStream(in);
        }
        
        public void run () {
        	int numBytesRead = 0;
            byte[] buff = new byte[50];
            try{
               while ((numBytesRead = reader.read(buff, 0, buff.length)) != -1) {
            	   System.out.println("Read " + numBytesRead + " bytes: " + byteArrayToString(buff, numBytesRead));
               }
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }            
        }
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
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("PharosClient: " + msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.demo.irobotcam.TestServos <options>");
		print("Where <options> include:");
		print("\t-comm <comm port>: To comm port on which the MCU is attached (default: /dev/ttyUSB0)");
		print("\t-debug: enable debug mode");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String portName = "/dev/ttyUSB0";
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-comm")) {
					portName = args[++i];
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else {
					System.setProperty ("PharosMiddleware.debug", "true");
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		print("Port: " + portName);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new TestServos(portName);
	}

}
