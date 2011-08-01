package pharoslabut.GUI;

import java.util.ArrayList;

/**
 *
 * @author Johanna Rivera Santos
 * Summer 2011
 */
public class RobotMoverList{
   private ArrayList<RobotMover> robots;
   private static RobotMoverList SINGLETON = new  RobotMoverList();
   int counter;
    
    
    private RobotMoverList(){
        this.robots = new ArrayList<RobotMover>();
        this.counter = 0;
    }
    
    public static RobotMoverList getInstace(){
        return SINGLETON;
    }
    
    public void add(RobotMover robot){
        robots.add(robot);
        counter++;
    }
    
    public void add(int index, RobotMover robot){
        robots.add(index, robot);
        counter++;
    }
    
    public void remove(int index){
        robots.remove(index);
        counter--;
    }
    
    public int size(){
        return this.robots.size();
    }
    
    public RobotMover getRobot(int index){
        return this.robots.get(index);
    }
    
     
}


