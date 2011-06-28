package pharoslabut.behavior.management;

import java.io.FileNotFoundException;
import java.util.Vector;

import pharoslabut.behavior.*;
import pharoslabut.behavior.communication.*;
import pharoslabut.behavior.fileParsing.ReadWithScanner;



public class Manager {
	Robot _robot;
	WorldModel _wm;
	Vector<Behavior> _behVect;
	Behavior _current;
	Server _server;
	Client _client;
	public Manager(String configFile) throws NumberFormatException, FileNotFoundException
	{
		
		ReadWithScanner fileParser = new ReadWithScanner(configFile);
		int teamSize = Integer.parseInt((fileParser.getParameterValue("teamSize")).trim());
		int myIndex = Integer.parseInt((fileParser.getParameterValue("myIndex")).trim());
		_wm = new WorldModel(teamSize, myIndex);
		fileParser.getTeamIpPort(_wm);
		_robot = new Robot(_wm.getMyIp(),_wm.getMyPort());
		
		//Creating the behaviors
		_behVect = new Vector<Behavior>();
		_behVect.add(new BehMoveForward(_wm));
		_behVect.add(new BehRightTurn(_wm));
		_behVect.add(new BehLeftTrun(_wm));
		
		//Connecting the behaviors
		_behVect.get(0).addNext(_behVect.get(1));
		_behVect.get(0).addNext(_behVect.get(2));
		_behVect.get(2).addNext(_behVect.get(1));
		
		_current = _behVect.get(0);
		_wm.setCurrentBehaviorName(_current.getClass().getName());
		
		_server = new Server(_wm);
		_server.start();
		_client = new Client(_wm);
		_client.start();
	}
	
	public void run()
	{
		while(_wm.isAllMyClientsConnected() == false || _wm.isConnectToAllServers() == false)
		{
			try {
				Thread.currentThread().sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(_current.startCondition() == false)
		{
			System.out.println("Program finished");
			return;
		}
		//update that we established a connection to all servers, and are now able to continue
		_server.sendBehaviorToClients();
		//If you want your robot to be synchronized when behavior changed, run this method.
		_current.waitToTeam();
		
		while(_current != null)
		{
			
			//running the action loop
			while(_current.stopCondition() == false)
			{
				_current.action();
				// This will act as keep-alive message
				_server.sendBehaviorToClients();
			}
			
			_wm.setCurrentBehaviorName("stop"+(_current.getClass().getName()));
			_server.sendBehaviorToClients();
			System.out.println("Sending -- stop"+_current.getClass().getName()+" to my clients");
			_current.waitToTeam();
			
			
			
			_current = _current.getNext();
			if(_current == null)
				break;
			
			_wm.setCurrentBehaviorName(_current.getClass().getName());
			_server.sendBehaviorToClients();
			_current.waitToTeam();
			
			
			
		}
		
		System.out.println("Program finished");
		System.exit(0);
	}
}
