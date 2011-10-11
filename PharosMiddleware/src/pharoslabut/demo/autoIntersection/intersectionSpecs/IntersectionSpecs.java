package pharoslabut.demo.autoIntersection.intersectionSpecs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import pharoslabut.logger.Logger;

/**
 * The intersection specifications.  Describes the intersection, i.e., 
 * how many roads are intersecting, how many lanes,
 * whether the vehicles may turn, etc
 * 
 * @author Michael Hanna
 * @author Chien-Liang Fok
 */
public class IntersectionSpecs implements java.io.Serializable {
	
	private static final long serialVersionUID = -6517026202617597952L;
	
	/**
	 * The name of the intersection.
	 */
	private String name;
	
	/**
	 * A list of roads that are at the intersection.
	 */
	private Vector<Road> roads;
	
	/**
	 * Maps every entry point to a list of allowed exit points
	 */
	private HashMap<EntryPoint, Vector<ExitPoint>> mapEntryToExits;
	
	/**
	 * The constructor
	 * 
	 * @param name The name of the intersection.
	 */
	public IntersectionSpecs(String name) {
		this.name = name;
	}
	
	/**
	 * Adds a road to the intersection.
	 * 
	 * @param road The road to add.
	 */
	public void addRoad(Road road) {
		roads.add(road);
		
		// For each entry point, add it to the mapEntryToExits hashmap.
		Iterator<EntryPoint> entryPoints = road.getEntryPoints();
		while (entryPoints.hasNext()) {
			EntryPoint entryPoint = entryPoints.next();
			
			if (!mapEntryToExits.containsKey(entryPoint))
				mapEntryToExits.put(entryPoint, new Vector<ExitPoint>());
			else
				Logger.logErr("Entry point " + entryPoint + " already in mapEntryToExits.");
		}
	}
	
	/**
	 * Adds a valid exit point to an entry point.  This is an exit through which the vehicle
	 * can travel if the vehicle entered in the specified entry point.
	 * 
	 * @param entryPoint The entry point.
	 * @param exitPoint The exit point.
	 */
	public void addValidExitPoint(EntryPoint entryPoint, ExitPoint exitPoint) {
		Vector<ExitPoint> exitPoints = mapEntryToExits.get(entryPoint);
		if (exitPoints != null) {
			exitPoints.add(exitPoint);
		} else {
			Logger.logErr(entryPoint + " not in mapEntryToExits hash map.");
		}
	}
	
	/**
	 * 
	 * @return All of the exit points in the intersection.
	 */
	public Vector<ExitPoint> getExitPoints() {
		Vector<ExitPoint> exitPoints = new Vector<ExitPoint>();
		for (int i=0; i < roads.size(); i++) {
			roads.get(i).saveExitPoints(exitPoints);
		}
		return exitPoints;
	}
	
	/**
	 * 
	 * @return All of the entry points in the intersection.
	 */
	public Vector<EntryPoint> getEntryPoints() {
		Vector<EntryPoint> entryPoints = new Vector<EntryPoint>();
		for (int i=0; i < roads.size(); i++) {
			roads.get(i).saveEntryPoints(entryPoints);
		}
		return entryPoints;
	}
	
	
	
//	@SuppressWarnings("unchecked")
//	public void printHashMap(HashMap hm) {
//		Iterator iterator = hm.keySet().iterator();  
//		while (iterator.hasNext()) {  
//			String key = iterator.next().toString();  
//			String value = hm.get(key).toString();  
//			System.out.println(key + " " + value);  
//		}  
//	}
	
	@Override
	public String toString() {
		return "IntersectionSpecs";
	}

}
