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

	public MRPConfData(MRPConfigMsg behMsg){
		_MRPCircularRepeats = 0;
		_robotData = new Vector<Robot>();
		_missionData = new Vector<MissionData>();
		StringParsing parsetheconfData;
		
		parsetheconfData = new StringParsing(behMsg.GetConfigData());
		int numofrobots = Integer.parseInt((parsetheconfData.getParameterValue("TeamSize").trim()));
		int numbehave = Integer.parseInt((parsetheconfData.getParameterValue("BehSize").trim()));
		_Myindex = Integer.parseInt((parsetheconfData.getParameterValue("MyIndex").trim()));
		_MRPCircularRepeats = Integer.parseInt((parsetheconfData.getParameterValue("Circular").trim()));		
		parsetheconfData.GetMissionData(_missionData, numbehave);
		parsetheconfData.GetRobotData(_robotData, numofrobots);
	}
	
	public int CircularRepeat(){return _MRPCircularRepeats;}
	public int GetMyindex(){return _Myindex;}
	public Robot GetRobotData(int inIndex){return _robotData.get(inIndex);}
	public int GetNumRobots(){return _robotData.size();}
	
	public MissionData GetMissionData(int inIndex){return _missionData.get(inIndex);}
	public int GetNumMissions(){return _missionData.size();}
}

