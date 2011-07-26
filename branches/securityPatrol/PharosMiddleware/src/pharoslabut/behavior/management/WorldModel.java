package pharoslabut.behavior.management;

import pharoslabut.behavior.fileParsing.StringParsing;
import pharoslabut.logger.FileLogger;

public class WorldModel {

	private int wmTeamSize; //number of robots
	//The name of the behavior that each team member runs(this data fills by communication).
	//The value of the current robot behavior will be at the end of the array.
	private String [] wmCurrentBehavior; 
	private String [] teamIp;
	private int [] teamPort;
	private long [] _lastTimeUpdate;
	private int [] wmCurrentIndex;
//	private boolean allmyClientConnected; //all my clients connect
	
	final String _outofRange = "OUT_OF_RANGE";
	final int MAX_WAITING_PERIOD = 60000;
	
	private FileLogger _flogger = null;
	
//	private boolean connectToAllServers;
	//The robot index in team
	private int wmMyIndex;
	
	/**
	 * The constructor.
	 * 
	 * @param teamSize The number of robots in the team.
	 * @param robotIndex this robot's index within the team.
	 * @param flogger The file logger for debugging (may be null).
	 */
	WorldModel(int teamSize, int robotIndex, FileLogger flogger)
	{
		wmTeamSize = teamSize;
		wmMyIndex = robotIndex;
		_flogger = flogger;
		
		wmCurrentBehavior = new String[wmTeamSize];
		teamIp = new String[wmTeamSize];
		teamPort = new int[wmTeamSize];
		_lastTimeUpdate = new long[wmTeamSize];
		wmCurrentIndex = new int[wmTeamSize];
		count=0;
//		allmyClientConnected = false;
//		connectToAllServers = false;
	}
	private int count;
//	public synchronized boolean isAllMyClientsConnected(){return allmyClientConnected;}
//	public synchronized void setAllMyClientsConnected(){allmyClientConnected = true;}
	
//	public synchronized boolean isConnectToAllServers(){return connectToAllServers;}
//	public synchronized void setConnectToAllServers(){connectToAllServers = true;}
	
	public synchronized void setIp(int index, String ip){teamIp[index] = ip;}
	public synchronized String getIp(int index){return teamIp[index];}
	
	public synchronized void setPort(int index, int port){teamPort[index] = port;}
	public synchronized int getPort(int index){return teamPort[index];}
	
	public synchronized int getCount(){return count;}
	public synchronized void setCount(int c){count = c;}
	
	public void setCurrentMsgTime(int index){_lastTimeUpdate[index] = System.currentTimeMillis();}
	public long getLastMsgTime(int index){return _lastTimeUpdate[index];}

	public synchronized String getCurrentBehaviorName(){return wmCurrentBehavior[wmMyIndex];}
	public synchronized int getCurrentBehaviorID(){return wmCurrentIndex[wmMyIndex];}
	public synchronized int getTeamBehaviorID(int index){return wmCurrentIndex[index];}

//	public synchronized void setcurrentBehaviorID(int id){wmCurrentIndex[wmMyIndex] = id;}
//	public synchronized void setCurrentBehaviorName(String c){
	public synchronized void setMyCurrentBehavior(String c, int id){
		System.out.print("Set behavior name: "+c+" in location : "+wmMyIndex+System.getProperty("line.separator"));
		wmCurrentIndex[wmMyIndex] = id;
		wmCurrentBehavior[wmMyIndex] = new String(c);
	}
	public synchronized int getMyIndex(){return wmMyIndex;}
	
	public synchronized void setTeamCurrentBehavior(String beh, int index, int behaveID)
	{
		wmCurrentBehavior[index] = beh; 
		wmCurrentIndex[index] = behaveID;
	}
	public synchronized void setTeamOutOfRange(int index){wmCurrentBehavior[index] = _outofRange;}
	
	public synchronized int getTeamSize(){return wmTeamSize;}
	public synchronized String getMyIp(){return teamIp[wmMyIndex];}
	public synchronized int getMyPort(){return teamPort[wmMyIndex];}
	
