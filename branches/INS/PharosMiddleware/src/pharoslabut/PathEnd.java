
package pharoslabut;


import java.util.*;


//Movements and Path
public class PathEnd 
{
	private LinkedList<MarkedPath> path_list = new LinkedList<MarkedPath>();
	private LinkedList<RoboMov> mov_list = new LinkedList<RoboMov>();
	
 	public void AddPointHd(int x, int y, int heading)
	{
		MarkedPath wayPoint = new MarkedPath(x,y,heading);
		path_list.addFirst(wayPoint);
	}
	
	public MarkedPath GetPoint(int index)
	{
		return path_list.get(index);
	}
	
	public int PathSize()
	{
		return path_list.size();
	}
	
	public void ClearPath()
	{
		path_list.clear();
	}
	
	public void AddMovEd(int movType, int x, int y, int h)
	{
		RoboMov mov = new RoboMov(movType,x,y,h);
		mov_list.add(mov);
	}

	public RoboMov GetRmvMovHd()
	{
		return mov_list.remove();	
	}
	
	public RoboMov GetMov(int index)
	{
		return mov_list.get(index);
	}
	
	public int MovSize()
	{
		return mov_list.size();
	}
	
	public void ClearMov()
	{
		mov_list.clear();
	}
	
	public void printPath()
	{
		int index;
		MarkedPath waypoint;
		for (index = 0; index < path_list.size(); index++)
		{
			waypoint = path_list.get(index);
			System.out.println("X: "+waypoint.X+" Y: "+waypoint.Y+" H: "+waypoint.H);
		}
	}
	
	public void printMov()
	{
		int index;
		RoboMov mov_cmd;
		for (index = 0; index < mov_list.size(); index++)
		{
			mov_cmd = mov_list.get(index);
			switch(mov_cmd.MovType)
			{	//0 Forward, 1 Backward, 2 TurnCW, 3 TurnCCW
				case 0: System.out.print("Move Fwd ");break;
				case 1: System.out.print("Move Bwd ");break;
				case 2: System.out.print("Turn  Cw "); break;
				case 3: System.out.print("Turn CCW ");break;
			}
			System.out.println("till: X("+mov_cmd.goalWaypoint.X+") Y("+mov_cmd.goalWaypoint.Y+") H("+mov_cmd.goalWaypoint.H+")");
		}
	}
	/*
	public void executeMov(RobotMover Robot)
	{
		int i;
		RoboMov mov_cmd;
		for(i=0; i<MovSize();i++)
		{
			mov_cmd = GetMov(i);
			Robot.FB_Mov(mov_cmd);
		}
	}*/
}
