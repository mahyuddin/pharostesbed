package robotPerimeter;
import java.nio.ByteBuffer;
import java.util.Arrays;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;

import edu.utexas.ece.mpc.context.summary.HashMapContextSummary;


public class HashMapContextSummaryInterface {

	public static void insertLocationStamp(HashMapContextSummary summ, String key, LocationStamp loc )
	{
		if (loc==null)
			return;
		storeDouble(summ, key+"latitude", loc.location.latitude());
		storeDouble(summ, key+"longitude", loc.location.longitude());
		storeDouble(summ, key+"elevation", loc.location.elevation());
		storeDouble(summ, key+"timestamp", loc.timestamp);

	}
	public static LocationStamp retrieveLocationStamp(HashMapContextSummary summ, String key)
	{
		if (summ == null) return null;
		LocationStamp loc = new LocationStamp();
		double lat = retrieveDouble(summ, key+"latitude");
		double longit = retrieveDouble(summ, key+"longitude");
		double elev = retrieveDouble(summ, key+"elevation");
		loc.location = new Location(lat, longit, elev);
		loc.timestamp = retrieveDouble(summ, key+"timestamp");

		return loc;
	}
	private static void storeDouble(HashMapContextSummary summ, String key, double dub)
	{
		int [] x = convertDoubleToInteger(dub);
		summ.put(key+"0", x[0]);
		summ.put(key+"1", x[1]);
	}
	private static double retrieveDouble (HashMapContextSummary summ, String key)
	{
		double dub;
		Logger.log(key);
		if (summ.get(key+"0")==null || summ.get(key+"1")==null) 
			return 0;
		int a = summ.get(key+"0");
		int b = summ.get(key+"1");
		int [] x = {a, b};
		dub = convertIntegerToDouble(x);
		return dub;
	}
	private static int [] convertDoubleToInteger(double dub)
	{
		int [] integ = new int[2];
		byte [] bytes = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		
//		dub = 57657.55645645; //TODO remove
		
		buf.putDouble(dub);
		buf.position(0);
//		
//		
//		Logger.log(""+dub);
//		Logger.log(buf.toString());
//		Logger.log(Arrays.toString(buf.array()));
		
		for (int i=0;i<2;i++){
			integ[i] = buf.getInt();
//			Logger.log(Arrays.toString(integ));
		}
		return integ;
	}
	private static double convertIntegerToDouble(int [] x)
	{
		double dub;
		byte [] bytes = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.putInt(x[0]);
		buf.putInt(x[1]);
		buf.position(0);
		dub = buf.getDouble();

		return dub;
	}
	
	//TargetSightings have keys in 1 of 2 ways, depending on the type of context.
	//in local summaries (HashMapContextSummary), the key is the id of the target seen
	//in targetGroupSummaries (HashMapContextGroupSummary), the key is the id of the host who saw it
	
	public static TargetSighting retrieveTargetSighting(
			HashMapContextSummary summ, int Tid) {
		TargetSighting sighting = new TargetSighting();
		String prefix = "targetSightingId" + Tid;
		if (summ.get(prefix+"hostId")==null) return null;
		sighting.hostId = summ.get(prefix+"hostId");
		sighting.targetId = summ.get(prefix+"targetId");
		sighting.timestamp = retrieveDouble(summ, prefix+"timestamp");
		sighting.slope = retrieveDouble(summ, prefix+"slope");
		sighting.yIntercept = retrieveDouble(summ, prefix+"yIntercept");
		sighting.targetLocation = retrieveLocationStamp(summ, prefix + "targetlocationstamp");

		return sighting;
	}

	public static void insertTargetSighting(
			HashMapContextSummary summ, TargetSighting sighting) {
		String prefix = "targetSightingId" + sighting.hostId;

		summ.put(prefix+"hostId",sighting.hostId );
		summ.put(prefix+"targetId", sighting.targetId);
		storeDouble(summ, prefix+"timestamp", sighting.timestamp);
		storeDouble(summ, prefix+"slope", sighting.slope);
		storeDouble(summ, prefix+"yIntercept", sighting.yIntercept);
		insertLocationStamp(summ,prefix+"targetlocationstamp", sighting.targetLocation);

	}
	public static void removeTargetSighting(HashMapContextSummary summ, int Tid)
	{
		String prefix = "targetSightingId" + Tid;
		summ.remove(prefix + "hostId");
		summ.remove(prefix+"targetId");
		summ.remove(prefix+"timestamp");
		summ.remove(prefix+"slope");
		summ.remove(prefix+"yIntercept");

	}


}
