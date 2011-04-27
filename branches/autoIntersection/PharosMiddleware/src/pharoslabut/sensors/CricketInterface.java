package pharoslabut.sensors;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;

/*TODO: add param to constr ser port crick is att, own thread that reads data from serial,
 * parse out into for each line and package into class "CricketData" and publishes data to
 * registered listeners 
 * 
 */

public class CricketInterface {

	Vector<CricketDataListener> listeners = new Vector<CricketDataListener>();
	
	public CricketInterface(String portName) {
		try {
			connect(portName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Registers a cricket mote data listener.
	 * 
	 * @param cdl The cricket mote data listener.
	 */
	public void registerCricketDataListener(CricketDataListener cdl) {
		listeners.add(cdl);
	}
	
	/**
	 * Deregisters a cricket mote data listener.
	 * 
	 * @param cdl The cricket mote data listener.
	 */
	public void deregisterCricketDataListener(CricketDataListener cdl) {
		listeners.remove(cdl);
	}
	
	/**
	 * Passes the new cricket data to each registered cricket data listener.
	 * 
	 * @param cd New cricket data.
	 */
	private void newCricketData(CricketData cd) {
		
		log("Publishing new cricket data: " + cd);
		
		Enumeration<CricketDataListener> e = listeners.elements();
		while (e.hasMoreElements()) {
			e.nextElement().newCricketData(cd);
		}
	}
		
    void connect (String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() ){
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort ) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.FLOWCONTROL_NONE);
                
                InputStream in = serialPort.getInputStream();
                (new Thread(new SerialReader(in))).start();

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    /**
     * @author Seth Gee
     * Reads serial data coming in and parses all information.
     * Creates new CricketData object with parsed data.
     *
     */
    public class SerialReader implements Runnable {
        InputStream in;
        BufferedReader reader;
        
        public SerialReader ( InputStream in ) {
            this.in = in;
            this.reader = new BufferedReader(new InputStreamReader(in));
        }
        
        public void run () {
            String line = null;
            try{
            	
               while ((line = reader.readLine()) != null) {
            	 String[] rawStr = line.split(","); 		// parse string by ',' delimiter
            	 
            	 if (rawStr.length == 7) {
	            	 String version    = parseData(rawStr[0]);	// parse out version number
	         		 String cricketID  = parseData(rawStr[1]);	// parse out cricket mote ID number
	         		 String spaceID    = parseData(rawStr[2]);  // parse out space ID string
	         		 String distance   = parseData(rawStr[3]);  // parse out distance to beacon
	         		 String duration   = parseData(rawStr[4]);  // parse out duration 
	         		 String flightTime = parseData(rawStr[5]);  // parse out flight time of sonar
	         		 String sysTime    = parseData(rawStr[6]);  // parse out system time
	         		 newCricketData(new CricketData(Double.parseDouble(version), cricketID, spaceID, Integer.parseInt(distance), Integer.parseInt(duration), Long.parseLong(flightTime), Long.parseLong(sysTime)));
            	 }
            	 else if (rawStr.length == 4) {
            		 String version    = parseData(rawStr[0]);	// parse out version number
	         		 String cricketID  = parseData(rawStr[1]);	// parse out cricket mote ID number
	         		 String spaceID    = parseData(rawStr[2]);  // parse out space ID string
	         		 String sysTime    = parseData(rawStr[3]);  // parse out system time
	         		 newCricketData(new CricketData(Double.parseDouble(version), cricketID, spaceID, Long.parseLong(sysTime)));
            	 }
        		 
            	 System.out.println("Read line has " + rawStr.length + " elements in it with "+ line.length() + " characters: \"" + line + "\"");
               }
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }            
        }

		private String parseData(String str) {
			String[] fullParse = str.split("=");
			return fullParse[1];
		}
    }
    
    private void log(String msg) {
    	System.out.println(msg);
    }
    
    //take out later
    public static void main(String[] args) {
            new CricketInterface("/dev/ttyUSB0");
	}

}
