
package pharoslabut;
public class RoboMov {
	
	public int MovType;
	public MarkedPath goalWaypoint;
	//0 Forward, 1 Backward, 2 TurnCW, 3 TurnCCW
	public RoboMov(int n_mov, int n_X, int n_Y, int n_H) 
	{
		MovType = n_mov;
		goalWaypoint = new MarkedPath(n_X, n_Y, n_H);
	}

}
