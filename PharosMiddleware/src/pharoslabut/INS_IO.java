package pharoslabut;

import playerclient.NoNewDataException;

public class INS_IO {
	protected double Xpos;
	protected double Ypos;
	protected double Yaw;
	public INS_IO(RobotMover Mover)
	{
		try {
			Xpos = Mover.motors.getX();
		} catch (NoNewDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Ypos = Mover.motors.getY();
		} catch (NoNewDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Yaw = Mover.motors.getYaw();
		} catch (NoNewDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public double INS_UpdateX(RobotMover Vehicle){
		try {
			Xpos = Vehicle.motors.getX();
		} catch (NoNewDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Xpos;
		
	}
	public double INS_UpdateY(RobotMover Vehicle){
		try {
			Ypos = Vehicle.motors.getY();
		} catch (NoNewDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Ypos;
		
	}
	
	public String Orient(double Yaw){
		if(Yaw<45){
			return "E";
		}
		else if(Yaw<90)
		{return "NE"; 
		 }
		else if(Yaw==90)
		{return "N";
		}
		
		else if(Yaw<135){
			return "NW";
		}
		else if(Yaw==180){
			return "W";
		}
		else if(Yaw<225){
			return "SW";
		}
		else if(Yaw==270){
			return "S";
		}
		else if(Yaw < 315){
			return "SE";
		}
		else return "You're lost";
		
	}
	
	
	
}
