package pharoslabut.cartographer;
import java.util.*;

// may not need this.. we could bypass this and just use MapSector
public class Astar {

	public Astar(){
		List<OrderedPair> goalPoints = new ArrayList<OrderedPair>();
		int side = 11;
		int radius = side/2;
		OrderedPair start;
		OrderedPair end;
		start = new OrderedPair(radius,radius);	// middle of the sector
		
		// right now, just four possible places
		end = new OrderedPair(start.getX()+radius,start.getY());
		goalPoints.add(end);
		end = new OrderedPair(start.getX()-radius,start.getY());
		goalPoints.add(end);
		end = new OrderedPair(start.getX(),start.getY()+radius);
		goalPoints.add(end);
		end = new OrderedPair(start.getX(),start.getY()-radius);
		goalPoints.add(end);
		
		// We don't have to create the mapsector here in Astar. In all honesty, MapSector IS the Astar algorithm
		// Reminder to change name to Astar and delete this java file
		MapSector sector = new MapSector(side,side,start,goalPoints);
		sector.findPath();	// this exits as soon as it finds a path	
	}
	
}
