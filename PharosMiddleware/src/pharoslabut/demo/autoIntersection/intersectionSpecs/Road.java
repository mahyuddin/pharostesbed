package pharoslabut.demo.autoIntersection.intersectionSpecs;

import java.util.Iterator;
import java.util.Vector;

/**
 * A road is represented as a set of entry and exit points to and from the intersection.
 * 
 * @author Chien-Liang Fok
 */
public class Road {

	private Vector<ExitPoint> exitPoints = new Vector<ExitPoint>();
	
	private Vector<EntryPoint> entryPoints = new Vector<EntryPoint>();
	
	public Road() {
	}
	
	public void addExitPoint(ExitPoint exitPoint) {
		exitPoints.add(exitPoint);
	}
	
	public void addEntryPoint(EntryPoint entryPoint) {
		entryPoints.add(entryPoint);
	}
	
	public int numEntryPoints() {
		return entryPoints.size();
	}
	
	public int numExitPoints() {
		return exitPoints.size();
	}
	
	public ExitPoint getExitPoint(int i) {
		return exitPoints.get(i);
	}
	
	public EntryPoint getEntryPoint(int i) {
		return entryPoints.get(i);
	}
	
	public Iterator<ExitPoint> getExitPoints() {
		return exitPoints.iterator();
	}
	
	public Iterator<EntryPoint> getEntryPoints() {
		return entryPoints.iterator();
	}
	
	public void saveEntryPoints(Vector<EntryPoint> entryPoints) {
		entryPoints.addAll(this.entryPoints);
	}
	
	public void saveExitPoints(Vector<ExitPoint> exitPoints) {
		exitPoints.addAll(this.exitPoints);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName() + ":");
		
		sb.append("\n\tEntry Points:");
		Iterator<EntryPoint> itr0 = entryPoints.iterator();
		while (itr0.hasNext()) {
			sb.append("\n\t\t" + itr0.next());
		}
		
		sb.append("\n\tExit Points:");
		Iterator<ExitPoint> itr1 = exitPoints.iterator();
		while (itr1.hasNext()) {
			sb.append("\n\t\t" + itr1.next());
		}
		
		return sb.toString();
	}
}
