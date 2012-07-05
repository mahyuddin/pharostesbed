import java.io.IOException;
import java.io.InputStream;
//import java.io.OutputStream;

import javax.swing.JFrame;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;


/**
 * Tests the compass by reading its data directly from the serial port.
 * 
 * Some of the code in this class was taken from the example here:
 * http://rxtx.qbang.org/wiki/index.php/Event_based_two_way_Communication
 * 
 * @author Chien-Liang Fok
 */
public class CompassTester {

	private CompassGUI compassPanel;
	
	public CompassTester(String portName) {
		try {
			connect(portName);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Create the GUI
		compassPanel = new CompassGUI();
		JFrame frame = new JFrame("Compass");
		frame.getContentPane().add(compassPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(frame.getRootPane());
		frame.setVisible(true);
	}
	
	private void connect ( String portName ) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() ) {
            System.out.println("Error: Port is currently in use");
            System.exit(1);
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort ) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
//                OutputStream out = serialPort.getOutputStream();
  
                serialPort.addEventListener(new SerialReader(in));
                serialPort.notifyOnDataAvailable(true);

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
                System.exit(1);
            }
        }     
    }
    
    /**
     * Handles the input coming from the serial port. A new line character
     * is treated as the end of a block in this example. 
     */
    public class SerialReader implements SerialPortEventListener {
        private InputStream in;
        private byte[] buffer = new byte[1024];
        
        public SerialReader ( InputStream in ) {
            this.in = in;
        }
        
        public void serialEvent(SerialPortEvent arg0) {
            int data;
          
            try {
                int len = 0;
                while ( ( data = in.read()) > -1 ) {
                    if ( data == '\n' ) {
                        break;
                    }
                    buffer[len++] = (byte) data;
                }
                String line = new String(buffer,0,len);
                
                if (debug())
                	System.out.print(line);
                
                String[] tokens = line.split("[=, ]");
//                System.out.println("Tokens:");
//                for (int i=0; i < tokens.length; i++) {
//                	System.out.println("\ttokens[" + i + "] = " + tokens[i]);
//                }
                if (tokens.length == 8 && tokens[0].equals("Heading")) {
                	double headingDeg = Double.valueOf(tokens[1]).doubleValue();
                	double headingRad = headingDeg / 180 * Math.PI;
                	double heading = 0;
                	if (headingRad < Math.PI) {
                		heading = -1 * headingRad;
                	} else {
                		heading = Math.PI - (headingRad - Math.PI);
                	}
                	compassPanel.setHeading(heading);
                }
            }
            catch ( IOException e ) {
                e.printStackTrace();
                System.exit(-1);
            }             
        }
    }
    
    public static boolean debug() {
    	return System.getProperty ("CompassTester.debug") != null;
    }
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + CompassTester.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-port <serial port>: The serial port to connect to (default /dev/ttyS0)");
		print("\t-debug: enable debug mode");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String port = "/dev/ttyS0";
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-port")) {
					port = args[++i];
				} 
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("CompassTester.debug", "true");
				}
				else {
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		print("Port: " + port);
		print("Debug: " + ((System.getProperty ("CompassTester.debug") != null) ? true : false));
		
		new CompassTester(port);
	}
}
