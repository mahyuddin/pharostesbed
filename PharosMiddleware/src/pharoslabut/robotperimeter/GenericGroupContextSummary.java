package pharoslabut.robotperimeter;
import java.util.Map;
import java.util.TreeMap;

import edu.utexas.ece.mpc.context.summary.HashMapContextSummary;
import edu.utexas.ece.mpc.context.summary.HashMapGroupContextSummary;

@SuppressWarnings("serial")
public class GenericGroupContextSummary extends HashMapGroupContextSummary {

	public GenericGroupContextSummary(int gId) {
		super(gId);
	}

	public GenericGroupContextSummary(GenericGroupContextSummary a) {
		super(a);
}

	public void addLocalSummary(HashMapContextSummary local) {
		super.addMemberId(local.getId());
		HashMapContextSummaryInterface.insertLocationStamp(this,
				"hostLocationId"+local.getId(), HashMapContextSummaryInterface
				.retrieveLocationStamp(local, "ownLocation"));
	}
	public Map<Integer,LocationStamp> retrieveAllLocalLocations()
	{
		Map<Integer,LocationStamp> all = new TreeMap<Integer,LocationStamp>();
		for (Integer g : getMemberIds())
		{
			all.put(g, HashMapContextSummaryInterface.retrieveLocationStamp(this, "hostLocationId" + g.intValue()));
		}
		return all;
	}
}
