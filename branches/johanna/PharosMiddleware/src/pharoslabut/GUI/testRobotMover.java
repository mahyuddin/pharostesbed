package pharoslabut.GUI;

/**
 *
 * @author joharivsan
 */
public class testRobotMover{


	/**
     * @param args the command line arguments
     */
   public static void main(String[] args) {
       
    
		String fileName;
		String serverIP;
		int serverPort;
		String robotType;
		double speed;
		double angle;
		long duration;
		String indoor = "no";
        // indoor: yes, outdoor: no        
     //  RobotMoverList robots = new RobotMoverList.get
    
       RobotMover robot1 = new RobotMover("10.11.12.34", 6665, "RobotMover.log", "traxxas", .3, 0, 10000, "yes");
      // RobotMover robot2 = new RobotMover("10.11.12.26", 6665, "RobotMover2.log", "traxxas", .5, 0, 10000, "yes");
    
       robot1.run();
       //robot2.run();
      // robots.add(robot1);
      // robots.add(robot2);
   
    /*  try {
			for (int i=0; i < args.length; i++){
                                for(int  j =0; j < robots.size(); j++){
                                    if (args[i].equals("-server")){
                    serverIP = robots.getRobot(j).getServerIp();
                                        serverIP = args[++i];}
                                    else if (args[i].equals("-port")){
                                        
					serverPort = robots.getRobot(j).getServerPort();
                                        serverPort = Integer.valueOf(args[++i]);}
                                            
                                    else if (args[i].equals("-log")){
                                        
					fileName = robots.getRobot(j).getFileName();
                                        fileName = args[++i];}
                                            
                                    else if (args[i].equals("-robot")) {
                                        
					robotType = robots.getRobot(j).getRobotType();
                                        robotType = args[++i];}
                                            
                                    else if (args[i].equals("-speed")){
                                        speed = robots.getRobot(j).getSpeed();
                                    	speed = Double.valueOf(args[++i]);}
                                            
                                    else if (args[i].equals("-angle")){
                                        angle = robots.getRobot(j).getAngle();
					angle = Double.valueOf(args[++i]);}
                                            
                                    else if (args[i].equals("-duration")){
                                        duration = robots.getRobot(j).getDuration();
					duration = Long.valueOf(args[++i]);}
                                            
                                    else {
					usage();
					System.exit(1);
                                    }
			}
                     }
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}*/
       
     /*  for(int l = 0; l < robots.size(); l++){
           serverIP = robots.getRobot(l).getServerIp();
           serverPort = robots.getRobot(l).getServerPort();
           fileName = robots.getRobot(l).getFileName();
           robotType = robots.getRobot(l).getRobotType();
           speed = robots.getRobot(l).getSpeed();
           angle = robots.getRobot(l).getAngle();
           duration = robots.getRobot(l).getDuration();
       
 
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		System.out.println("File: " + fileName);
		System.out.println("RobotType: " + robotType);
		System.out.println("Speed: " + speed);
		System.out.println("Angle: " + angle);
		System.out.println("Duration: " + duration);
 
		new RobotMover(serverIP, serverPort, fileName, robotType, speed, angle, duration, indoor);
	}
   */
  }
}

