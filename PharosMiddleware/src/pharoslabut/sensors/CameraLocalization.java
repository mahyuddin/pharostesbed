package pharoslabut.sensors;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Scanner;
import java.util.Vector;

import pharoslabut.logger.Logger;
import playerclient3.structures.PlayerPoint2d;

public class CameraLocalization extends Thread {

	Integer checkInterval = 10; // 10 ms = 100 HZ
	String fileName = "";
	private String lastLine = "";
	
	private PlayerPoint2d currentLocation = new PlayerPoint2d();
	
	
	public CameraLocalization(Integer checkInterval, String fileName) {
		this.checkInterval = checkInterval;
		this.fileName = fileName;
		
		new Thread(this).start();
	}

	@Override
	public void run() {

		while(true) {
			try {
				Runtime rt = Runtime.getRuntime();
				String cmd = "tail -n 1 " + fileName; // get last line of file
				
				Process pr = rt.exec(cmd);

				BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

				String lastLine = input.readLine();
				if (lastLine == null || lastLine == "") {
					continue;
				}
				System.out.println(lastLine);
				 
				Scanner sc = new Scanner(lastLine);
				while (sc.hasNext()) {
					try {
						sc.next();
						double x = sc.nextDouble();
						double y = sc.nextDouble();
						sc.nextLine();
						currentLocation = new PlayerPoint2d(x, y);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
				
				
//			try {
//				RandomAccessFile fileHandler = new RandomAccessFile(fileName, "r" );
//				long fileLength = fileName.length() - 1;
//				StringBuilder sb = new StringBuilder();
//
//				for(long filePointer = fileLength; filePointer != -1; filePointer--) {
//					fileHandler.seek( filePointer );
//					byte readByte = fileHandler.readByte();
//
//					if(readByte == 0xA) {
//						if(filePointer == fileLength) {
//							continue;
//						} else {
//							break;
//						}
//					} else if(readByte == 0xD) {
//						if(filePointer == fileLength - 1) {
//							continue;
//						} else {
//							break;
//						}
//					}
//
//					sb.append((char)readByte);
//				}
//				lastLine = sb.reverse().toString();
//
//			} catch( java.io.FileNotFoundException e ) {
//				e.printStackTrace();
//				lastLine = null;
//			} catch( java.io.IOException e ) {
//				e.printStackTrace();
//				lastLine = null;
//			}
	}
	
	
	
	public static void main (String args[]) {
		new CameraLocalization(100, "/Users/Kevin/Desktop/JavaTools.txt");
	}

	/**
	 * @return the currentLocation
	 */
	public synchronized PlayerPoint2d getCurrentLocation() {
		return currentLocation;
	}
	
}
