package pharoslabut;
public class Node{
	private double xCoord, yCoord;
	private int path; 
	private Node up, down, left, right, upright, upleft, bottomright, bottomleft;
	private double g_score;					// the distance from the starting node to the current node
	
	public Node(){
		xCoord = 0;
		yCoord = 0;
		path = 0;
		up = null;
		down = null;
		left = null;
		right = null;
		upright = null;
		upleft = null;
		bottomright = null;
		bottomleft = null;
		g_score = 0;
	}
	
	public Node(double x, double y, Node u, Node d, Node l, Node r, Node ur, Node ul, Node br, Node bl, int p){
		xCoord = x;
		yCoord = y;
		up = u;
		down= d;
		left = l;
		right = r;
		upright = ur;
		upleft = ul;
		bottomright = br;
		bottomleft = bl;
		path = p;
	}
	
	public void setObstacle(int p){
		path = p;
	}

	public void setCoordinate(double x, double y){
		xCoord = x;
		yCoord = y;
	}
	
	public void setGScore(double score){
		g_score = score; 
	}

	public void setCorner(Node ur, Node ul, Node br, Node bl){
		upright = ur;
		upleft = ul;
		bottomright = br;
		bottomleft = bl;
	}
	
	public void setNeighbor(Node u, Node d, Node l, Node r){
		up = u;
		down= d;
		left = l;
		right = r;
	}
		
	public void getInfo(){
		System.out.println("Coordinate: " + xCoord + "," + yCoord + " Path: " + path + " g_score: " + g_score);
	}
	
	public double getX(){
		return xCoord;
	}
	
	public double getY(){
		return yCoord;
	}
	
	public int getPath(){
		return path;
	}
	
	public Node getUp(){
		return up;
	}
	
	public Node getDown(){
		return down;
	}
	
	public Node getLeft(){
		return left;
	}
	
	public Node getRight(){
		return right;
	}
	
	public Node getUpright(){
			return upright;
	}
		
	public Node getUpleft(){
			return upleft;
	}
		
	public Node getBottomright(){
			return bottomright;
	}
		
	public Node getBottomleft(){
			return bottomleft;
	}
	
	
	public void printNeighbors(){
		if(this.up!=null)
			System.out.println("Up: " + this.up.getX() + "," + this.up.getY());
		else
			System.out.println("Up: NULL,NULL");
		if(this.upleft!=null)
			System.out.println("Upleft: " + this.upleft.getX() + "," + this.upleft.getY());
		else
			System.out.println("Upleft: NULL,NULL");
		if(this.left!=null)
			System.out.println("Left: " + this.left.getX() + "," + this.left.getY());
		else
			System.out.println("Left: NULL,NULL");
		if(this.bottomleft!=null)
			System.out.println("Bottomleft: " + this.bottomleft.getX() + "," + this.bottomleft.getY());
		else
			System.out.println("Bottomleft: NULL,NULL");	
		if(this.down!=null)
			System.out.println("Down: " + this.down.getX() + "," + this.down.getY());
		else
			System.out.println("Down: NULL,NULL");
		if(this.bottomright!=null)
			System.out.println("Bottomright: " + this.bottomright.getX() + "," + this.bottomright.getY());
		else
			System.out.println("Bottomright: NULL,NULL");	
		if(this.right!=null)
			System.out.println("Right: " + this.right.getX() + "," + this.right.getY());
		else
			System.out.println("Right: NULL,NULL");
		if(this.upright!=null)
			System.out.println("Upright: " + this.upright.getX() + "," + this.upright.getY());
		else
			System.out.println("Upright: NULL,NULL");
		return;
	}
	
	public Node[] getNeighbors(){
		Node[] neighbors = {null,null,null,null,null,null,null,null};
		if(this.up!=null&&this.up.path==1)
			neighbors[0] = this.up;
		if(this.upleft!=null&&this.up.path==1&&this.left.path==1&&this.upleft.path==1)
			neighbors[1] = this.upleft;
		if(this.left!=null&&this.left.path==1)
			neighbors[2] = this.left;
		if(this.bottomleft!=null&&this.left.path==1&&this.down.path==1&&this.bottomleft.path==1)
			neighbors[3] = this.bottomleft;
		if(this.down!=null&&this.down.path==1)
			neighbors[4] = this.down;
		if(this.bottomright!=null&&this.down.path==1&&this.right.path==1&&this.bottomright.path==1)
			neighbors[5] = this.bottomright;
		if(this.right!=null&&this.right.path==1)
			neighbors[6] = this.right;
		if(this.upright!=null&&this.right.path==1&&this.up.path==1&&this.upright.path==1)
			neighbors[7] = this.upright;
		return neighbors;
	}
	
	public double getGScore(){
		return g_score;
	}
}


