package pharoslabut.demo.mrpatrol2.msgs;

import pharoslabut.RobotIPAssignments;
import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.demo.mrpatrol2.config.RobotExpSettings;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.Message;
import pharoslabut.logger.Logger;

import java.util.*;

/**
 * Contains the specifications for a multi-robot patrol 2 (MRP2) experiment.
 * This is sent by the experiment coordinator to the MPR2 servers running on
 * each robot.
 * 
 * @author Chien-Liang Fok
 */
public class LoadExpSettingsMsg implements Message {
	
	private ExpConfig expConfig;
	
	/**
	 * The constructor.
	 * 
	 * @param expConfig The experiment configuration.
	 */
	public LoadExpSettingsMsg(ExpConfig expConfig) {
		this.expConfig = expConfig;
	}
	
	/**
	 * 
	 * @return The experiment configuration.
	 */
	public ExpConfig getExpConfig() {
		return expConfig;
	}
	
	/**
	 * 
	 * @return The local robot's settings.
	 */
	public RobotExpSettings getMySettings() {
		String myName;
		try {
			myName = RobotIPAssignments.getName();
			Vector<RobotExpSettings> team = expConfig.getTeam(); 
			for (int i=0; i < team.size(); i++) {
				RobotExpSettings currSettings = team.get(i);
				if (currSettings.getName().toUpperCase().equals(myName))
					return currSettings;
			}
			Logger.logErr("Unable to find settings for myself ("+ myName + ")!");
		} catch (PharosException e) {
			e.printStackTrace();
			Logger.logErr("Unable to get my own name: " + e.getMessage());
		}
	
		System.exit(1);	
		return null;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.LOAD_SETTINGS;
	}
	
	public String toString() {
		return getClass().getName() + ": " + expConfig.toString();
	}
}