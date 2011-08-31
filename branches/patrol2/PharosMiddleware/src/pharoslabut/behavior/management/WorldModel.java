package pharoslabut.behavior.management;

import pharoslabut.behavior.fileParsing.StringParsing;
import pharoslabut.demo.mrpatrol.MRPConfData;
import pharoslabut.logger.Logger;

public class WorldModel {

	private int _wmTeamSize; //number of robots
	//The name of the behavior that each team member runs(this data fills by communication).
	//The value of the current robot behavior will be at the end of the array.
	private String [] _wmCurrentBehavior; 
	private String [] _teamIp;
	private int [] _teamPort;
	private long [] _lastTimeUpdate;
	private int [] _wmCurrentIndex;
	int _numBehaviors;
//	private boolean allmyClientConnected; //all my clients connect
	
	final String _outOfRange = "OUT_OF_RANGE";
	private final int VALIDITY_WINDOW = 1;
	
	//the amount of time the robot is waiting for its teammates - after that, it will continue without coordination
	final int MAX_WAITING_PERIOD = 5000;
	
//	private FileLogger _flogger = null;
	
//	private boolean connectToAllServers;
	
	/**
	 * The index of the local robot within the world model
	 */
	private int wmMyIndex;
	
	/**
	 * The number of times the stop condition was checked for a behavior.
	 */
	private int count;
	
	/**
	 * The constructor.
	 * 
	 * @param mrpConfData The configuration of the multi-robot patrol experiment.
	 */
	WorldModel(MRPConfData mrpConfData) {
		_wmTeamSize = mrpConfData.GetNumRobots(); // The number of robots in the team.
		wmMyIndex = mrpConfData.GetMyindex(); // This robot's index within the team.
		_numBehaviors = mrpConfData.GetNumMissions(); // The number of behaviors
		
		_wmCurrentBehavior = new String[_wmTeamSize];
		for(int i=0;i<_wmTeamSize; i++){
			_wmCurrentBehavior[i] = _outOfRange;
		}
		_teamIp = new String[_wmTeamSize];
		_teamPort = new int[_wmTeamSize];
		_lastTimeUpdate = new long[_wmTeamSize];
		_wmCurrentIndex = new int[_wmTeamSize];
		count=0;
//		allmyClientConnected = false;
//		connectToAllServers = false;
	}
	public WorldModel(){
		_wmTeamSize = 0; 
		wmMyIndex = 0;
		_numBehaviors = 0;
		
		_wmCurrentBehavior = new String[_wmTeamSize];
		_teamIp = new String[_wmTeamSize];
		_teamPort = new int[_wmTeamSize];
		_lastTimeUpdate = new long[_wmTeamSize];
		_wmCurrentIndex = new int[_wmTeamSize];
		count=0;
	}
	
	
//	public synchronized boolean isAllMyClientsConnected(){return allmyClientConnected;}
//	public synchronized void setAllMyClientsConnected(){allmyClientConnected = true;}
	
//	public synchronized boolean isConnectToAllServers(){return connectToAllServers;}
//	public synchronized void setConnectToAllServers(){connectToAllServers = true;}
	
	public synchronized void setIp(int index, String ip){_teamIp[index] = ip;}
	public String getIp(int index){return _teamIp[index];}
	
	public synchronized void setPort(int index, int port){_teamPort[index] = port;}
	public int getPort(int index){return _teamPort[index];}
	
	public int getCount(){return count;}
	public synchronized void incCount() { count++; }
	public synchronized void resetCount() { count = 0; }
//	public synchronized void setCount(int c){count = c;}
	
	public synchronized void setCurrentMsgTime(int index){_lastTimeUpdate[index] = System.currentTimeMillis();}
	public long getLastMsgTime(int index){return _lastTimeUpdate[index];}

	public String getCurrentBehaviorName(){return _wmCurrentBehavior[wmMyIndex];}
	public int getCurrentBehaviorID(){return _wmCurrentIndex[wmMyIndex];}
	public int getTeamBehaviorID(int index){return _wmCurrentIndex[index];}
	public String getTeamBehaviorName(int index){return _wmCurrentBehavior[index];}

//	public synchronized void setcurrentBehaviorID(int id){wmCurrentIndex[wmMyIndex] = id;}
//	public synchronized void setCurrentBehaviorName(String c){
	public synchronized void setMyCurrentBehavior(String c, int id){
		System.out.print("Set behavior name: "+c+" in location : "+wmMyIndex+System.getProperty("line.separator"));
		_wmCurrentIndex[wmMyIndex] = id;
		_wmCurrentBehavior[wmMyIndex] = new String(c);
	}
	public int getMyIndex(){return wmMyIndex;}
	
