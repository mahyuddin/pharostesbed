package pharoslabut.demo.autoIntersection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * The intersection specifications.  Describes the intersection, i.e., how many roads are intersecting, how many lanes,
 * whether the vehicles may turn, etc
 * 
 * @author Michael Hanna
 * @author Chien-Liang Fok
 */
public class IntersectionSpecs implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6517026202617597952L;
	
	/**
	 * The total number of ways in the used intersection
	 */
	private int nWays;
	
	/**
	 * Nlanes is the number of lanes in each way of the intersection
	 * It should be an even number
	 * Nlanes/2 are going North, and Nlanes/2 are going South
	 */
	private int nLanes;
	
	/**
	 * The total number of lanes entering the intersection
	 */
	private int nEntries;
	
	/**
	 * The total number of lanes exiting the intersection
	 */
	private int nExits;
	
	/**
	 * The width of the intersection in cm
	 * assuming the intersection length == the intersection width
	 */
	private int intersectionWidth;
	
	/**
	 * mapEntry2Exits is a hashtable to map every entry point to a list(vector) of allowed exit points
	 */
	private HashMap<Integer, Vector<Integer>> mapEntry2Exits;
	
	/**
	 * mapEntry2Direction is a hashtable to map every entry point to the corresponding entry direction
	 * The direction type is a Character
	 * 'N' = North
	 * 'S' = South
	 * 'E' = EAST
	 * 'W' = WEST
	 * 'I' = IN the intersection, in case of circular intersections
	 * 'O' = OUT of the intersection, in case of circular intersections
	 */
	private HashMap<Integer, Character> mapEntry2Direction;
	
	/**
	 * mapExit2Direction is a hashtable to map every exit point to the corresponding exit direction
	 * The direction type is a Character
	 * 'N' = North
	 * 'S' = South
	 * 'E' = EAST
	 * 'W' = WEST
	 * 'I' = IN the intersection, in case of circular intersections
	 * 'O' = OUT of the intersection, in case of circular intersections
	 */
	private HashMap<Integer, Character> mapExit2Direction;
	
	/**
	 * The constructor
	 * @param nWays
	 * @param nLanes
	 */
	public IntersectionSpecs(int nWays, int nLanes, int intersectionWidth, HashMap<Integer, Vector<Integer>> mapEntry2Exits,
			HashMap<Integer, Character> mapEntry2Direction, HashMap<Integer, Character> mapExit2Direction) {
		this.nWays = nWays;
		this.nLanes = nLanes;
		this.nEntries = (nLanes/2) * nWays;
		this.nExits = (nLanes/2) * nWays;
		this.intersectionWidth = intersectionWidth;
		
		this.mapEntry2Exits = mapEntry2Exits;
		this.mapEntry2Direction = mapEntry2Direction;
		this.mapExit2Direction = mapExit2Direction;
	}
	
	public int getNWays() {
		return this.nWays;
	}
	
	public int getNLanes() {
		return this.nLanes;
	}
	
	public int getNEntries() {
		return this.nEntries;
	}
	
	public int getNExits() {
		return this.nExits;
	}
	
	public int getIntersectionWidth() {
		return this.intersectionWidth;
	}
	
	public HashMap<Integer, Vector<Integer>> getMapEntry2Exits() {
		return mapEntry2Exits;
	}
	
	public HashMap<Integer, Character> getMapEntry2Direction() {
		return mapEntry2Direction;
	}
	
	public HashMap<Integer, Character> getMapExit2Direction() {
		return mapExit2Direction;
	}
	
	@SuppressWarnings("unchecked")
	public void printHashMap(HashMap hm) {
		Iterator iterator = hm.keySet().iterator();  
		while (iterator.hasNext()) {  
			String key = iterator.next().toString();  
			String value = hm.get(key).toString();  
			System.out.println(key + " " + value);  
		}  
	}
	
	@Override
	public String toString() {
		return "IntersectionSpecs";
	}

}
