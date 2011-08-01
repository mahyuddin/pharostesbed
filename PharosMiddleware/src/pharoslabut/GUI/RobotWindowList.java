/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pharoslabut.GUI;

import java.util.ArrayList;

/**
 *
 * @author Johanna Rivera Santos
 */
public class RobotWindowList {
     private ArrayList<RobotWindow> window;
   private static RobotWindowList SINGLETON = new  RobotWindowList();
   int counter;
   
   
   private RobotWindowList(){
       this.window = new ArrayList<RobotWindow>();
        this.counter = 0;
   }
    
   public static RobotWindowList getInstance(){
       return SINGLETON;
   }
   
   
   public void addWindow(RobotWindow window){
       this.window.add(window);
       counter++;
   }
   
   public void add(int index, RobotWindow window){
        this.window.add(index, window);
        counter++;
    }
   
    public void remove(int index){
        window.remove(index);
        counter--;
    }
    
    
    public RobotWindow getWindow(int index){
        return this.window.get(index);
    }
}
