package pharoslabut.behavior.management;

import java.util.Vector;
import java.io.*;
import java.util.Scanner;

public class ExtractConfData {
	public Vector<Robot> _robotinfo;
	
	ExtractConfData(String configFile) throws FileNotFoundException{
		if(configFile == null)
			return;
		
		_robotinfo= new Vector<Robot>();
		Scanner scanner = new Scanner(new FileReader(configFile));
	    try {
	        //use a Scanner to get each line
	        while ( scanner.hasNextLine() ){
	          Robot rob = processLine( scanner.nextLine() );
	          _robotinfo.add(rob);
	        }
	      }
	    finally {
	    	scanner.close();
	    }
	}
	
	protected Robot processLine(String myLine){
		    //use a second Scanner to parse the content of each line 
		  Scanner scanner = new Scanner(myLine);
		  scanner.useDelimiter(" ");
		  if ( scanner.hasNext() ){
			  String robotip = scanner.next();
		      String tmp_port = scanner.next();
		      int robotport = Integer.parseInt(tmp_port);
		      return new Robot(robotip, robotport);
		  }
		  else {
		      return null;
		  }
	}
		  
	
	
}
