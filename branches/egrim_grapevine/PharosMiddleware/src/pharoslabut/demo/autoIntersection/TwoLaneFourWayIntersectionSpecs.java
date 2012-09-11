package pharoslabut.demo.autoIntersection;

import java.util.HashMap;
import java.util.Vector;

/**
 * The intersection Specifications for a 2-lane, 4-way intersection
 * @author Michael Hanna
 */
public class TwoLaneFourWayIntersectionSpecs extends IntersectionSpecs {

	private static final long serialVersionUID = -2737528121576109063L;
		
	private static HashMap<Integer, Vector<Integer>> mapEntry2Exits = mapEntry2Exits();
	private static HashMap<Integer, Character> mapEntry2Direction = mapEntry2Direction();
	private static HashMap<Integer, Character> mapExit2Direction = mapExit2Direction();
	
	
	public TwoLaneFourWayIntersectionSpecs(int intersectionWidth) {
		super(4, 2, intersectionWidth, mapEntry2Exits, mapEntry2Direction, mapExit2Direction);
	}
	
	public static HashMap<Integer, Vector<Integer>> mapEntry2Exits() {
		HashMap<Integer, Vector<Integer>> mapEntry2Exits = new HashMap<Integer, Vector<Integer>>();
		Vector<Integer> v;
		
		v = new Vector<Integer>();
		v.add(2);
		v.add(3);
		v.add(4);
		mapEntry2Exits.put(1, v);
		
		v = new Vector<Integer>();
		v.add(1);
		v.add(3);
		v.add(4);
		mapEntry2Exits.put(2, v);
		
		v = new Vector<Integer>();
		v.add(1);
		v.add(2);
		v.add(4);
		mapEntry2Exits.put(3, v);
		
		v = new Vector<Integer>();
		v.add(1);
		v.add(2);
		v.add(3);
		mapEntry2Exits.put(4, v);
		
		return mapEntry2Exits;
	}
	
	public static HashMap<Integer, Character> mapEntry2Direction() {
		HashMap<Integer, Character> mapEntry2Direction = new HashMap<Integer, Character>();
		mapEntry2Direction.put(1, 'N');
		mapEntry2Direction.put(2, 'W');
		mapEntry2Direction.put(3, 'S');
		mapEntry2Direction.put(4, 'E');
		return mapEntry2Direction;
	}
	
	public static HashMap<Integer, Character> mapExit2Direction() {
		HashMap<Integer, Character> mapExit2Direction = new HashMap<Integer, Character>();
		mapExit2Direction.put(1, 'S');
		mapExit2Direction.put(2, 'E');
		mapExit2Direction.put(3, 'N');
		mapExit2Direction.put(4, 'W');
		return mapExit2Direction;
	}
}
