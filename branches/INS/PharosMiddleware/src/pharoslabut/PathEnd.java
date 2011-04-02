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
	
	public void AddMovEd(int movType, int amt)
	{
		RoboMov mov = new RoboMov(movType,amt);
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
				case 0: System.out.println("Move Fwd "+mov_cmd.MovAmt+"m"); break;
				case 1: System.out.println("Move Bwd "+mov_cmd.MovAmt+"m"); break;
				case 2: System.out.println("Turn Cw  "+mov_cmd.MovAmt+"d"); break;
				case 3: System.out.println("Turn CCW "+mov_cmd.MovAmt+"d"); break;
			}
		}
	}
	
}
