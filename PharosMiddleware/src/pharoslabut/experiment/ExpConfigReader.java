package pharoslabut.experiment;

import java.io.*;

/**
 * Reads the configuration of an experiment.
 * 
 * @author Chien-Liang Fok
 *
 */
public class ExpConfigReader {

	public static ExpConfig readExpConfig(String fileName) {
		ExpConfig result = new ExpConfig();
		BufferedReader input = null;
		try {
			input =  new BufferedReader(new FileReader(fileName));
		} catch (IOException ex){
			ex.printStackTrace();
			System.err.println("Unable to open " + fileName);
			System.exit(1);
		}
		
		try {
			String line = null;
			int lineno = 1;
			while (( line = input.readLine()) != null) {
				if (!line.equals("")) {
					if (line.contains("START_INTERVAL")) {
						String[] elem = line.split("[\\s]+");
						try {
							result.setStartInterval(Integer.valueOf(elem[1]));
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Warning: Syntax error on line " + lineno + " of motion script " + fileName + ":\n" + line);
							System.exit(1);
						}
					}
					else if (line.contains("EXP_NAME")) {
						String[] elem = line.split("[\\s]+");
						try {
							result.setExpName(elem[1]);
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Warning: Syntax error on line " + lineno + " of motion script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("ROBOT")){
						try {
							String[] elem = line.split("[\\s]+");
							RobotExpSettings rs = new RobotExpSettings();
							//							System.out.println("Read in line: " + line);
							//							for (int i=0; i < elem.length; i++) {
							//								System.out.println(i + ": " + elem[i]);
							//							}
							rs.setRobotName(elem[0]);
							rs.setIPAddress(elem[1]);
							rs.setPort(Integer.valueOf(elem[3]));
							rs.setMotionScript(elem[4]);
							result.addRobot(rs);
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Warning: Ignoring line " + lineno + " of motion script " + fileName);
						}
					}
				}
				lineno++;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
