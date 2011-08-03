package pharoslabut.behavior.management;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import pharoslabut.behavior.*;
import pharoslabut.io.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.*;
import pharoslabut.demo.mrpatrol.*;
import pharoslabut.exceptions.PharosException;



public class Manager {
	Robot _robot;
	WorldModel _wm;
	Vector<Behavior> _behVect;
	Behavior _current;
	int _currentIndex;
	//Server _server;
	//Client _client;
	private MissionData [] _missionData; 
	protected static FileLogger _flogger = null;
	NavigateCompassGPS _NavigateData;
	/* need to know whether to connect the last behavior to the first, and if so - for how many repeats */
	private int _CircularRepeats; 
	private boolean _manageDynamic;
	Behavior _concludeBehave;
	
	private TCPMessageSender _sender;
	
	public Manager(MRPConfData mrpConfdata, NavigateCompassGPS navigationdata, TCPMessageSender sender, FileLogger flogger){
		_wm = new WorldModel(mrpConfdata.GetNumRobots(), mrpConfdata.GetMyindex(), mrpConfdata.GetNumMissions(), flogger);
		_behVect = new Vector<Behavior>();
		_CircularRepeats = mrpConfdata.CircularRepeat();
		_NavigateData = navigationdata;
		_sender = sender;
		_flogger = flogger;
		_manageDynamic = mrpConfdata.IsDynamicCoordinated();
		
		log("Manager: number of robots:"+mrpConfdata.GetNumRobots()+
					" nummissions: "+mrpConfdata.GetNumMissions() + 
						" Dynamic (loose) coordination: "+_manageDynamic);
		for(int index = 0; index<mrpConfdata.GetNumRobots(); index++){
			_wm.setIp(index,mrpConfdata.GetRobotData(index).GetIP());
			_wm.setPort(index, mrpConfdata.GetRobotData(index).GetPort());
		}
		for(int round = 0; round < _CircularRepeats ; round ++){
			for(int index = 0; index < mrpConfdata.GetNumMissions(); index++){	
				_behVect.add(new BehGotoGPSCoord(_wm, mrpConfdata.GetMissionData(index), _NavigateData, _flogger));
				_behVect.get(_behVect.size()-1).BehSetIndex(index + round*mrpConfdata.GetNumMissions());
				log(" behavior index is: "+_behVect.get(_behVect.size()-1).BehGetIndex());
				// add the circular behavior nature
				if(index !=0 || round!=0){
					_behVect.get(_behVect.size()-2).addNext(_behVect.get(_behVect.size()-1));
				}
			}
		}
		
		log("System circular: "+_CircularRepeats+"behavior vector size is: "+_behVect.size());
//		if( _CircularRepeats > 0){
//			_behVect.get(mrpConfdata.GetNumMissions()-1).addNext(_behVect.get(0));
//		}
		log("Manager: after _CircularRepeats\n");
		_current = _behVect.get(0);
		_currentIndex = 0;
//		try {
//		log("Manager: before vector print\n");
//		log("behavior number "+_behVect.get(0).BehGetIndex()+" and next is "+_behVect.get(0).getNext().BehGetIndex()+"\n");
//		log("behavior number "+_behVect.get(1).BehGetIndex()+" and next is "+_behVect.get(1).getNext().BehGetIndex()+"\n");
//		log("behavior number "+_behVect.get(2).BehGetIndex()+" and next is "+_behVect.get(2).getNext().BehGetIndex()+"\n");
//		log("behavior number "+_behVect.get(3).BehGetIndex()+" and next is "+_behVect.get(3).getNext().BehGetIndex()+"\n");
//		log("behavior number "+_behVect.get(4).BehGetIndex()+" and next is "+_behVect.get(4).getNext().BehGetIndex()+"\n");
//		log("behavior number "+_behVect.get(5).BehGetIndex()+" and next is "+_behVect.get(5).getNext().BehGetIndex()+"\n");
//		log("finished printing 6 behaviors\n");
//		} catch(Exception e) {
//			log("ERROR: " + e.getMessage());
//		}
//		
		
		try{
			for(int kk=0;kk<_behVect.size();kk++){
				log("behavior number "+_behVect.get(kk).BehGetIndex());
				log("next is "+_behVect.get(kk).getNext().BehGetIndex());
			}
		}catch(Exception e){
			log("Error: " + e.getMessage());
		}

		if(mrpConfdata.GetHomePort() !=null){
			_concludeBehave = new BehGotoGPSCoord(_wm, mrpConfdata.GetHomePort(), _NavigateData, _flogger);
			_concludeBehave.BehSetIndex(_behVect.size());
		} else {
			_concludeBehave = null;
		}

		
		log("_behVect: "+ _current.getClass().getName()+_currentIndex);
		_wm.setMyCurrentBehavior((_current.getClass().getName())+_currentIndex, _currentIndex);
	}
	
	

	
	public void updateTeammates(String behavename, int teammateID, int behaveID){
		log("updateTeammates: new behavior " + behavename + " for teammate " + teammateID + " behaveID " + behaveID);
		_wm.setTeamCurrentBehavior(behavename, teammateID, behaveID);
	}
	
