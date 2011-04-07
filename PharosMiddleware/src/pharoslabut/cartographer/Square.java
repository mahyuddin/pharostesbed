package pharoslabut.cartographer;

import java.util.HashSet;
import java.util.Set;

public class Square {
	
	private int x;
	private int y;
	private boolean start;
	private boolean end;

	private double localCost; // cost of getting from this square to goal
	private double parentCost; // cost of getting from parent square to this node
	private double passThroughCost;// cost of getting from the start to the goal
	// through this square
	
	private MapSector mapSector;
	private Set<Square> adjacencies = new HashSet<Square>();
	private boolean quadrantTop = true;	// top quadrant; true means traversable
	private boolean quadrantLft = true; // left quadrant
	private boolean quadrantBot = true;	// bottom quadrant
	private boolean quadrantRgt = true; // right quadrant
	private int numBlocked = 0;
	
	private Square parent;
	private Square diagonalSquare;
	
	public Square(int x, int y, MapSector mapSector) {

		this.x = x;
		this.y = y;
		this.mapSector = mapSector;
		
		// testing
		/*if (Math.random() > .9) {
			quadrantTop = false;
			numBlocked++;
		}
		if (Math.random() > .9) {
			quadrantLft = false;
			numBlocked++;
		}
		if (Math.random() > .9) {
			quadrantBot = false;
			numBlocked++;
		}
		if (Math.random() > .9) {
			quadrantRgt = false;
			numBlocked++;
		}*/
	}

	public int getX() {

		return x;
	}

	public void setX(int x) {

		this.x = x;
	}

	public int getY() {

		return y;
	}

	public void setY(int y) {

		this.y = y;
	}

	public boolean isStart() {

		return start;
	}

	public void setStart(boolean start) {

		this.start = start;
	}

	public boolean isEnd() {

		return end;
	}

	public void setEnd(boolean end) {

		this.end = end;
	}

	public Set<Square> getAdjacencies() {

		return adjacencies;
	}

	public void setAdjacencies(Set<Square> adjacencies) {

		this.adjacencies = adjacencies;
	}

	public Square getParent() {

		return parent;
	}

	public void setParent(Square parent) {

		this.parent = parent;
	}

	public void isDiagonalTo(Square diagonalSquare){
		diagonalSquare = this.diagonalSquare;
	}
	
	public boolean ifDiagonalTo(Square thisSquare){
		if (this.diagonalSquare == thisSquare)
			return true;
		return false;
	}
	
