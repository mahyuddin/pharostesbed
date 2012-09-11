package pharoslabut.behavior.fileParsing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import pharoslabut.behavior.MissionData;
import pharoslabut.behavior.management.Robot;

/**
 * This is used by pharoslabut.demo.mrpatrol.MRPConfData
 * 
 * @author Noa Agmon
 */
public class StringParsing {
	private String _dataBuffer;
//	private boolean _simulationmode;

	public StringParsing(String databuffer){
		_dataBuffer = new String(databuffer);
//		_simulationmode = (System.getProperty ("simulateBehave") != null) ? true : false;
	}
	
	public static String removePrefix(String str, String prefix)
	{
		if(str.startsWith(prefix))
			return str.substring(prefix.length());
		else
			return str;
	}
	
	public String getParameterValue(String parameter){
		Scanner scanner = new Scanner(_dataBuffer);
		String line;
		String value;
		try {
			// first use a Scanner to get each line

			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				Scanner lineScanner = new Scanner(line);
				try {
					lineScanner.useDelimiter("=");
					if (lineScanner.hasNext()) {
						value = lineScanner.next();
						value = value.trim();
						if (value.equals(parameter)) {
							value = (lineScanner.next()).trim();
							return value;
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	public static boolean havePrefix(String str, String prefix)
	{
		if(str.startsWith(prefix))
			return true;
		else
			return false;
	}

	public static String copyFileToBuffer(String filename)
	{
	    StringBuilder text = new StringBuilder();
	    String NL = System.getProperty("line.separator");
	    Scanner scanner;
		try {
			scanner = new Scanner(new FileInputStream(filename));
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
	
	public boolean GetMissionData(Vector<MissionData> missiondata, int nummission){
		Scanner scanner = new Scanner(_dataBuffer);
		String line;
		String parameter = "MissionInformation";
		boolean foundtheline = false;
		String value;
		try {
			// first use a Scanner to get each line

			while (scanner.hasNextLine() && !foundtheline) {
				line = scanner.nextLine();
				Scanner lineScanner = new Scanner(line);
				try {
					lineScanner.useDelimiter("=");
					if (lineScanner.hasNext()) {
						value = lineScanner.next();
						value = value.trim();
						if (value.equals(parameter)) {// now the next nummissions lines hold the information we need
							foundtheline=true;
						}
					}
				} catch (Exception e) {
					System.out.print("\tError in reading Mission Configuration data\n");
					e.printStackTrace();
				}
			}
			if(foundtheline){
				String Avalue;
				for(int myline=0;myline<nummission;myline++){
					line = scanner.nextLine();
					Scanner lineScanner = new Scanner(line);
					lineScanner.useDelimiter(";");
					Avalue = lineScanner.next().trim();
					double latitude = Double.parseDouble(Avalue);
					Avalue = lineScanner.next().trim();
					double longitude = Double.parseDouble(Avalue);
					Avalue = lineScanner.next().trim();
					double velocity = Double.parseDouble(Avalue);
					missiondata.add(new MissionData(latitude, longitude, velocity));
				}
//				String newLine = System.getProperty("line.separator");
				//if(_simulationmode){
				if(true){
					for(int i=0; i < nummission; i++)
			    		System.out.println("Destination: " + missiondata.get(i).getDest() 
			    				+ " velocity: " + missiondata.get(i).getVelocity());
				}
		  		return true;
			}
		} catch (Exception e) {
			System.out.print("Error in parsing data\n");
		}
		return false;	
	}
	
	public MissionData GetHomePort(){
		Scanner scanner = new Scanner(_dataBuffer);
		String line;
		String parameter = "HomePort";
		String value;
		try {
			// first use a Scanner to get each line

			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				Scanner lineScanner = new Scanner(line);
				try {
					lineScanner.useDelimiter("=");
					if (lineScanner.hasNext()) {
						value = lineScanner.next();
						value = value.trim();
						if (value.equals(parameter)) {
							value = (lineScanner.next()).trim();
							String Avalue;
							Scanner datalineScanner = new Scanner(value);
							datalineScanner.useDelimiter(";");
							Avalue = datalineScanner.next().trim();
							double latitude = Double.parseDouble(Avalue);
							Avalue = datalineScanner.next().trim();
							double longitude = Double.parseDouble(Avalue);
							Avalue = datalineScanner.next().trim();
							double velocity = Double.parseDouble(Avalue);
							return new MissionData(latitude, longitude, velocity);	
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;	

	}
	
	public boolean GetRobotData(Vector<Robot> robotdata, int numrobots){
		Scanner scanner = new Scanner(_dataBuffer);
		String line;
		String parameter = "RobotInformation";
		boolean foundtheline = false;
		String value;
		try {
			// first use a Scanner to get each line

			while (scanner.hasNextLine() && !foundtheline) {
				line = scanner.nextLine();
				Scanner lineScanner = new Scanner(line);
				try {
					lineScanner.useDelimiter("=");
					if (lineScanner.hasNext()) {
						value = lineScanner.next();
						value = value.trim();
						if (value.equals(parameter)) {// now the next nummissions lines hold the information we need
							foundtheline=true;
						}
					}
				} catch (Exception e) {
					System.out.print("Error in reading Robot data\n");
				}
			}
			if(foundtheline){
				String Avalue;
				String myip;
				for(int myline=0;myline<numrobots;myline++){
					line = scanner.nextLine();
					Scanner lineScanner = new Scanner(line);
					lineScanner.useDelimiter(";");
					lineScanner.next().trim(); // get rid of the index
					myip = lineScanner.next().trim();
					Avalue = lineScanner.next().trim();
					int myport = Integer.parseInt(Avalue);
					robotdata.add(new Robot(myip, myport));
				}
//				if(_simulationmode){
				if(true){
					String newLine = System.getProperty("line.separator");
			       	for(int i=0;i<numrobots;i++)
			    		System.out.print("IP: "+robotdata.get(i).GetIP()+" Port: "+robotdata.get(i).GetPort()+newLine);
				}
				return true;
			}
		} catch (Exception e) {
			System.out.print("Error in parsing data\n");
		}
		return false;	

	}
		
	public void write(){
		System.out.print(_dataBuffer);
	}
	


public static void main(String... aArgs) throws IOException {
    String fileName = aArgs[0];
    StringParsing test = new StringParsing(copyFileToBuffer(fileName));
	Vector<Robot> robotData = new Vector<Robot>();
	Vector<MissionData> missionData = new Vector<MissionData>();
//	String newLine = System.getProperty("line.separator");
	MissionData homeport;
   
    int numofrobots = Integer.parseInt((test.getParameterValue("TeamSize").trim()));
    int numbehave = Integer.parseInt((test.getParameterValue("BehSize").trim()));
    int myindex = Integer.parseInt((test.getParameterValue("MyIndex").trim()));
    int circular = Integer.parseInt((test.getParameterValue("Circular").trim()));
    
    System.out.println("circular: " + circular + " my index: " + myindex);
    System.out.println("numofbehave: " + numbehave + " numofrobots: "+numofrobots);
    
    if(!test.GetMissionData(missionData, numbehave)){
    	System.out.println("unable to create "+numbehave+" behaviors");
    }
    else{
    	System.out.println("Printing all " + numbehave + " behaviors");
    	for(int i=0;i<numbehave;i++){
    		
    		System.out.println("Dest: " + missionData.get(i).getDest() + " velocity: " + missionData.get(i).getVelocity());	
    	}
    }
    if(!test.GetRobotData(robotData, numofrobots)){
      	System.out.println("unable to create " + numofrobots + " robots");	   	
    } else {
    	for(int i=0;i<numofrobots;i++){
    		System.out.println("IP: "+robotData.get(i).GetIP() + " Port: " + robotData.get(i).GetPort());
    	}
    	
    }
    homeport = test.GetHomePort();
    if(homeport!=null){
    	System.out.println("HOMEPORT: Dest: " + homeport.getDest() + "velocity: "+ homeport.getVelocity());
    }
    	
    test.write();
    System.exit(1);
  }
}