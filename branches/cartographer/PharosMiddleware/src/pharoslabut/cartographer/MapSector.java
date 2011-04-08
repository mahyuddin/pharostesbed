package pharoslabut.cartographer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/* Map Sector Class
 * This class implements a certain region of the WorldView Map
 * This reads the WorldView Map and converts a specific region of that map
 * into a Map Sector.
 */
public class MapSector {
	public int NORTH = 0;
	public int SOUTH = 1;
	public int WEST = 2;
	public int EAST = 3;
	
	public boolean found;
	private int rows;
	private int columns;
	private OrderedPair startCoord;	// every map sector has a starting coordinate (as close to midpoint as possible)
	private OrderedPair goalCoord;	// every map also has a goal (the goal should be anywhere on the perimeter)
	List<OrderedPair> listGoals = new ArrayList<OrderedPair>();
	private Square[][] elements;
	private Square start;
	private Square goal;
	
	// for testing, and obstacle simulation
	private static final String CLOSED_TOP = "+ - ";
	private static final String OPEN_TOP = "+   ";
	private static final String CLOSED_LEFT = "|   ";
	private static final String CLOSED_LEFT_PATH = "| . ";
	private static final String CLOSED_LEFT_START = "| S ";
	private static final String CLOSED_LEFT_GOAL = "| G ";
	private static final String OPEN_LEFT = "    ";
	private static final String OPEN_LEFT_PATH = "  . ";
	private static final String OPEN_LEFT_START = "  S ";
	private static final String OPEN_LEFT_GOAL = "  G ";

	private List<Square> opened = new ArrayList<Square>();
	private List<Square> closed = new ArrayList<Square>();
	public List<Square> bestList = new ArrayList<Square>();

	/**
	 * Constructor
	 */
	public MapSector(int rows, int columns, OrderedPair mapSecCorner, int radius) {

		this.rows = rows;
		this.columns = columns;
		elements = new Square[rows][columns];
		//this.startCoord = startCoord;
		this.startCoord = new OrderedPair(rows/2, rows/2);
		//double []currLoc = LocationTracker.getCurrentLocation();
		//this.startCoord = new OrderedPair((int)(currLoc[0]/8), (int)(currLoc[1]/8));
		// right now, just four possible places
		OrderedPair end;
		//end = new OrderedPair(startCoord.getX()+radius,startCoord.getY());
		end = new OrderedPair(startCoord.getX(),startCoord.getY()-radius);
		listGoals.add(end);
		//end = new OrderedPair(startCoord.getX()-radius,startCoord.getY());
		end = new OrderedPair(startCoord.getX()+radius,startCoord.getY());
		listGoals.add(end);
		end = new OrderedPair(startCoord.getX(),startCoord.getY()+radius);
		listGoals.add(end);
		//end = new OrderedPair(startCoord.getX(),startCoord.getY()-radius);
		end = new OrderedPair(startCoord.getX()-radius,startCoord.getY());
		listGoals.add(end);
		//sectorConvert(mapSecCorner); // convert from Worldview to Mapsector format
	}
	
	public int findPath(){
		
		int i = 0;
		int direction = -1;
		init();
		while(found==false && i != listGoals.size()){
			goalCoord = listGoals.get(i);
			setStartAndGoal();
			draw();
			direction = findBestPath();
			if(found==false){
				System.out.println("--Move on to next possible goal point");
				goal.setEnd(false);
				opened.clear();
				closed.clear();
				bestList.clear();
				i++;
			}
		}
		return direction;
	}
	
	private class quadData{
		int numBlockedTop;
		int numBlockedBot;
		int numBlockedRgt;
		int numBlockedLft;
		void quadData(){
			this.numBlockedTop = 0;
			this.numBlockedBot = 0;
			this.numBlockedLft = 0;
			this.numBlockedRgt = 0;
		}
	}
	
