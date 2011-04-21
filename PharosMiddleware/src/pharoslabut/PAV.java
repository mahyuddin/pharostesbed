package pharoslabut;
/*************************************************************************
**PAV Class																**
**Author: Devin Murphy													**
**Date: 1/24/2011														**
**Version: 1.1															**
**Last Modified: 2/22/2011												**
**About:																**
**																		**
**																		**
*************************************************************************/
public class PAV 
{
	private boolean initStatus;
	private String direction;
	private String name;
	private int currentX;
	private int currentY;
	private int currentNode;
	
	public PAV(String n)
	{
		name = n;
		initStatus = false;
		direction = "North";
		currentX = 0;
		currentY = 0;
		currentNode = 0;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDirection()
	{
		return direction;
	}
	
	public boolean getInitStatus()
	{
		return initStatus;
	}
	
	public int getCurrentX()
	{
		return currentX;
	}
	
	public int getCurrentY()
	{
		return currentY;
	}
	
	public void incrementNode()
	{
		currentNode++;
	}
	
	public int getCurrentNode()
	{
		return currentNode;
	}
	
	public void setCurrentX(int x)
	{
		currentX = x;
	}
	
	public void setCurrentY(int y)
	{
		currentY = y;
	}
	
	public void setCurrentXY(int x, int y)
	{
		currentX = x;
		currentY = y;
	}
	
	public void initializePAV(int x, int y)
	{
		currentX = x;
		currentY = y;
		initStatus = true;
	}
	
	public void resetInitStatus()
	{
		initStatus = false;
	}
	
}