	public synchronized void setTeamCurrentBehavior(String beh, int index, int behaveID)
	{
//		boolean wasAccepted = false;
		
		
		if(beh.equals(_outOfRange)){
			Logger.log("Got an update out of range of teammate "+ index + " ignoring...");
			return;
		}

		if(_wmCurrentBehavior[index].equals(_outOfRange)){
			Logger.log("Behavior of teammate " + index + " BACK IN RANGE");
		} else {
			Logger.log("Teammate " + index + " was already in range.");
		}

		setCurrentMsgTime(index);
		
		if(_wmCurrentIndex[index] > behaveID){
			Logger.log("Got an old message: new behaveID is " + behaveID + "old behaveID is "+ _wmCurrentIndex[index]);
		}else{
			Logger.log("updating teammate "+ index + " behavior " + beh + " ID " + behaveID);
			boolean isStop = StringParsing.havePrefix(_wmCurrentBehavior[index], "stop");
			if(isStop && _wmCurrentIndex[index]==behaveID){
				Logger.log("Received an older message before STOP of teammate "+ index + " ignoring...");
				return;
			}
			_wmCurrentBehavior[index] = beh; 
			_wmCurrentIndex[index] = behaveID;

		}
/*
		for(int validIndices = wmCurrentIndex[index]; validIndices <= wmCurrentIndex[index] + VALIDITY_WINDOW && !wasAccepted; validIndices++) {
//			int currValidIndex = validIndices % _numBehaviors;
			if (behaveID == validIndices) {
				Logger.log("updating teammate "+ index + " behavior " + beh + " ID " + behaveID);
				
				wmCurrentBehavior[index] = beh; 
				wmCurrentIndex[index] = behaveID;
				
				wasAccepted = true;
			}
		}
		
		if (!wasAccepted)
			Logger.log("Got an old message: new behaveID is " + behaveID + "old behaveID is "+ wmCurrentIndex[index]);
*/
	}

	private synchronized void setTeamOutOfRange(int index){
		_wmCurrentBehavior[index] = _outOfRange;
		Logger.log("Setting teammate "+ index + " OUT_OF_RANGE");
	}
	
	public int getTeamSize(){return _wmTeamSize;}
	public String getMyIp(){return _teamIp[wmMyIndex];}
	public int getMyPort(){return _teamPort[wmMyIndex];}
	
	public synchronized void checkAliveTeam(){
		long currentTime = System.currentTimeMillis();
		for (int i=0; i<_wmTeamSize; i++){
			if(i == wmMyIndex)
				continue;
			if ((currentTime-_lastTimeUpdate[i]) > MAX_WAITING_PERIOD){
				setTeamOutOfRange(i);
			}
		}
	}

