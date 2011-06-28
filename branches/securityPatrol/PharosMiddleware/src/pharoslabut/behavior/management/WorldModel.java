package pharoslabut.behavior.management;

import pharoslabut.behavior.fileParsing.StringParsing;

public class WorldModel {

	private int wmTeamSize; //number of robots
	//The name of the behavior that each team member runs(this data fills by communication).
	//The value of the current robot behavior will be at the end of the array.
	private String [] wmCurrentBehavior; 
	private String [] teamIp;
	private int [] teamPort;
	private boolean allmyClientConnected; //all my clients connect
	
	private boolean connectToAllServers;
	//The robot index in team
	private int wmMyIndex;
	WorldModel(int teamSize, int robotIndex)
	{
		wmTeamSize = teamSize;
		wmMyIndex = robotIndex;
		wmCurrentBehavior = new String[wmTeamSize];
		teamIp = new String[wmTeamSize];
		teamPort = new int[wmTeamSize];
		count=0;
		allmyClientConnected = false;
		connectToAllServers = false;
	}
	private int count;
	public synchronized boolean isAllMyClientsConnected(){return allmyClientConnected;}
	public synchronized void setAllMyClientsConnected(){allmyClientConnected = true;}
	
	public synchronized boolean isConnectToAllServers(){return connectToAllServers;}
	public synchronized void setConnectToAllServers(){connectToAllServers = true;}
	
	public synchronized void setIp(int index, String ip){teamIp[index] = ip;}
	public synchronized String getIp(int index){return teamIp[index];}
	
	public synchronized void setPort(int index, int port){teamPort[index] = port;}
	public synchronized int getPort(int index){return teamPort[index];}
	
	public synchronized int getCount(){return count;}
	public synchronized void setCount(int c){count = c;}
	
	public synchronized String getCurrentBehaviorName(){return wmCurrentBehavior[wmMyIndex];}
	public synchronized void setCurrentBehaviorName(String c){wmCurrentBehavior[wmMyIndex] = c;}
	public synchronized int getMyIndex(){return wmMyIndex;}
	
	public synchronized void setTeamCurrentBehavior(String beh, int index){wmCurrentBehavior[index] = beh;}
	
	public synchronized int getTeamSize(){return wmTeamSize;}
	public synchronized String getMyIp(){return teamIp[wmMyIndex];}
	public synchronized int getMyPort(){return teamPort[wmMyIndex];}
	public synchronized boolean isTeamSynchronized()
	{
		int i;
		String behaviorName = wmCurrentBehavior[wmMyIndex];
		if(behaviorName == null)
			return false;
		
/*		if(wmTeamSize == 1)
			return true;
*/		
		boolean isStop = StringParsing.havePrefix(behaviorName, "stop"); 
		String behPureName;
		if(isStop)
			behPureName = StringParsing.removePrefix(behaviorName,"stop");
		else
			behPureName = behaviorName;
		
		String teamBeh;
		boolean isStopTeamMember;
		for(i=0;i<wmTeamSize;i++)
		{	if(wmCurrentBehavior[i] == null)
				return false;
			
		teamBeh = StringParsing.removePrefix(wmCurrentBehavior[i], "stop");
		isStopTeamMember = StringParsing.havePrefix(wmCurrentBehavior[i],"stop");
			
			if(isStop == false && behPureName.equals(teamBeh)==false)
				return false;
			if((isStop == true) && (isStopTeamMember == false) && (behPureName.equals(teamBeh)))
				return false;
			
		}
		return true;
	}
	
	
}