	public synchronized boolean checkAliveTeam(){
		long currentTime = System.currentTimeMillis();
		for (int i=0;i<wmTeamSize;i++){
			if(i==wmMyIndex)
				continue;
			if ((currentTime-_lastTimeUpdate[i]) > MAX_WAITING_PERIOD){
				setTeamOutOfRange(i);
			}
		}
			
		return true;
	}

	public synchronized boolean isTeamSynchronized()
	{
		int i;
		String behaviorName = wmCurrentBehavior[wmMyIndex];
		
		if(behaviorName == null){
			log("isTeamSynchronized: current behavior NULL\n");
			return false;
		}
		
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
		{	
			if(wmCurrentBehavior[i] == null) {
				log("isTeamSynchronized: Behavior " + i + " is NULL\n");
				return false;
			} else 
				log("isTeamSynchronized: Behavior " + i + " is " + wmCurrentBehavior[i]);

			teamBeh = StringParsing.removePrefix(wmCurrentBehavior[i], "stop");
			isStopTeamMember = StringParsing.havePrefix(wmCurrentBehavior[i],"stop");
			
			// If team member's behavior ID is smaller than mine - I have to wait for it. 
			// If teammate's behavior ID is larger than mine - I continue
			
			//Get Teammate's behavior ID
			
			if(isStop == false && behPureName.equals(teamBeh)==false)
				return false;
			if((isStop == true) && (isStopTeamMember == false) && (behPureName.equals(teamBeh)))
				return false;
			
		}
		return true;
	}
	
	
	public synchronized boolean isTeamSynchronizedDynamically()
	{
		int i;
		String behaviorName = wmCurrentBehavior[wmMyIndex];
		
		log("isTeamSynchronizedDynamically: entering dynamic synchronization\n");
		if(behaviorName == null){
			System.out.print("current behavior NULL\n");
			return false;
		}
		
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
		{	
			if(wmCurrentBehavior[i] == null) {
				log("isTeamSynchronizedDynamically: behavior "+i+" NULL\n");
				return false;
			} else 
				log("isTeamSynchronizedDynamically: Behavior "+i+" is "+wmCurrentBehavior[i]);

			teamBeh = StringParsing.removePrefix(wmCurrentBehavior[i], "stop");
			isStopTeamMember = StringParsing.havePrefix(wmCurrentBehavior[i],"stop");
			
			//If team member is out of range - disregard it
			if(teamBeh.equals(_outofRange)){
				log("isTeamSynchronizedDynamically: Team member " + i + " behavior is marked as OUT_OF_RANGE");
				continue;
			}
			// If team member's behavior ID is smaller than mine - I have to wait for it. 
			// If teammate's behavior ID is greater than mine - I continue
			
			//Get Teammate's behavior ID
			int mybehaveIndex = getCurrentBehaviorID();
			int teammateBehaveIndex = getTeamBehaviorID(i);
			
			if(isStop == false && behPureName.equals(teamBeh)==false){
				//if I'm behind - I should continue. If I'm more advanced - I should wait
				if(mybehaveIndex<teammateBehaveIndex)
					continue;
				else
					return false;
			}
			if((isStop == true) && (isStopTeamMember == false) && (behPureName.equals(teamBeh))){
				//if I'm behind - I should continue. If I'm more advanced - I should wait
				if(mybehaveIndex<teammateBehaveIndex)
					continue;
				else
					return false;
			}
			
		}
		return true;
	}
	
	private void logErr(String msg) {
		String result = "WorldModel: ERROR: " + msg;
		
		System.err.println(result);
		
		// always log text to file if a FileLogger is present
		if (_flogger != null)
			_flogger.log(result);
	}
	
	private void log(String msg) {
		String result = "WorldModel: " + msg;
		
		// only print log text to string if in debug mode
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		
		// always log text to file if a FileLogger is present
		if (_flogger != null)
			_flogger.log(result);
	}
}