	public synchronized boolean isTeamSynchronized()
	{
		int i;
		String behaviorName = _wmCurrentBehavior[wmMyIndex];
//		int teamBehID;
//		int myBeID = getTeamBehaviorID(wmMyIndex);
		
		if(_wmTeamSize ==1) {
			Logger.log("Team is synchronized: only one team member");
			return true;
		}
		
		if(behaviorName == null) {
			Logger.log("Team NOT synchronized: current behavior is NULL\n");
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
		for(i=0; i < _wmTeamSize; i++) {	
			if(_wmCurrentBehavior[i] == null) {
				Logger.log("Team NOT synchronized: Behavior " + i + " is NULL\n");
				return false;
			} else 
				Logger.log("Behavior " + i + " is " + _wmCurrentBehavior[i]);

			
			teamBeh = StringParsing.removePrefix(_wmCurrentBehavior[i], "stop");
			isStopTeamMember = StringParsing.havePrefix(_wmCurrentBehavior[i],"stop");
			
//			teamBehID = getTeamBehaviorID(i);
			// If team member's behavior ID is smaller than mine - I have to wait for it. 
			// If teammate's behavior ID is larger than mine - I continue
			
			//Get Teammate's behavior ID
			
			if(isStop == false && behPureName.equals(teamBeh)==false) {
				Logger.log("Team NOT synchronized: isstop = false; behPureName.equals(teamBeh) = false");
				return false;
			}
			if((isStop == true) // I am stopped 
					&& (isStopTeamMember == false) // my neighbor is not stopped 
					&& (behPureName.equals(teamBeh)) // we are running the same behavior
				) {
				
				Logger.log("Team NOT synchronized: isstop = true; isStopTeamMember = false; behPureName.equals(teamBeh) = true");
				return false;
			}
/*			if((isStop == true) && (isStopTeamMember == false) && !(teamBehID == (myBeID+1)%_numBehaviors)){
				log(" Team is NOT synch: isstop = true; isStopTeamMember = false; !(teamBeID == (myBeID+1)%numBeh");
				return false;
			}
*/		}
		Logger.log("Team is synchronized: passed all false checks.");
		return true;
	}
	
	
	public synchronized boolean isTeamSynchronizedDynamically()
	{
		int i;
		String myBehaviorName = _wmCurrentBehavior[wmMyIndex];
		
		Logger.log("entering dynamic synchronization\n");
		if(myBehaviorName == null){
			System.out.print("current behavior NULL\n");
			return false;
		}
		
/*		if(wmTeamSize == 1)
			return true;
*/		
		boolean iAmStopped = StringParsing.havePrefix(myBehaviorName, "stop"); 
		String myBehPureName;
		if(iAmStopped)
			myBehPureName = StringParsing.removePrefix(myBehaviorName,"stop");
		else
			myBehPureName = myBehaviorName;
		
		String teamBeh;
		boolean isStopTeamMember;
		for(i=0;i<_wmTeamSize;i++)
		{	
			if(_wmCurrentBehavior[i] == null) {
				Logger.log("behavior "+i+" NULL\n");
				return false;
			} else 
				Logger.log("Behavior "+i+" is "+_wmCurrentBehavior[i]);

			teamBeh = StringParsing.removePrefix(_wmCurrentBehavior[i], "stop");
			isStopTeamMember = StringParsing.havePrefix(_wmCurrentBehavior[i],"stop");
			
			//If team member is out of range - disregard it
			if(teamBeh.equals(_outOfRange)){
				Logger.log("Team member " + i + " behavior is marked as OUT_OF_RANGE");
				continue;
			}
			// If team member's behavior ID is smaller than mine - I have to wait for it. 
			// If teammate's behavior ID is greater than mine - I continue
			
			//Get Teammate's behavior ID
			int mybehaveIndex = getCurrentBehaviorID();
			int teammateBehaveIndex = getTeamBehaviorID(i);
			
			if(teammateBehaveIndex<mybehaveIndex){
				Logger.log("Temmate " +  i + " with behavior ID "+ teammateBehaveIndex + ", my behavior ID = "+ mybehaveIndex +  "  ---- Team uncoordinated, WAIT");
				return false;
			}
			
			if(iAmStopped == false && myBehPureName.equals(teamBeh)==false){
				//if I'm behind - I should continue. If I'm more advanced - I should wait
				if(mybehaveIndex<=teammateBehaveIndex)
					continue;
				else
					return false;
			}
			if((iAmStopped == true) && (isStopTeamMember == false) && (myBehPureName.equals(teamBeh))){
				//if I'm behind - I should continue. If I'm more advanced - I should wait
				Logger.log("I'm stopped, my mate is not (on the same behavior) - I wait");
				return false;
			}
			
		}
		return true;
	}
	
	public synchronized void copyWM(int[] behaviorIDlist, String[] behaviorNameList){
		for(int i=0; i < _wmTeamSize; i++){
			behaviorIDlist[i] = _wmCurrentIndex[i];
			behaviorNameList[i] = _wmCurrentBehavior[i];
		}
	}
	
//	private void logErr(String msg) {
//		String result = "WorldModel: ERROR: " + msg;
//		
//		System.err.println(result);
//		
//		// always log text to file if a FileLogger is present
//		if (_flogger != null)
//			_flogger.log(result);
//	}
	
//	private void log(String msg) {
//		String result = "WorldModel: " + msg;
//		
//		// only print log text to string if in debug mode
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		
//		// always log text to file if a FileLogger is present
//		if (_flogger != null)
//			_flogger.log(result);
//	}
	
	public String toString() {
		return "WorldModel: MyIP=" + getMyIp() + ", MyPort=" + getMyPort() + ", TeamSize=" + getTeamSize() 
		+ ",\n\tMyIndex=" + getMyIndex() + ", CurrentBehaviorID=" + getCurrentBehaviorID() + ", CurrentBehaviorName=" + getCurrentBehaviorName() 
		+ ",\n\tisTeamSynchronized=" + isTeamSynchronized() + ", isTeamSynchronizedDynamically=" + isTeamSynchronizedDynamically();
	}
}
