package pharoslabut;

//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;
//import javax.imageio.ImageIO;


public class AStar {

	public static double getDistance(Node a, Node b){
		return Math.sqrt(Math.pow(a.getX()-b.getX(),2.0)+Math.pow(a.getY()-b.getY(),2.0));
	}
	
	public static void linkMap(Node[][] map){
		int row = map.length;
		int col = map[0].length;		//map[4][3] will have row = 4, col = 3
		Node up,down,left,right;
		int i = 0;
		int j = 0;
		for(i=0;i<row;i++){
			for(j=0;j<col;j++){
				if(j==0)	
					up = null;
				else		
					up = map[i][j-1];
				if(j==col-1)	
					down = null;
				else			
					down = map[i][j+1];
				if(i==0)	
					left = null;
				else		
					left = map[i-1][j];
				if(i==row-1)	
					right = null;
				else			
					right = map[i+1][j];
				map[i][j].setNeighbor(up,down,left,right);
			}
		}
		return;
	}
	
	public static int findIndex(double[] arr, double x){
		int i=0;
		boolean found = false;
		while(i<arr.length&&!found)
		{
			if(arr[i]==x)
				found = true;
			else
				i++;
		}
		return i;
	}
	

	public static Node findLowF(LinkedList list,LinkedListIterator itr, Node end){
		Node x = new Node();
		double temp = 9999;
		itr = list.first();
		while(itr.isValid()){
			if(temp>itr.retrieve().getGScore() + getDistance(itr.retrieve(),end))
			{
				x = itr.retrieve();
				temp = itr.retrieve().getGScore() + getDistance(itr.retrieve(),end);
			}
			itr.advance();
		}
		return x;
	}
	
	public static Node findMaxG(LinkedList list,LinkedListIterator itr){
		Node x = new Node();
		double temp = -1;
		itr = list.first();
		while(itr.isValid()){
			if(temp<itr.retrieve().getGScore()){
				x = itr.retrieve();
				temp = itr.retrieve().getGScore();
			}
			itr.advance();
		}
		return x;
	}
	
	public static LinkedList findPath(Node[][] map, Node start, Node goal){
		LinkedList closedset = new LinkedList();
		LinkedList openset = new LinkedList();
		LinkedList path = new LinkedList();
		LinkedListIterator openitr,closeditr,pathitr;
		openitr = openset.zeroth();
		closeditr = closedset.zeroth();
		pathitr = path.zeroth();
		double g_score = 0;
		double cost = 0;
		boolean reached = false;
		start.setGScore(g_score);
		openset.insert(start, openitr);
		path.insert(start,pathitr);
		Node cur = findLowF(openset,openitr,goal);
		while(!cur.equals(goal)&&!reached){
			if(openset.isEmpty()){
				return constructPath(path,start,start);
			}
			
			cur = findLowF(openset,openitr,goal);
			/*			
			System.out.println("Current Node selected");
			cur.getInfo();
			
			Debugging lines. Don't delete.
*/			
			openset.remove(cur);
			closedset.insert(cur,closeditr);
			for(int i=0;i<cur.getNeighbors().length;i++){
				if((cur.getNeighbors()[i]!=null))
					cost = cur.getGScore() + getDistance(cur,cur.getNeighbors()[i]);
				if((cur.getNeighbors()[i]!=null)&&cur.getNeighbors()[i].equals(goal)){
					reached = true;
					path.insert(cur.getNeighbors()[i],pathitr);
					cur.getNeighbors()[i].setGScore(cost);
					break;
				}
				if((cur.getNeighbors()[i]!=null)&&openset.contains(cur.getNeighbors()[i])&&cost<cur.getNeighbors()[i].getGScore())
					openset.remove(cur.getNeighbors()[i]);
				if((cur.getNeighbors()[i]!=null)&&closedset.contains(cur.getNeighbors()[i])&&cost<cur.getNeighbors()[i].getGScore())
					closedset.remove(cur.getNeighbors()[i]);
				if((cur.getNeighbors()[i]!=null)&&!openset.contains(cur.getNeighbors()[i])&&!closedset.contains(cur.getNeighbors()[i])){
					cur.getNeighbors()[i].setGScore(cost);
					openset.insert(cur.getNeighbors()[i],openitr);
					path.insert(cur.getNeighbors()[i],pathitr);
				}
			}
		}
		System.out.println("REACHED DESTINATION");
		return constructPath(path,start,goal);
	}
	
	
	public static LinkedList constructPath(LinkedList p,Node start,Node end){
		System.out.println("RECONSTRUCTING PATH");
		LinkedList path = new LinkedList();
		LinkedListIterator pathitr,pitr;
		pathitr = path.zeroth();
		pitr = p.zeroth();
		Node cur = end;
		Node temp_cur = cur;
		path.insert(cur,pathitr);
		int i = 0;
		while(!cur.equals(start)&&i<cur.getNeighbors().length){
			if((cur.getNeighbors()[i]!=null)&&p.contains(cur.getNeighbors()[i])&&cur.getNeighbors()[i].getGScore()<temp_cur.getGScore()){
				temp_cur = cur.getNeighbors()[i];
				i++;
			}
			else
				i++;
			if(i==cur.getNeighbors().length){
				cur = temp_cur;
				path.insert(cur,pathitr);
				i = 0;
			}
		}
		return path;
	}
	
	public static List<Double> move_instruction(LinkedList path, Node start){
	
		/****************************************	
		 * 	Lookup Table for move_instruction	*
		 * 	0  - No turn						*
		 * 	1  - Turn right 45					*
		 *  2  - Turn right 90					*
		 *  3  - Turn right 135					*
		 * 	4  - Turn right 180					*
		 * 	-1 - Turn left 45					*
		 *  -2 - Turn left 90					*
		 *  -3 - Turn left 135					*
		 *  10 - Move forward					*
		 ****************************************/
		LinkedListIterator pathitr, pathitr_pre;
		List<Integer> heading = new ArrayList<Integer>();
		List<Double> command = new ArrayList<Double>();
		double x,y;
		double turn_code = 0;
		double move_dis = 1.0;
		pathitr_pre = path.first();
		pathitr = path.first();
		pathitr.advance();
		heading.add(0);
		for(;pathitr.isValid();pathitr.advance()){
			x = pathitr.retrieve().getX() - pathitr_pre.retrieve().getX();
			y = pathitr.retrieve().getY() - pathitr_pre.retrieve().getY();
			if(x==1.0&&y==0.0)
				heading.add(0);
			if(x==1.0&&y==1.0)
				heading.add(315);
			if(x==0.0&&y==1.0)
				heading.add(270);
			if(x==-1.0&&y==1.0)
				heading.add(225);
			if(x==-1.0&&y==0.0)
				heading.add(180);
			if(x==-1.0&&y==-1.0)
				heading.add(135);
			if(x==0.0&&y==-1.0)
				heading.add(90);
			if(x==1.0&&y==-1.0)
				heading.add(45);
			pathitr_pre.advance();
		}
		System.out.println("Heading");
		System.out.println(heading);		
		
		for(int i=0;i<heading.size()-1;i++){
			turn_code = (heading.get(i)-heading.get(i+1))/45;
			if(turn_code>4)
				turn_code = turn_code - 8;
			else if(turn_code<=-4)
				turn_code = turn_code + 8;
			if(heading.get(i+1)%2!=0)
				move_dis = 1.4;
			else
				move_dis = 1.0;
			command.add(turn_code);
			command.add(10.0*move_dis);
		}
		
		return command;
	}
}