	public void calculateAdjacencies() {
		int top = x - 1;
		int bottom = x + 1;
		int left = y - 1;
		int right = y + 1;

		/*if (bottom < mapSector.getRows()) {
			if (isAdjacent()) {
				mapSector.getSquare(bottom, y).addAdjacency(this);
				this.addAdjacency(mapSector.getSquare(bottom, y));
			}
		}

		if (right < mapSector.getColumns()) {
			if (isAdjacent()) {
				mapSector.getSquare(x, right).addAdjacency(this);
				this.addAdjacency(mapSector.getSquare(x, right));
			}
		}
		
		if( right < mapSector.getColumns() && top > 0 ) {
			boolean topRightFromTop = (mapSector.getSquare(top, y)).adjacencies.contains(mapSector.getSquare(top, right));
			boolean topRightFromRight = (mapSector.getSquare(x, right)).adjacencies.contains(mapSector.getSquare(top, right));
			boolean topAvail = (mapSector.getSquare(top, y)).adjacencies.contains(mapSector.getSquare(x, y));
			boolean rightAvail = (mapSector.getSquare(x, right)).adjacencies.contains(mapSector.getSquare(x, y));;
			if( topRightFromTop && topRightFromRight && topAvail && topAvail && rightAvail ){
				mapSector.getSquare(top, right).isDiagonalTo(this);
				mapSector.getSquare(top, right).addAdjacency(this);
				this.addAdjacency(mapSector.getSquare(top, right));
			}
		}
		
		if( left > 0 && top > 0 ) {
			boolean topLeftFromTop = (mapSector.getSquare(top, y)).adjacencies.contains(mapSector.getSquare(top, left));
			boolean topLeftFromLeft = (mapSector.getSquare(x, left)).adjacencies.contains(mapSector.getSquare(top, left));
			boolean topAvail = (mapSector.getSquare(top, y)).adjacencies.contains(mapSector.getSquare(x, y));
			boolean leftAvail = (mapSector.getSquare(x, left)).adjacencies.contains(mapSector.getSquare(x, y));;
			if( topLeftFromTop && topLeftFromLeft && topAvail && topAvail && leftAvail ){
				mapSector.getSquare(top, left).isDiagonalTo(this);
				mapSector.getSquare(top, left).addAdjacency(this);
				this.addAdjacency(mapSector.getSquare(top, left));
			}
		}*/
		
		if (bottom < mapSector.getRows()) {
			if (mapSector.getSquare(bottom, y).isTraversable() && this.isTraversable()) {
				mapSector.getSquare(bottom, y).addAdjacency(this);
				this.addAdjacency(mapSector.getSquare(bottom, y));
			}
		}

		if (right < mapSector.getColumns()) {
			if (mapSector.getSquare(x, right).isTraversable() && this.isTraversable()) {
				mapSector.getSquare(x, right).addAdjacency(this);
				this.addAdjacency(mapSector.getSquare(x, right));
			}
		}
		
		if( right < mapSector.getColumns() && top > 0 ) {	// traverse diagonally to top right
			boolean topRightOK = mapSector.getSquare(top, right).isTraversable();
			boolean topAvail = (mapSector.getSquare(top, y)).getQBot() && (mapSector.getSquare(top, y)).getQRgt();
			boolean rightAvail = (mapSector.getSquare(top, y)).getQLft() && (mapSector.getSquare(top, y)).getQTop();
			if( isTraversable() && topRightOK && topAvail && rightAvail ){
				mapSector.getSquare(top, right).isDiagonalTo(this);
				mapSector.getSquare(top, right).addAdjacency(this);
				this.addAdjacency(mapSector.getSquare(top, right));
			}
		}
		
		if( left > 0 && top > 0 ) {	// traverse diagonally to top left
			boolean topLeftOK = mapSector.getSquare(top, left).isTraversable();
			boolean topAvail = (mapSector.getSquare(top, y)).getQBot() && (mapSector.getSquare(top, y)).getQLft();
			boolean leftAvail = (mapSector.getSquare(top, y)).getQTop() && (mapSector.getSquare(top, y)).getQRgt();;
			if( isTraversable() && topLeftOK && topAvail && leftAvail ){
				mapSector.getSquare(top, left).isDiagonalTo(this);
				mapSector.getSquare(top, left).addAdjacency(this);
				this.addAdjacency(mapSector.getSquare(top, left));
			}
		}
	}

	public void addAdjacency(Square square) {

		adjacencies.add(square);
	}
	
	public void removeAdjacency(Square square) {
		adjacencies.remove(square);
	}

	public double getPassThrough(Square goal) {

		if (isStart()) {
			return 0.0;
		}

		return getLocalCost(goal) + getParentCost();
	}

	public double getLocalCost(Square goal) {

		if (isStart()) {
			return 0.0;
		}

		localCost = 1.0 * (Math.abs(x - goal.getX()) + Math.abs(y - goal.getY()));
		return localCost;
	}

	public double getParentCost() {

		if (isStart()) {
			return 0.0;
		}

		//if(parentCost == 0.0) {
			parentCost = (this.ifDiagonalTo(this.getParent())==true ? 1.4 : 1.0) + + .5 * (parent.getParentCost() - 1.0); 
		//}

		return parentCost;
	}
	
	public boolean isAdjacent() {

		if (Math.random() > .3) {
			return true;
		}
		return false;
	}
	
	/* getters */
	public boolean isFullyAdjacent(Square neighbor){
		return neighbor.getQTop() && neighbor.getQBot() && neighbor.getQRgt() && neighbor.getQLft();
	}
	public boolean isTraversable(){	// is traversable if all 4 quadrants are true (traversable)
		return getQTop() && getQBot() && getQRgt() && getQLft();
	}
	public boolean getQTop(){
		return quadrantTop;
	}
	public boolean getQBot(){
		return quadrantBot;
	}
	public boolean getQRgt(){
		return quadrantRgt;
	}
	public boolean getQLft(){
		return quadrantLft;
	}
	/* setters */
	public void setQTop(boolean truth){
		quadrantTop = truth;
	}
	public void setQBot(boolean truth){
		quadrantBot = truth;
	}
	public void setQRgt(boolean truth){
		quadrantRgt = truth;
	}
	public void setQLft(boolean truth){
		quadrantLft = truth;
	}

}
