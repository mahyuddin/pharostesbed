package pharoslabut;

public class RoboMov {
	
	public int MovType;
	public int MovAmt;
	//0 Forward, 1 Backward, 2 TurnCW, 3 TurnCCW
	public RoboMov(int n_mov, int n_amt) 
	{
		MovType = n_mov;
		MovAmt = n_amt;
	}

}
