package pharoslabut.cpsAssert.cpsAssertionFramework;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import playerclient3.structures.PlayerPoint2d;

public class Sensors {
	
	
	public final static Object[][] SensorsTable = { {SensorType.CAMERA_LOCALIZATION, PlayerPoint2d.class, "robotLocation", "cameraLoc", "cameraLocalization", "location"} //, 
											//{ add other rows for other sensors here},
											, {SensorType.CRICKET, PlayerPoint2d.class, "cricket", "beacon", "cricketMote", "mote" }
											};
	
	
	/**
	 * determines if a keyword matches with a particular sensor 
	 * @param keyword
	 * @return the index of the row (determines which sensor the keyword matches with. Returns -1 if no match is found.
	 */
	public static int keywordMatcher(String keyword) {
		for (int i = 0; i < SensorsTable.length; i++) {
			for (int j = 0; j < SensorsTable[i].length; j++) {
				if (keyword.equals(SensorsTable[i][j]))
					return i;
			}
		}
		return -1;
	}
	
	
	/**
	 * returns a key-value pair of <SensorType, Return Data Type> for the sensor specified by <b> sensorIndex </b>
	 * @param sensorIndex
	 * @return 
	 * @throws IndexOutOfBoundsException
	 */
	public static Entry<SensorType, Object> getMatchedSensor(int sensorIndex) throws IndexOutOfBoundsException {
		
		return new AbstractMap.SimpleEntry<SensorType, Object>((SensorType) SensorsTable[sensorIndex][0], SensorsTable[sensorIndex][1]);
		
	}
	

//	RANGE,
//	SHARP_IR,
//	CRICKET,
//	COMPASS,
//	ODOMETER,
//	POSITION2D,
//	LOCATION,
//	CAMERA_LOCALIZATION
}
