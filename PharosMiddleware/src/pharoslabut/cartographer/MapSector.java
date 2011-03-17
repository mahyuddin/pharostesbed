package pharoslabut.cartographer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MapSector {
	
	public boolean found;
	private int rows;
	private int columns;
	private OrderedPair startCoord;	// every map sector has a starting coordinate (as close to midpoint as possible)
	private OrderedPair goalCoord;	// every map also has a goal (the goal should be anywhere on the perimeter)
	List<OrderedPair> listGoals = new ArrayList<OrderedPair>();
	private Square[][] elements;
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
	public MapSector(int rows, int columns, OrderedPair startCoord, List goalPoints) {

		this.rows = rows;
		this.columns = columns;
		elements = new Square[rows][columns];
		this.startCoord = startCoord;
		this.listGoals = goalPoints;
		//sectorConvert(); // convert from Worldview to Mapsector format
	}
	
	public void findPath(){
		
		int i = 0;
		init();
		while(found==false && i != listGoals.size()){
			goalCoord = listGoals.get(i);
			setStartAndGoal();
			draw();
			findBestPath();
			if(found==false){
				System.out.println("--Move on to next possible goal point");
				goal.setEnd(false);
				opened.clear();
				closed.clear();
				bestList.clear();
				i++;
			}
		}
	}
	
	/* This code will convert the WorldView section into a 2D array (not arraylist) that 
	 * is black and white, and easy for the Astar algorithm to read, with each sector of 
	 * area = side^2. the var side is defined above in this class
	 * 
	 * This may not be needed, we can increase the complexity of the astar algorithm and 
	 * not necesarily have to recreate a new map just for this purpose. Warning: this may
	 * put stress on the CPU. 
	 */
	private void sectorConvert(){
		// convert world view sector into map sector
		
	}

	private void init() {
		
		createSquares();
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

		for (int i = 0; i < rows; i++) {
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

		if (bestList.contains(square)) {
			System.out.print(CLOSED_LEFT_PATH);
			return;
		}
		System.out.print(CLOSED_LEFT);

	}

	private void drawTop(Square square) {

		int x = square.getX();
		int y = square.getY();

		if (x == 0) {
			System.out.print(CLOSED_TOP);
			return;
		}

		for (Square neighbor : square.getAdjacencies()) {
			if (neighbor.getX() == x - 1 && neighbor.getY() == y) {
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

	public void findBestPath() {

		System.out.println("Calculating best path...");
		Set<Square> adjacencies = elements[startCoord.x][startCoord.y].getAdjacencies();
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
				return;
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
	}

	private void populateBestList(Square square) {

		bestList.add(square);
		if (square.getParent().isStart() == false) {
			populateBestList(square.getParent());
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
