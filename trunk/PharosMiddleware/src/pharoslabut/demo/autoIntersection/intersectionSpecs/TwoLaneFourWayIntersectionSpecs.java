package pharoslabut.demo.autoIntersection.intersectionSpecs;

import pharoslabut.navigate.Heading;
import pharoslabut.navigate.Location;

/**
 * The intersection Specifications for a 2-lane, 4-way intersection.
 * 
 * @author Chien-Liang Fok
 * @author Michael Hanna
 */
public class TwoLaneFourWayIntersectionSpecs extends IntersectionSpecs {

	private static final long serialVersionUID = -2737528121576109063L;

	public TwoLaneFourWayIntersectionSpecs() {
		super("TwoLaneFourWayIntersection");
		
		// Define the entry points
		// Use fake locations for now
		EntryPoint e1 = new EntryPoint("E1", Heading.NORTH, new Location(1,3)); 
		EntryPoint e2 = new EntryPoint("E2", Heading.WEST, new Location(3,4));
		EntryPoint e3 = new EntryPoint("E3", Heading.SOUTH, new Location(4,2));
		EntryPoint e4 = new EntryPoint("E4", Heading.EAST, new Location(2,1));
		
		// Define the exit points
		ExitPoint x1 = new ExitPoint("X1", Heading.SOUTH, new Location(1,2));
		ExitPoint x2 = new ExitPoint("X2", Heading.EAST, new Location(1,2));
		ExitPoint x3 = new ExitPoint("X3", Heading.NORTH, new Location(1,2));
		ExitPoint x4 = new ExitPoint("X4", Heading.WEST, new Location(1,2));
		
		// Define the roads
		Road road1 = new Road();
		road1.addEntryPoint(e1);
		road1.addEntryPoint(e3);
		road1.addExitPoint(x1);
		road1.addExitPoint(x3);
		
		Road road2 = new Road();
		road2.addEntryPoint(e2);
		road2.addEntryPoint(e4);
		road2.addExitPoint(x2);
		road2.addExitPoint(x4);
		
		addRoad(road1);
		addRoad(road2);
		
		// Define the valid exit points through which a vehicle may exit
		// given a specific entry point.  In the following definition, there are no U-turns.
		addValidExitPoint(e1, x2);
		addValidExitPoint(e1, x3);
		addValidExitPoint(e1, x4);
		
		addValidExitPoint(e2, x1);
		addValidExitPoint(e2, x3);
		addValidExitPoint(e2, x4);
		
		addValidExitPoint(e3, x1);
		addValidExitPoint(e3, x2);
		addValidExitPoint(e3, x4);
		
		addValidExitPoint(e4, x1);
		addValidExitPoint(e4, x2);
		addValidExitPoint(e4, x3);
	}

	@Override
	public boolean willIntersect(String entryPointID1, String exitPointID1,
			String entryPointID2, String exitPointID2) 
	{
		// For now, just hard-code in the two possible forms of parallelism.
		// TODO: Use math to determine whether the paths will intersect based on the entry and exit point locations.

		if (entryPointID1.equals("E1") && exitPointID1.equals("X3")) 
			return !(entryPointID2.equals("E3") && exitPointID2.equals("X1"));
		else if (entryPointID2.equals("E1") && exitPointID2.equals("X3")) 
			return !(entryPointID1.equals("E3") && exitPointID1.equals("X1"));
		
		else if (entryPointID1.equals("E4") && exitPointID1.equals("X2")) 
			return !(entryPointID2.equals("E2") && exitPointID2.equals("X4"));
		else if (entryPointID2.equals("E4") && exitPointID2.equals("X2")) 
			return !(entryPointID1.equals("E2") && exitPointID1.equals("X4"));
			
		else
			return false;
		
	}
}
