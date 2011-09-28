package pharoslabut.demo.mrpatrol;

import java.util.Vector;

import pharoslabut.behavior.*;
import pharoslabut.behavior.fileParsing.StringParsing;
import pharoslabut.behavior.management.*;

/**
 * Contains the specifications of a multi-robot patrol experiment.
 * 
 * @author Noa Agmon
 */
public class MRPConfData {
	private Vector<Robot> _robotData;
	private int _Myindex;
	private int _MRPCircularRepeats; 
	private Vector<MissionData> _missionData;
	private MissionData _homePort;
	private boolean _dynamicCoordination; 
	private boolean _synchronizeCoordTable;
	

	public MRPConfData(MRPConfigMsg behMsg){
		_MRPCircularRepeats = 0;
		_robotData = new Vector<Robot>();
		_missionData = new Vector<MissionData>();
		StringParsing parsetheconfData;
		
		parsetheconfData = new StringParsing(behMsg.GetConfigData());
		int numofrobots = Integer.parseInt((parsetheconfData.getParameterValue("TeamSize").trim()));
		int numbehave = Integer.parseInt((parsetheconfData.getParameterValue("BehSize").trim()));
		int getDynCoord = Integer.parseInt((parsetheconfData.getParameterValue("Dynamic").trim()));
		int getsynchtable = Integer.parseInt((parsetheconfData.getParameterValue("SynTable").trim()));
		
		_dynamicCoordination = (getDynCoord == 1)? true : false;
		_synchronizeCoordTable = (getsynchtable == 1)?true : false;
		
		_Myindex = Integer.parseInt((parsetheconfData.getParameterValue("MyIndex").trim()));
		_MRPCircularRepeats = Integer.parseInt((parsetheconfData.getParameterValue("Circular").trim()));		
		parsetheconfData.GetMissionData(_missionData, numbehave);
		parsetheconfData.GetRobotData(_robotData, numofrobots);
		_homePort = parsetheconfData.GetHomePort();
	}
	
	public boolean IsDynamicCoordinated(){return _dynamicCoordination;}
	public boolean SynchTableWithPeers(){return _synchronizeCoordTable;}
	public int CircularRepeat(){return _MRPCircularRepeats;}
	public int GetMyindex(){return _Myindex;}
	public Robot GetRobotData(int inIndex){return _robotData.get(inIndex);}
	public int GetNumRobots(){return _robotData.size();}
	public MissionData GetHomePort(){return _homePort;}
	
	public MissionData GetMissionData(int inIndex){return _missionData.get(inIndex);}
	public int GetNumMissions(){return _missionData.size();}
}