	public void run()
	{
		log("run: running Manager\n");

		if(!_current.startCondition())
		{
			log("run: Program finished (initial start condition is false)");
			return;
		}
		//update that we established a connection to all servers, and are now able to continue
		sendBehaviorToClients();
		//If you want your robot to be synchronized when behavior changed, run this method.
		waitToTeam();

//		int numberRounds = _CircularRepeats*_behVect.size();
		while(_current != null)
		{
			log("run: running behavior "+_current.getClass().getName()+_currentIndex);

			//running the action loop
			while(!_current.stopCondition())
			{
				log("run: stop condition false, continue to run...");
				synchronized(this) {
				
					try {
						wait(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				log("run: calling action...");
				_current.action();
				
				log("run: calling sendBehaviorToClients...");
				// This will act as keep-alive message
				sendBehaviorToClients();
				
				log("run: end of current loop...");
			}
			log("run: End behavior");
			
			_wm.setMyCurrentBehavior("stop"+(_current.getClass().getName())+_currentIndex, _currentIndex);
			sendBehaviorToClients();
			log("Sending -- stop"+_current.getClass().getName()+_currentIndex+" to my clients");
			waitToTeam();
			
			_current = _current.getNext();
			if(_current == null) {
				log("run: Finished all behaviors - exiting");
				break;
			}

			_currentIndex = _current.BehGetIndex();
			
			_wm.setMyCurrentBehavior(_current.getClass().getName()+_currentIndex, _currentIndex);
			sendBehaviorToClients();
			waitToTeam();
			
//			if(numberRounds<=0)
//				break;
//			numberRounds--;
		}

		if(_concludeBehave!=null){
			if(_concludeBehave.startCondition() == true){
				log("run: Entering concluding behavior (start condition true)");
				while(!_concludeBehave.stopCondition()){
					log("run: running conclude behavior");
					_concludeBehave.action();
				}
			}else{
				log("run: startCondition of concluding behavior is false");
			}
		}
		
		log("run: Program finished");
		System.exit(0);
	}
	
	/**
	 * Sends a MultiRobotBehaveMsg to each teammate.
	 */
	private void sendBehaviorToClients() {
		log("sendBehaviorToClients: Sending behavior to teammates:"
				+ "\n\tBehavior name " + _wm.getCurrentBehaviorName() 
				+ "\n\tBehavior ID: "+ _wm.getCurrentBehaviorID() 
				+ "\n\tMy index "+ _wm.getMyIndex()
				+ "\n\tMy port "+ _wm.getMyPort()+"\n");
		
		MultiRobotBehaveMsg	msg = new MultiRobotBehaveMsg(_wm.getCurrentBehaviorName(), _wm.getCurrentBehaviorID(), _wm.getMyIndex());
		log("sendBehaviorToClients: Sending message: " + msg);
		
		// for each team member
		for (int i = 0; i < _wm.getTeamSize(); i++) {
			
			// if the team member is not myself
			if (i != _wm.getMyIndex()) {
				String ip = _wm.getIp(i);
				int port = _wm.getPort(i);
				log("sendBehaviorToClients: Attempting to send message to " + ip + ":" + port);
				
				InetAddress address = null;
				try {
					address = InetAddress.getByName(ip);
				} catch (UnknownHostException e) {
					logErr("sendBehaviorToClients: UnknownHostException when trying to get InetAddress for " + ip + ", error message: " + e.getMessage());
					e.printStackTrace();
					continue;
				}
				
				if (address != null) {
					try {
						log("sendBehaviorToClients: BEFORE Send: Sending " + _wm.getCurrentBehaviorName() + " to Client " + i + " at " + address + ":" + port + "\n");		
						_sender.sendMessage(address, port, msg);
						log("sendBehaviorToClients: AFTER Send: Sent " + _wm.getCurrentBehaviorName() + " to Client " + i);
					} catch (PharosException e) {
						logErr("sendBehaviorToClients: PharosException when trying to send message to " + address + ":" + port + ", error message: " + e.getMessage());
						e.printStackTrace();
					}
				} else {
					logErr("sendBehaviorToClients: Unable to send message because address was null!");
				}
			}
		}
	}

	/**
	 * Waits for the team to become synchronized.
	 * Periodically transmits MultiRobotBehaveMsg to the entire team until
	 * the team is synchronized from this robot's perspective.
	 */
	public void waitToTeam() {
		log("waitToTeam: Waiting for teammates, dynamic = " + _manageDynamic+"\n");
		boolean isSynched;
		
		if(_manageDynamic) {
			_wm.checkAliveTeam();
			isSynched = _wm.isTeamSynchronizedDynamically();
		}else
			isSynched = _wm.isTeamSynchronized() ;

		while(!isSynched) {
			
			log("waitToTeam: team not yet synched, calling sendBehaviorToClients");
			// send message to the teammates also while waiting for updates
			sendBehaviorToClients();
			
			log("waitToTeam: done calling sendBehaviorToClients, pausing for 100ms before checking if team is synched");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(_manageDynamic)
				_wm.checkAliveTeam();
			isSynched = _manageDynamic ? _wm.isTeamSynchronizedDynamically() : _wm.isTeamSynchronized() ;
		}
		
		log("waitToTeam: team is synched!");
	}

	private void logErr(String msg) {
		String result = "Manager: ERROR: " + msg;
		
		System.err.println(result);
		
		// always log text to file if a FileLogger is present
		if (_flogger != null)
			_flogger.log(result);
	}
	
	private void log(String msg) {
		String result = "Manager: " + msg;
		
		// only print log text to string if in debug mode
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		
		// always log text to file if a FileLogger is present
		if (_flogger != null)
			_flogger.log(result);
	}


}
