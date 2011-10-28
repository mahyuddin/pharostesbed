package pharoslabut.cps;


import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.CricketData;
import pharoslabut.sensors.CricketDataListener;
import pharoslabut.sensors.CricketInterface;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.Position2DListener;
import pharoslabut.sensors.RangerDataBuffer;
import pharoslabut.sensors.RangerListener;

import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.RangerInterface;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;
import playerclient3.structures.position2d.PlayerPosition2dData;
import playerclient3.structures.ranger.PlayerRangerData;


public class CPSAssertSensor implements CricketDataListener, RangerListener, Position2DListener{


	public CPSAssertSensor (
			PlayerClient pc, FileLogger flogger,
			boolean useCricket, CricketInterface ci, String cricketSerialPort, 
			boolean useRanger, RangerInterface ri, RangerDataBuffer rdb,
			boolean useOdometry, Integer odometryDeviceIndex, Position2DInterface odometryInterface, Position2DBuffer odometryBuffer,
			boolean useCompass, Integer compassDeviceIndex, Position2DInterface compassInterface, CompassDataBuffer compassBuffer
			) 
	{
		Logger.setFileLogger(flogger); // set up logger (this does nothing if logger was already set up)

		if (useCricket) {
			if (ci == null) {
				if (cricketSerialPort == null) {
					throw new NullPointerException("CPSAssertSensor Constructor: Parameter \"cricketSerialPort\" was null when trying to create a new CricketInterface.\n" + 
							"Please specify either a non-null \"cricketSerialPort\" or CricketInterface, or set the useCricket boolean value to false to disable Cricket Mote usage.");
				}
				ci = new CricketInterface(cricketSerialPort);
			}
			ci.registerCricketDataListener(this);
		}
		
		if (useRanger) {
			if (rdb == null) {
				if (ri == null) {
					ri = new RangerInterface(pc);
				}
				rdb = new RangerDataBuffer(ri);
			}
			rdb.addRangeListener(this);
		}
		
		if (useOdometry) {
			if (odometryBuffer == null) {
				if (odometryInterface == null) {
					if (pc == null) {
						throw new NullPointerException("CPSAssertSensor Constructor: PlayerClient \"pc\" was null when connecting to Odometry Interface.\n" + 
								"Please specify one of the values, or set the useOdometry boolean value to false to disable odometry usage."); 
					}
					if (odometryDeviceIndex == null) {
						odometryDeviceIndex = 0;
						Logger.log("\"odometryDeviceIndex\" was null, will use value = 0 as default.");
					}
					try{
						odometryInterface = pc.requestInterfacePosition2D(odometryDeviceIndex, PlayerConstants.PLAYER_OPEN_MODE);;
					} catch (PlayerException e) { 
						System.out.println("Error: could not connect to Odometry's Position2dInterface."); 
						System.exit(1); 
					}
				}
				odometryBuffer = new Position2DBuffer(odometryInterface);
			}
			odometryBuffer.addPos2DListener(this);
		}
		
		if (useCompass) {
			if (compassBuffer == null) {
				if (compassInterface == null) {
					if (pc == null) {
						throw new NullPointerException("CPSAssertSensor Constructor: PlayerClient \"pc\" was null when connecting to Compass Interface.\n" + 
								"Please specify one of the values, or set the useCompass boolean value to false to disable Compass usage."); 
					}
					if (compassDeviceIndex == null) {
						compassDeviceIndex = 1;
						Logger.log("\"compassDeviceIndex\" was null, will use value = 1 as default.");
					}
					try{
						compassInterface = pc.requestInterfacePosition2D(compassDeviceIndex, PlayerConstants.PLAYER_OPEN_MODE);;
					} catch (PlayerException e) { 
						System.out.println("Error: could not connect to Compass's Position2dInterface."); 
						System.exit(1); 
					}
				}
				compassBuffer = new CompassDataBuffer(compassInterface);
			}
			compassBuffer.addPos2DListener(this);
		}


	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub


	}

	@Override
	public void newCricketData(CricketData cd) {
		// TODO Auto-generated method stub

	}

	@Override
	public void newRangerData(PlayerRangerData data) { /*do nothing*/ }

	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) { /*do nothing*/ }

	
	
	
	public void AssertCricket(Double actual, Inequality ineq) {

	}







}