	/* This code will convert the WorldView section into a 2D array (not arraylist) that 
	 * is black and white, and easy for the Astar algorithm to read, with each sector of 
	 * area = side^2. the var side is defined above in this class
	 * This already knows the size of the entire map
	 */
	private void sectorConvert(OrderedPair corner){
		// convert world view sector into map sector
		int xMS, yMS, x, y = 0;
		//for(xMS = 0; xMS<rows; xMS++){	// for every element in the mapsector
			//for(yMS = 0; yMS<columns; yMS++){
		for(xMS = rows-1; xMS>=0; xMS--){	// for every element in the mapsector
			for(yMS = columns-1; yMS>=0; yMS--){
				// inside every element of the mapsector we have 64 elements
				quadData square = new quadData();
				for(x = 0; x<8; x++){	// for every worldview element in the mapsector element
					for(y = 0; y<8; y++){
						//if( x!=y && x+y!=7 && WorldView.readSampleConfidence(xMS*8 + x + corner.x, yMS*8 + y + corner.y) > .5){
						if( WorldView.readConfidence(xMS*8 + x + corner.x, yMS*8 + y + corner.y) > .5){
							//if( x!=y && x+y!=7 && WorldView.readConfidence(x + corner.x, y + corner.y) > .5){
							// if not at a loc coord that is on the X that splits the Mapsector element into quads
							// and if the Worldview is pretty confident that there's an obstruction at that 5cmx5cm square
							if(y>=x && y<=7-x)
								square.numBlockedLft++;
							if(y>=x && y>=7-x)
								square.numBlockedTop++;
							if(y<=x && y<=7-x)
								square.numBlockedBot++;
							if(y<=x && y>=7-x)
								square.numBlockedRgt++;
						}
						//System.out.println("sample confidence for " + (xMS*8+x) + ", " + (8*yMS+y) + ": "+ WorldView.readSampleConfidence(x*xMS + corner.x, y*yMS + corner.y));
					}
				}
				// decide whether each of the 4 quad is traversable
				elements[xMS][yMS] = new Square(xMS, yMS, this);
				if(square.numBlockedLft > 0){//> 12/3){	// if more than 1/3 of the 12 blocks inside each quad is blocked
					elements[xMS][yMS].setQLft(false);
					System.out.println("blocked lft = " + square.numBlockedLft);
				}
				if(square.numBlockedRgt > 0){//> 12/3){
					elements[xMS][yMS].setQRgt(false);
					System.out.println("blocked rght = " + square.numBlockedRgt);
				}
				if(square.numBlockedTop > 0){//> 12/3){
					elements[xMS][yMS].setQTop(false);
					System.out.println("blocked top = " + square.numBlockedTop);
				}
				if(square.numBlockedBot > 0){//> 12/3){
					elements[xMS][yMS].setQBot(false);
					System.out.println("blocked bot = " + square.numBlockedBot);
				}
				
			}

		}
		System.out.println("done, " + rows*8 + " by " + columns*8);
	}

	private void init() {
		
		//createSquares();
		OrderedPair testInit = new OrderedPair(0, 0);
		sectorConvert(testInit);
		generateAdjacencies();
	}

	public int getRows() {
		
		return rows;
	}

	public int getColumns() {
		
		return columns;
	}

	private void setStartAndGoal() {
		
		elements[startCoord.x][startCoord.y].setStart(true);
		start = elements[startCoord.x][startCoord.y];
		goal = elements[goalCoord.x][goalCoord.y];
		goal.setEnd(true);
	}

