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
		
//		new Thread(this).start();
	}

	@Override
	public void run() {
/*
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
				//System.out.println(lastLine);
				 
				Scanner sc = new Scanner(lastLine);
				sc.useDelimiter("-");

				try {
					if (sc.hasNext()) {
						sc.next();
						sc.useDelimiter(",");
						Double x = null, y = null;
						if (sc.hasNextDouble()) {
							x = sc.nextDouble();
							if (sc.hasNextDouble()) {
								y = sc.nextDouble();
								if (x != null && y != null) {
									currentLocation = new PlayerPoint2d(x, y);
									//System.out.println("Current Location is (" + x + "," + y + ")");
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
		*/
	}
	
	
	public static void main (String args[]) {
		// simple test of logic
//		new CameraLocalization(100, "/Users/Kevin/Desktop/JavaTools.txt");
	}

	/**
	 * @return the currentLocation
	 */
	public synchronized PlayerPoint2d getCurrentLocation() {
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = "tail -n 2 " + fileName; // get last 2 lines of file
			
			Process pr = rt.exec(cmd);

			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			String lastLine = input.readLine();
			if (lastLine == null || lastLine == "") {
				System.out.println("Last line of " + fileName + " was empty or null.");
				return null;
			}
			System.out.println(lastLine);
			 
			Scanner sc = new Scanner(lastLine);
			sc.useDelimiter(" ");

			try {
				if (sc.hasNext()) {
					System.out.println(sc.next());
					sc.useDelimiter(",");
					Double x = null, y = null;
					if (sc.hasNext()) {
						x = Double.parseDouble(sc.next().trim());
						if (sc.hasNext()) {
							y = Double.parseDouble(sc.next().trim());
							if (x != null && y != null) {
								currentLocation = new PlayerPoint2d(x, y);
								System.out.println("Current Location is (" + x + "," + y + ")");
								return currentLocation;
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
