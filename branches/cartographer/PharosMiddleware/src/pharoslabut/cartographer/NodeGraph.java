package pharoslabut.cartographer;

import java.util.LinkedList;

/**
 * @author Alex Aur
 * NodeGraph contains the data structure for the highest level of navigation. Each node in this graph represents a MapSector.
 */
public class NodeGraph {
	//0 - North
	//1 - South
	//2 - West
	//3 - East
	public final int NUM_NEIGHBORS = 4;
	public final int WHITE = 0; // Unexplored
	public final int BLACK = 1; // Traversed
	public final int RED = 2; // Obstacle
	
	NodeInfo nodeGraph [][];  //3-D array. First two dimensions index into node. 3rd Dimension is for neighbors
	int rows;
	int cols;
	int numWhite = 0;
	
	/**
	 * Constructor. Also initializes the nodeGraph
	 */
	public NodeGraph(int rows, int cols){
		nodeGraph = new NodeInfo[rows][cols]; //create nodeGraph with rows*cols elements, each with 4 neighbors
		this.rows = rows;
		this.cols = cols;
		initializeGraph();
	}
	
	/**
	 * Sets the adjacencies at the coordinate (xCoord, yCoord)
	 * The input order is [North, South, West, East] (all Ordered Pair Objects)
	 * @param xCoord
	 * @param yCoord
	 * @param directions An OrderedPair array that lists valid neighbors
	 */
	public void setNeighbors(int xCoord, int yCoord, OrderedPair [] directions){
		nodeGraph[xCoord][yCoord].setNeighbors(directions);
	}
	
	/**
	 * Sets the color of the node located at (xCoord, yCoord)
	 * @param xCoord
	 * @param yCoord
	 * @param newColor Black, White, or Red.
	 */
	public void setColor(int xCoord, int yCoord, int newColor){
		nodeGraph[xCoord][yCoord].setColor(newColor);
	}
	
	/**
	 * Helper function to initialize nodes and adjacencies. 
	 * Initializes all node colors to white (aka unexplored terrain)
	 * (0,0) is in the lower left and (rows,cols) is in the upper right.
	 */
	private void initializeGraph(){
		for(int r = 0; r < rows; r++){
			for(int c = 0; c < cols; c++){
				OrderedPair [] directions = new OrderedPair[4];
				//Check north neighbor
				if(r == rows - 1) //no North neighbor for top elements
					directions[0] = null;
				else
					directions[0] = new OrderedPair(r+1,c);
				
				//Check South neighbor
				if(r == 0) //no South neighbor for bottom elements
					directions[1] = null;
				else
					directions[1] = new OrderedPair(r-1,c);
				
				//Check West neighbor
				if(c == 0) //no West neighbor for left elements
					directions[2] = null;
				else
					directions[2] = new OrderedPair(r,c-1);
				
				//Check East neighbor
				if(c == cols - 1) // no East neighbor for right elements
					directions[3] = null;
				else
					directions[3] = new OrderedPair(r,c+1);
				
				NodeInfo info = new NodeInfo(directions,WHITE);
				numWhite++;
				nodeGraph[r][c] = info;
			}
		}
	}
	
	/**
	 * Checks if the map is done. If there are no white nodes left, then all spaces have been traversed.
	 * If there are white spaces left, we need to go to them still.
	 * @return
	 */
	public boolean graphComplete(){
		return (numWhite == 0);
	}
	
	/**
	 * @author Alex Aur
	 * NodeInfo Class contains information for each node in the NodeGraph.
	 * This information includes the list of neighbors and the current color of the node.
	 */
	public class NodeInfo{
		OrderedPair [] neighbors;
		int color;
		
		/**
		 * Constructor for NodeInfo
		 * @param neighbors
		 * @param color
		 */
		public NodeInfo(OrderedPair [] neighbors, int color){
			this.neighbors = neighbors;
			this.color = color;
		}
		
		/**
		 * Returns color
		 * @return
		 */
		public int getColor(){
			return color;
		}
		
		/**
		 * Sets color
		 * @param color
		 */
		public void setColor(int color){
			this.color = color;
		}
		
		/**
		 * Returns neighbor array
		 * @return
		 */
		public OrderedPair [] getNeighbors(){
			return neighbors;
		}
		
		/**
		 * Sets the neighbors list
		 * @param neighbors
		 */
		public void setNeighbors(OrderedPair[] neighbors){
			this.neighbors = neighbors;			
		}
		
	}
}