	private void generateAdjacencies() {

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j].calculateAdjacencies();
			}
		}
	}

	private void createSquares() {

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j] = new Square(i, j, this);
			}
		}
	}

	public Square getSquare(int x, int y) {

		return elements[x][y];
	}

	public void setSquare(Square square) {

		elements[square.getX()][square.getY()] = square;
	}

	public void draw() {

		System.out.println("Drawing maze");
		drawContents();
		drawBorder();
	}

	private void drawContents() {

		for (int i = rows-1; i >= 0; i--) {
			for (int j = 0; j < columns; j++) {
				Square square = elements[i][j];
				drawTop(square);
			}
			System.out.println("+");

			for (int j = 0; j < columns; j++) {
				Square square = elements[i][j];
				drawLeft(square);
			}
			System.out.println("|");
		}
	}

	private void drawLeft(Square square) {

		int x = square.getX();
		int y = square.getY();

		if (y - 1 < 0) {
			if (square.isStart()) {
				System.out.print(CLOSED_LEFT_START);
				return;
			}

			if (square.isEnd()) {
				System.out.print(CLOSED_LEFT_GOAL);
				return;
			}

			if (bestList.contains(square)) {
				System.out.print(CLOSED_LEFT_PATH);
				return;
			}
			System.out.print(CLOSED_LEFT);
			return;
		}

		for (Square neighbor : square.getAdjacencies()) {
			if (neighbor.getX() == x && neighbor.getY() == y - 1) {
				if (square.isEnd()) {
					System.out.print(OPEN_LEFT_GOAL);
					return;
				}
				if (square.isStart()) {
					System.out.print(OPEN_LEFT_START);
					return;
				}
				if (bestList.contains(square)) {
					System.out.print(OPEN_LEFT_PATH);
					return;
				}
				System.out.print(OPEN_LEFT);
				return;
			}
		}

		if (square.isEnd()) {
			System.out.print(CLOSED_LEFT_GOAL);
			return;
		}
		
		if (square.isStart()) {
			System.out.print(CLOSED_LEFT_START);
			return;
		}

		if (bestList.contains(square)) {
			System.out.print(CLOSED_LEFT_PATH);
			return;
		}
		System.out.print(CLOSED_LEFT);

	}

	private void drawTop(Square square) {

		int x = square.getX();
		int y = square.getY();

		//if (x == 0) {
		if (x == rows-1){
			System.out.print(CLOSED_TOP);
			return;
		}

		for (Square neighbor : square.getAdjacencies()) {
		//	if (neighbor.getX() == x - 1 && neighbor.getY() == y) {
			if (neighbor.getX() == x + 1 && neighbor.getY() == y) {
				System.out.print(OPEN_TOP);
				return;
			}
		}

		System.out.print(CLOSED_TOP);
	}

	private void drawBorder() {

		for (int i = 0; i < columns; i++) {
			System.out.print(CLOSED_TOP);
		}
		System.out.println("+");
	}
	
	// returns the cardinal direction it chose to go to
	public int findBestPath() {

		System.out.println("Calculating best path...");
		Set<Square> adjacencies = elements[startCoord.x][startCoord.y].getAdjacencies();
		//closed.add(start);
		for (Square adjacency : adjacencies) {
			adjacency.setParent(elements[startCoord.x][startCoord.y]);
			if (adjacency.isStart() == false) {
				opened.add(adjacency);
			}
		}

		while (opened.size() > 0) {
			Square best = findBestPassThrough();
			opened.remove(best);
			closed.add(best);
			if (best.isEnd()) {
				found = true;
				System.out.println("Found Goal");
				populateBestList(goal);	
				draw();
				if(best.getY()>start.getY()) return NORTH;
				if(best.getY()<start.getY()) return SOUTH;
				if(best.getX()>start.getX()) return EAST;
				if(best.getX()<start.getX()) return WEST;
			} else {
				Set<Square> neighbors = best.getAdjacencies();
				for (Square neighbor : neighbors) {
					if (opened.contains(neighbor)) {
						Square tmpSquare = new Square(neighbor.getX(),
								neighbor.getY(), this);
						tmpSquare.setParent(best);
						if (tmpSquare.getPassThrough(goal) >= neighbor
								.getPassThrough(goal)) {
							continue;
						}
					}

					if (closed.contains(neighbor)) {
						Square tmpSquare = new Square(neighbor.getX(),
								neighbor.getY(), this);
						tmpSquare.setParent(best);
						if (tmpSquare.getPassThrough(goal) >= neighbor
								.getPassThrough(goal)) {
							continue;
						}
					}
					
					
					neighbor.setParent(best);

					opened.remove(neighbor);
					closed.remove(neighbor);
					opened.add(0, neighbor);
				}
			}
		}
		found = false;
		System.out.println("No Path to goal");
		return -1;
	}

	private void populateBestList(Square square) {

		bestList.add(square);
		if (square.getParent().isStart() == false) {
			populateBestList(square.getParent());
		}
		else{
			bestList.add(square.getParent());
		}
		return;
	}

	private Square findBestPassThrough() {

		Square best = null;
		for (Square square : opened) {
			if (best == null
					|| square.getPassThrough(goal) < best.getPassThrough(goal)) {
				best = square;
			}
		}
		return best;
	}

}
