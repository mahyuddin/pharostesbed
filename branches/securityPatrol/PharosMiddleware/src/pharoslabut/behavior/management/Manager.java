package pharoslabut.behavior.management;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import pharoslabut.behavior.*;
import pharoslabut.io.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.*;
import pharoslabut.behavior.communication.*;
import pharoslabut.behavior.fileParsing.ReadWithScanner;
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
	
	private TCPMessageSender _sender;
	
	public Manager(MRPConfData mrpConfdata, NavigateCompassGPS navigationdata, TCPMessageSender sender, FileLogger flogger){
		_wm = new WorldModel(mrpConfdata.GetNumRobots(), mrpConfdata.GetMyindex());
		_behVect = new Vector<Behavior>();
		_CircularRepeats = mrpConfdata.CircularRepeat();
		_NavigateData = navigationdata;
		_sender = sender;
		_flogger = flogger;
		
		System.out.print("Manager: number of robots:"+mrpConfdata.GetNumRobots()+" nummissions: "+mrpConfdata.GetNumMissions());
		for(int index = 0; index<mrpConfdata.GetNumRobots(); index++){
			_wm.setIp(index,mrpConfdata.GetRobotData(index).GetIP());
			_wm.setPort(index, mrpConfdata.GetRobotData(index).GetPort());
		}
		for(int index = 0; index < mrpConfdata.GetNumMissions(); index++){	
			_behVect.add(new BehGotoGPSCoord(_wm, mrpConfdata.GetMissionData(index), _NavigateData, _flogger));
			_behVect.get(index).BehSetIndex(index);
			log(" behavior index is: "+_behVect.get(index).BehGetIndex());
			// add the circular behavior nature
			if(index>0){
				_behVect.get(index-1).addNext(_behVect.get(index));
			}
		}
		
		log("System circular: "+_CircularRepeats+"behavior vector size is: "+_behVect.size());
		if( _CircularRepeats > 0){
			_behVect.get(mrpConfdata.GetNumMissions()-1).addNext(_behVect.get(0));
		}
		log("Manager: after _CircularRepeats\n");
		_current = _behVect.get(0);
		_currentIndex = 0;
		try {
		log("Manager: before vector print\n");
		log("behavior number "+_behVect.get(0).BehGetIndex()+" and next is "+_behVect.get(0).getNext().BehGetIndex()+"\n");
		log("behavior number "+_behVect.get(1).BehGetIndex()+" and next is "+_behVect.get(1).getNext().BehGetIndex()+"\n");
		log("behavior number "+_behVect.get(2).BehGetIndex()+" and next is "+_behVect.get(2).getNext().BehGetIndex()+"\n");
		log("behavior number "+_behVect.get(3).BehGetIndex()+" and next is "+_behVect.get(3).getNext().BehGetIndex()+"\n");
		log("behavior number "+_behVect.get(4).BehGetIndex()+" and next is "+_behVect.get(4).getNext().BehGetIndex()+"\n");
		log("behavior number "+_behVect.get(5).BehGetIndex()+" and next is "+_behVect.get(5).getNext().BehGetIndex()+"\n");
		log("finished printing 6 behaviors\n");
		} catch(Exception e) {
			log("ERROR: " + e.getMessage());
		}
		
		for(int kk=0;kk<_behVect.size();kk++){
			log("behavior number "+_behVect.get(kk).BehGetIndex()+" and next is "+_behVect.get(kk).getNext().BehGetIndex()+System.getProperty("line.separator"));
		}
		
		
		log("_behVect: "+ _current.getClass().getName()+_currentIndex);
		_wm.setCurrentBehaviorName((_current.getClass().getName())+_currentIndex);
		
//		_server = new Server(_wm);
//		_server.start();
//		_client = new Client(_wm);
//		_client.start();
	}
	
	
//	public Manager(String configFile_team, String configFile_Behave) throws NumberFormatException, FileNotFoundException
//	{
//		ReadWithScanner fileParser = new ReadWithScanner(configFile_team);
//		int teamSize = Integer.parseInt((fileParser.getParameterValue("teamSize")).trim());
//		int myIndex = Integer.parseInt((fileParser.getParameterValue("myIndex")).trim());
//		_wm = new WorldModel(teamSize, myIndex);
//		fileParser.getTeamIpPort(_wm);
//		_robot = new Robot(_wm.getMyIp(),_wm.getMyPort());
//		
//		initialize_behaviors(configFile_Behave);
//		
//		_current = _behVect.get(0);
//		_currentIndex = 0;
//		_wm.setCurrentBehaviorName((_current.getClass().getName())+_currentIndex);
//		log("Manager: setting behavior to "+_current.getClass().getName()+_currentIndex);
//		
////		_server = new Server(_wm);
////		_server.start();
////		_client = new Client(_wm);
////		_client.start();
//	}
	
