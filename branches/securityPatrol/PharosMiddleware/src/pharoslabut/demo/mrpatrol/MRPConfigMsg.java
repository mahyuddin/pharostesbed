package pharoslabut.demo.mrpatrol;

import java.io.*;
import java.util.Scanner;

import pharoslabut.io.*;

/**
 * This message is sent from the MRPatrolClient to the MRPatroServer to initialize an experiment.
 * 
 * @author Noa Agmon
 */
public class MRPConfigMsg implements Message {

	private static final long serialVersionUID = -7631305555004386678L;

	private String _allData;

	public MRPConfigMsg(String ConfigFile) {
		_allData = new String(ReadConfFile(ConfigFile));
		//		_allData = ReadConfFile(ConfigFile);
	}

	public String GetConfigData() {
		return _allData;
	}

	private String ReadConfFile(String Filename){
		StringBuilder text = new StringBuilder();
		String NL = System.getProperty("line.separator");
		Scanner scanner;
		try {
			scanner = new Scanner(new FileInputStream(Filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		try {
			while (scanner.hasNextLine()){
				text.append(scanner.nextLine() + NL);
			}
		}
		finally{
			scanner.close();
		}
		return text.toString();
	}

	@Override
	public MsgType getType() {
		return MsgType.CUSTOM; //return MsgType.LOAD_BEHAVIORCONFIG_FILE;
	}
	
	public String toString() {
		return "MRPConfigMsg: " + _allData;
	}
}
