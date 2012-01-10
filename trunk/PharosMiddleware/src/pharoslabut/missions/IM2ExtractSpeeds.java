package pharoslabut.missions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

public class IM2ExtractSpeeds {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Vector<Double> speeds = new Vector<Double>();
		
		BufferedReader br
		   = new BufferedReader(new FileReader(args[0]));
		String strLine;
		  //Read File Line By Line
		  while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
			  //System.out.println (strLine);
			  	if (strLine.contains("Sending Command, speed=")) {
			  		String[] tokens = strLine.split(" ");
			  		//System.out.println(tokens[5]);
			  		String speedStr = tokens[5].substring(tokens[5].indexOf("=")+1);
			  		speedStr = speedStr.substring(0,speedStr.length() - 1);
			  		double speed = Double.valueOf(speedStr);
			  		if (speed != 0)
			  			speeds.add(speed);
			  }
		  }
		  //Close the input stream
		  br.close();
		  
		  System.out.println("Speed population size: " + speeds.size());
		  System.out.println("Average: " + pharoslabut.util.Stats.getAvg(speeds));
		  System.out.println("95% Conf: " + pharoslabut.util.Stats.getConf95(speeds));
	}

}
