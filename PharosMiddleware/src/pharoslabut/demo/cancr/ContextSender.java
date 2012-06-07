package pharoslabut.demo.cancr;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.sensors.GPSDataBuffer;

public class ContextSender implements Runnable {

	int port = 6666;
	GPSDataBuffer gpsDataBuffer;
	Location destLoc;
	
	public ContextSender(GPSDataBuffer gpsDataBuffer, Location destLoc) {
		this.gpsDataBuffer = gpsDataBuffer;
		this.destLoc = destLoc;
		new Thread(this).start();
//		double counter = 0;
//		while(true) {
//			sendContext(new Location(counter++,counter++), new Location(counter++,counter++));
//			try {
//				synchronized(this) {
//					wait(2000);
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	}
	
	public void sendContext(Location currLoc, Location destLoc) {
		
		String sendStr = (int)(destLoc.longitude()) + "," + (int)(destLoc.latitude()) 
			+ "," + (int)(destLoc.longitude()) + "," + (int)(destLoc.latitude());
		Logger.log("Sending the following context package: \"" + sendStr + "\"");
		try {
			Socket s = new Socket("localhost", port);
			OutputStream os = s.getOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(os);
			bos.write(sendStr.getBytes(Charset.defaultCharset()));
			bos.flush();
			os.flush();
			s.shutdownOutput();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.logErr("Problem connecting to contextinator: " + e.toString());
		}
	}
	
	public void setDestLoc(Location destLoc) {
		this.destLoc = destLoc;
	}
//	public static void main(String[] args) {
//		new ContextSender();
//	}

	@Override
	public void run() {
		Logger.log("Starting to send context packages...");
		while(true) {
//			Location currLoc;
//			try {
//				//currLoc = new Location(gpsDataBuffer.getCurrLoc());
//				//sendContext(currLoc, destLoc);
//			} catch (NoNewDataException e) {
//				e.printStackTrace();
//				Logger.logErr("Unable to get current location: " + e);
//			}
			
			sendContext(destLoc, destLoc);
			
			synchronized(this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
