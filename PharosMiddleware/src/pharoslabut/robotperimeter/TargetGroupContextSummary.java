package robotPerimeter;
import java.util.ArrayList;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import Jama.Matrix;
import edu.utexas.ece.mpc.context.summary.GroupContextSummary;
import edu.utexas.ece.mpc.context.summary.HashMapContextSummary;


@SuppressWarnings("serial")
public class TargetGroupContextSummary extends GenericGroupContextSummary {

	public static int timeThreshold = 5 * 1000; //(ms) disregard sightings older than this number of seconds


	public TargetGroupContextSummary(int gId) {
		super(gId);
	}

	public TargetGroupContextSummary (TargetGroupContextSummary a)
	{
		super(a);
	}
	
	public TargetGroupContextSummary getCopy()
	{
		return this;
		//TODO replace with actual copy
//		TargetGroupContextSummary copy = new TargetGroupContextSummary(this);
//		copy.addMemberIds(this.getMemberIds());
//		return copy;
	}

    public GroupContextSummary getGroupCopy() {
    	return getCopy();
    }
	
	public LocationStamp getTargetLocation()
	{
		LocationStamp target = HashMapContextSummaryInterface.retrieveLocationStamp(this, "target");
		return target;
	}
	public void addLocalSummary(HashMapContextSummary local)
	{
		if (local==null) return;
		super.addLocalSummary(local);
		TargetSighting sighting = HashMapContextSummaryInterface.retrieveTargetSighting(local, local.getId());
		addTargetSighting(sighting);
		this.put("groupRadius", this.getMemberIds().size()*10+20);

	}
	public void addTargetSighting (TargetSighting sight)
	{
		if (sight == null) return;
		//remove target Sightings older than 5 seconds
		Integer[] memberIds = new Integer[this.getMemberIds().size()];
		this.getMemberIds().toArray(memberIds);

		for (int i=0;i<memberIds.length;i++)
		{
			int hostId = memberIds[i];
			TargetSighting sighting = HashMapContextSummaryInterface.retrieveTargetSighting(this, hostId);
			if (sighting==null) continue;
			if (sighting.timestamp < System.currentTimeMillis() - timeThreshold)
			{
				HashMapContextSummaryInterface.removeTargetSighting(this, hostId);
			}
		}

		TargetSighting oldSighting = HashMapContextSummaryInterface.retrieveTargetSighting(this, sight.hostId);
		Logger.log(sight.timestamp + " " + System.currentTimeMillis() + " " + timeThreshold);
		if (sight.timestamp > System.currentTimeMillis() - timeThreshold && (oldSighting==null || sight.timestamp > oldSighting.timestamp)) //newer or first sighting from given host
		{
			HashMapContextSummaryInterface.insertTargetSighting(this, sight);
			calculateBestTargetGuess();
		}
	}
	public void calculateBestTargetGuess()
	{
	

		//best guess for intercept

		double targetTimeStampLatest = 0;
		double latitude = 0;
		double longitude =0;
		
//		ArrayList<TargetSighting> allSightings = new ArrayList<TargetSighting>();
		int size = 0;
		for (int id: this.getMemberIds())
		{
			TargetSighting s = HashMapContextSummaryInterface.retrieveTargetSighting(this,id);
			if (s!=null){
				latitude+=s.targetLocation.location.latitude();
				longitude+=s.targetLocation.location.longitude();
				if (s.timestamp > targetTimeStampLatest)
					targetTimeStampLatest = s.timestamp;
				size++;
//				allSightings.add(s);
			}
		}

		latitude /= size;
		longitude /= size;
		LocationStamp targetLocation = new LocationStamp();
		targetLocation.timestamp = targetTimeStampLatest;

		targetLocation.location = new Location(latitude, longitude);

		HashMapContextSummaryInterface.insertLocationStamp(this, "target", targetLocation);
	}
		
//		//best guess for intercept
//
//		ArrayList<TargetSighting> allSightings = new ArrayList<TargetSighting>();
//		for (int id: this.getMemberIds())
//		{
//			TargetSighting s = HashMapContextSummaryInterface.retrieveTargetSighting(this,id);
//			if (s!=null)
//				allSightings.add(s);
//		}
//
//
//		//      /*
//		//      * matrix format:
//		//      * [ -m 1   * [x    = [y intercepts]
//		//      *   -m 1      y]
//		//      *   ...
//		//      *   -m 1 ]
//		//      */
//		//		public Matrix solve(Matrix B)
//		//		Solve A*X = B
//		//		Parameters:
//		//		B - right hand side
//		//		Returns:
//		//		solution if A is square, least squares solution otherwise
//
//		double [][] A = new double[allSightings.size()][2];
//		double [][] B = new double[allSightings.size()][1];
//
//		double targetTimeStampLatest = 0;
//
//		if (allSightings.size()<2){
//			return;
//		}
//		for (int i = 0; i < A.length;i++)
//		{
//			TargetSighting temp = allSightings.get(i);
//			A[i][0] = - temp.slope;
//			A[i][1] = 1;
//			B[i][0] = temp.yIntercept;
//			targetTimeStampLatest = (temp.timestamp > targetTimeStampLatest)?temp.timestamp:targetTimeStampLatest;
//		}
//
//		Matrix AMatrix = new Matrix(A);
//		Matrix BMatrix = new Matrix(B);
//
//		Matrix loc = AMatrix.solve(BMatrix);
//
//		LocationStamp targetLocation = new LocationStamp();
//		targetLocation.timestamp = targetTimeStampLatest;
//		double latitude = loc.get(0, 0);
//		double longitude = loc.get(1, 0);
//		targetLocation.location = new Location(latitude, longitude);
//
//		HashMapContextSummaryInterface.insertLocationStamp(this, "target", targetLocation);
//	}

}