//	private void initialize_behaviors(String configFile_Behave){
//		//Creating the behaviors
//		ReadWithScanner fileParser = new ReadWithScanner(configFile_Behave);
//		int numoftargets = 0;
//		int numofrounds = 0;
//		
//		try {
//			numoftargets = Integer.parseInt((fileParser.getParameterValue("NumTargets")).trim());
//		} catch (NumberFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logErr("Error in parsing int");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			numofrounds = Integer.parseInt((fileParser.getParameterValue("NumRounds")).trim());
//		} catch (NumberFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		_CircularRepeats = numofrounds;
//		_missionData = new MissionData[numoftargets];
//		
//		try {
//			fileParser.getLatitudeLongitude(_missionData, numoftargets);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		_behVect = new Vector<Behavior>();
//		for(int j=0;j<numoftargets; j++){
//			_behVect.add(new BehGotoGPSCoord(_wm, _missionData[j], _NavigateData, _flogger));
//			_behVect.get(j).BehSetIndex(j);
//			// add the circular behavior nature
//			if(j>0){
//				_behVect.get(j-1).addNext(_behVect.get(j));
//			}
//		}
//		// make it circular - connect the last to the first
//		if(_CircularRepeats > 0)
//			_behVect.get(numoftargets-1).addNext(_behVect.get(0));
//		
//	}
	
	public void updateTeammates(String behavename, int teammateID){
		_wm.setTeamCurrentBehavior(behavename, teammateID);
	}
	
	public void run()
	{

//		while(_wm.isAllMyClientsConnected() == false || _wm.isConnectToAllServers() == false)
//		{
//			System.out.println("waiting for teammates isallmyclientsconnected (\n"+_wm.isAllMyClientsConnected()+") isConnectToAllServers ("+_wm.isConnectToAllServers());
//			try {
//				synchronized(this){
//					wait(100);
//				}
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				logErr("Manager.run(): Failed to Sleep ");
//				e.printStackTrace();
//			}
//		}
		if(_current.startCondition() == false)
		{
			log("Program finished");
			return;
		}
		//update that we established a connection to all servers, and are now able to continue
		sendBehaviorToClients();
		//If you want your robot to be synchronized when behavior changed, run this method.
		_current.waitToTeam();

		int numberRounds = _CircularRepeats*_behVect.size();
		while(_current != null)
		{
			log("running behavior "+_current.getClass().getName()+_currentIndex);

			//running the action loop
			while(!_current.stopCondition())
			{
				synchronized(this) {
				
					try {
						wait(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				_current.action();
				// This will act as keep-alive message
				sendBehaviorToClients();
			}
			log("Manager.run(): End behavior");
			
			_wm.setCurrentBehaviorName("stop"+(_current.getClass().getName())+_currentIndex);
			sendBehaviorToClients();
			log("Sending -- stop"+_current.getClass().getName()+_currentIndex+" to my clients");
			_current.waitToTeam();
			
			
			
			_current = _current.getNext();
			if(_current == null)
				break;

			_currentIndex = _current.BehGetIndex();
			
			_wm.setCurrentBehaviorName(_current.getClass().getName()+_currentIndex);
			sendBehaviorToClients();
			_current.waitToTeam();
			
			if(numberRounds<=0)
				break;
			numberRounds--;

		}
		
		log("Program finished");
		System.exit(0);
	}
	
	private void sendBehaviorToClients() {
		int i;
		MultiRobotBehaveMsg msg = null;
		
		try {
			msg = new MultiRobotBehaveMsg(_wm.getCurrentBehaviorName(), _wm.getMyIndex(), InetAddress.getByName(_wm.getMyIp()), _wm.getMyPort());
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		for (i = 0; i < _wm.getTeamSize(); i++)
			if (i != _wm.getMyIndex())
			{
				System.out.println("BEFORE Send: "+_wm.getCurrentBehaviorName()+ " Sent to Client id: "+i);			
				
				try {
					_sender.sendMessage(InetAddress.getByName(_wm.getIp(i)), _wm.getPort(i), msg);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PharosException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//_out[i].println(_wm.getCurrentBehaviorName());
				
				System.out.println("AFTER Send: "+_wm.getCurrentBehaviorName()+ " Sent to Client id: "+i);
			}
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
