package pharoslabut.cartographer;
import java.util.*;

// may not need this.. we could bypass this and just use MapSector
public class Astar {

	//public Astar(){
	//public static void main(String[] args) {
	public List<Square> pathFind(){
		int side = 9;
		int radius = side/2;
		int direction = -1;
		OrderedPair start;
		//start = new OrderedPair(radius,radius);	// middle of the sector
		start = new OrderedPair(0,0);	// start at the bottom left corner
		
		System.out.println("entered sector");
		switch(direction){
		case 0:
			start.y+=32;
			break;
		case 1:
			start.y-=32;
			break;
		case 2:
			start.x-=32;
			break;
		case 3:
			start.x+=32;
			break;
		default:
			break;
		}
		MapSector sector = new MapSector(side,side,start, radius);
		direction = sector.findPath();	// this exits as soon as it finds a path
		System.out.println(sector.bestList.size());
		return sector.bestList;
	}
	
}
