package pharoslabut;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;


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
				System.out.println("NO PATH EXISTS");
				return constructPath(path,start,start);
			}
			cur = findLowF(openset,openitr,goal);
			/*			
			System.out.println("Current Node selected");
			cur.getInfo();
			openset.printList(openset);
						
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
		LinkedListIterator pathitr;
		pathitr = path.zeroth();
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
	
	public static List<Integer> get_heading(LinkedList path, Node start){
		LinkedListIterator pathitr, pathitr_pre;
		List<Integer> heading = new ArrayList<Integer>();
		double x,y;
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
		return heading;
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

		List<Integer> heading = get_heading(path, start);
		List<Double> command = new ArrayList<Double>();
		double turn_code = 0;
		double move_dis = 1.0;
	
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
	
	public static List<List<Double>> ir_helper(LinkedList path, List<Integer> heading){
		List<List<Double>> ir_range = new ArrayList<List<Double>>();
		LinkedListIterator pathitr;
		pathitr = path.first();
		for(int i=0;i<heading.size();i++)
		{
			List<Double> obs = nearest_obs(pathitr.retrieve());
			switch(heading.get(i)){
				case 0:		ir_range.add(Arrays.asList(obs.get(0),obs.get(6),obs.get(4)));	break;
				case 45:	ir_range.add(Arrays.asList(obs.get(1),obs.get(7),obs.get(5)));	break;
				case 90:	ir_range.add(Arrays.asList(obs.get(2),obs.get(0),obs.get(6)));	break;
				case 135:	ir_range.add(Arrays.asList(obs.get(3),obs.get(1),obs.get(7)));	break;
				case 180:	ir_range.add(Arrays.asList(obs.get(4),obs.get(2),obs.get(0)));	break;
				case 225:	ir_range.add(Arrays.asList(obs.get(5),obs.get(3),obs.get(1)));	break;
				case 270:	ir_range.add(Arrays.asList(obs.get(6),obs.get(4),obs.get(2)));	break;
				case 315:	ir_range.add(Arrays.asList(obs.get(7),obs.get(5),obs.get(3)));	break;
			}
			pathitr.advance();
		}
		System.out.println("Nearest Obstacle");
		System.out.println(ir_range);
		return ir_range;
	}
	
	public static List<Double> nearest_obs(Node x){
		List<Double> nearest_obs = new ArrayList<Double>();
		int counter = 0;
		Node temp = x; 
		while(temp.getUp()!=null){
			if(temp.getUp().getPath()!=0){
				counter++;
				temp = temp.getUp();
			}
			else
				break;
		}
		nearest_obs.add((double) counter);
		counter = 0;
		temp = x;
		
		while(temp.getUpleft()!=null){
			if(temp.getUpleft().getPath()!=0){
				counter++;
				temp = temp.getUpleft();
			}
			else
				break;
		}
		nearest_obs.add((double) counter);
		counter = 0;
		temp = x;
		
		while(temp.getLeft()!=null){
			if(temp.getLeft().getPath()!=0){
				counter++;
				temp = temp.getLeft();
			}
			else
				break;
		}
		nearest_obs.add((double) counter);
		counter = 0;
		temp = x;
		
		while(temp.getBottomleft()!=null){
			if(temp.getBottomleft().getPath()!=0){
				counter++;
				temp = temp.getBottomleft();
			}
			else
				break;
		}
		nearest_obs.add((double) counter);
		counter = 0;
		temp = x;
		
		while(temp.getDown()!=null){
			if(temp.getDown().getPath()!=0){
				counter++;
				temp = temp.getDown();
			}
			else
				break;
		}
		nearest_obs.add((double) counter);
		counter = 0;
		temp = x;
		
		while(temp.getBottomright()!=null){
			if(temp.getBottomright().getPath()!=0){
				counter++;
				temp = temp.getBottomright();
			}
			else
				break;
		}
		nearest_obs.add((double) counter);
		counter = 0;
		temp = x;
		
		while(temp.getRight()!=null){
			if(temp.getRight().getPath()!=0){
				counter++;
				temp = temp.getRight();
			}
			else
				break;
		}
		nearest_obs.add((double) counter);
		counter = 0;
		temp = x;
		
		while(temp.getUpright()!=null){
			if(temp.getUpright().getPath()!=0){
				counter++;
				temp = temp.getUpright();
			}
			else
				break;
		}
		nearest_obs.add((double) counter);
		
		return nearest_obs;
	}
	
	public static void main(String[] args) {
		System.out.println("A Star Search");
		LinkedList list = new LinkedList();
		LinkedList path = new LinkedList();
		LinkedListIterator itr,pathitr;
		itr = list.zeroth();
		pathitr = path.zeroth();
	/******************************************	
	 * 	2/8/2011; Test Case 1
	 * 
	 *	Testing basic functions;
	 
		Node a = new Node(); 
		Node b = new Node();
		a = new Node(0,0,a, a, a, a);
		b = new Node(1,1,b, b, b, b);
		System.out.println(getDistance(a,b));
	*/	
	/*	2/11/2011 Test case 2
	 * 
	 * 	Testing connecting map and setting obstacles.
		int i=0;
		int j=0;
		int row=4;
		int col=3;
		Node[][] map = new Node[row][col];
		for(i=0;i<row;i++)
			for(j=0;j<col;j++)
				map[i][j] = new Node(i,j,null,null,null,null,0);
		linkMap(map);
		setMapObstacles(map,2,0);
		setMapObstacles(map,2,1);
		System.out.println("Coordinates: "+map[2][0].getX()+" "+map[2][0].getY());
		map[2][0].printNeighbors();
		System.out.println("1 if it's obstacle: " + map[2][0].getObstacle());
		System.out.println("Distance between start and end: " + getDistance(map[0][0],map[3][2]));
	*/	
		
		
	/*	2/11/2011 - 2/14/2011 Test Case 3
	 * 
	 * Testing A* basics, Testing 7*7 map
	*/
		int i=0;
		int j=0;
		int row=7;
		int col=7;
		int[][] map1 = new int[row][col];
		for(i=0;i<row;i++)
			for(j=0;j<col;j++)
				map1[i][j] = 0;
		for(i=1;i<6;i++)
			for(j=1;j<6;j++)
				map1[i][j] = 1;
		Node[][] nodemap1 = MapReader.CreateNode(map1);
		List<Double> command = new ArrayList<Double>();
		List<List<Double>> ir_range = new ArrayList<List<Double>>();
		List<Integer> heading = new ArrayList<Integer>();
		Node start,end;
		nodemap1[3][1].setObstacle(0);
		nodemap1[3][2].setObstacle(0);
		nodemap1[4][2].setObstacle(0);
		nodemap1[4][3].setObstacle(0);
		nodemap1[4][4].setObstacle(0);
//		nodemap1[4][5].setObstacle(0);
		nodemap1[6][1].setObstacle(1);
		
//		nodemap1[0][0].printNeighbors();
//		nodemap1[3][3].setObstacle(0);
//		nodemap1[5][3].setObstacle(0);

	/*	
	 * 	2/15/2011 Test Case 4
	 * 	Testing integration with map representation
	 * 
	 * 
	 */
		BufferedImage image = null;
		try{
			image = ImageIO.read(new File("test.bmp"));
		}
		catch(IOException e){}
		int [][] map2 = MapReader.ReadImage(image);
		Node [][] nodemap2 = MapReader.CreateNode(map2);
		start = nodemap1[6][1];
		end = nodemap1[1][1];
		path = findPath(nodemap1,start,end);
		System.out.println("DISPLAYING PATH");
		LinkedList.printList(path);
		heading = get_heading(path,start);
		command = move_instruction(path, start);
		System.out.println("Movement Command");
		System.out.println(command);
		ir_range = ir_helper(path,heading);
	}
}

