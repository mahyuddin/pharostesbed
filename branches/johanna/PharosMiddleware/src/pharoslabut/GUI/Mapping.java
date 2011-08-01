package pharoslabut.GUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import pharoslabut.exceptions.NoNewDataException;


/**
 *
 * @author Johanna Rivera Summer 2011
 */
	public class Mapping extends JPanel{
            final int EARTH_RADIUS = 6371009 ;
            final Random generator = new Random();
            final Color[] colors = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GREEN, Color.MAGENTA,  
                                Color.ORANGE, Color.RED, Color.PINK, Color.LIGHT_GRAY, Color.YELLOW};
            double x = 0;
            double y = 0;
            double z = 0;
            
             Color altitude_high = new Color(0, 100, 0); //dark green
             Color altitude_med = new Color(85, 107, 47); // dark olive green
             Color altitude_low = new Color(189,183, 107); //Dark khaki
			
	
    @Override
		public void paintComponent(Graphics g) {

			Graphics2D g2 = (Graphics2D) g;
		
			/*Rectangle rectangle = new Rectangle(0, 0, 500, 500);
			g2.setColor(Color.WHITE);
			g2.fill(rectangle);
			g2.draw(rectangle);*/
                        
                        for(int i = 0; i < NewJFrame.robots.size(); i++){
                            
                            if((NewJFrame.robots.getRobot(i) != null) && NewJFrame.robots.getRobot(i).getIndoor().equalsIgnoreCase("no")) {
                                
                                Ellipse2D.Double circle;
                                    try {
                                        circle = new Ellipse2D.Double(convertX(), convertY(), 10, 10);
                                        g2.setColor(colors[i]);
                                        g2.fill(circle);
                                        g2.draw(circle);
                                     }catch (NoNewDataException ex) {
                                        Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
                                     }
                             try {
                                        if(convertZ() < 10){
                                        Ellipse2D.Double altitude = new Ellipse2D.Double(convertX(), convertY(), 20, 20);
                                        g2.setColor(altitude_low);
                                        g2.fill(altitude);
                                        g2.draw(altitude);
                
                                    }
                                     if(convertZ() >= 10){
                                        Ellipse2D.Double altitude = new Ellipse2D.Double(convertX(), convertY(), 20, 20);
                                         g2.setColor(altitude_med);
                                         g2.fill(altitude);
                                         g2.draw(altitude);
                                    }
                
                                    else{
                                         Ellipse2D.Double altitude;
                                    try {
                                        altitude = new Ellipse2D.Double(convertX(), convertY(), 20, 20);
                                        g2.setColor(altitude_high);
                                        g2.fill(altitude);
                                        g2.draw(altitude);
                                    }catch (NoNewDataException ex) {
                                          Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                       
                                    }
                } catch (NoNewDataException ex) {
                    Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
                }
                            
          }
                            
          else{
                  Ellipse2D.Double altitude = new Ellipse2D.Double(10, 10, 20, 20);
                  g2.setColor(Color.WHITE);
                  g2.fill(altitude);
                  g2.draw(altitude);
                                
                            }
                            
                        }
                       
			
                       
			
                        /*Convert units
                         *  x = R * cos(lat) * cos(lon)

                            y = R * cos(lat) * sin(lon)

                            z = R *sin(lat)
                         * R = 6371.009 km avergare
                         * 
                         * 
                         * 
                         */
			
	

		}
                
                

   
           public double convertX() throws NoNewDataException{
               
              for(int i = 0; i < NewJFrame.robots.size(); i++){
                  if(NewJFrame.robots.getRobot(i) != null){
                  x = Math.abs(EARTH_RADIUS * Math.sin(NewJFrame.robots.getRobot(i).Latitude()) * Math.cos(NewJFrame.robots.getRobot(i).Longitude())); 
                  }  
               }
               
               return x;
           }     
                
		
           public double convertY() throws NoNewDataException{
               for(int i = 0; i < NewJFrame.robots.size(); i++){
                if(NewJFrame.robots.getRobot(i) != null){
                  y =  Math.abs(EARTH_RADIUS * Math.cos(NewJFrame.robots.getRobot(i).Latitude()) * Math.sin(NewJFrame.robots.getRobot(i).Longitude())); 
                  }  
               }
               
               return y;
           }
           
           
           public double convertZ() throws NoNewDataException{
               
                for(int i = 0; i < NewJFrame.robots.size(); i++){
                if(NewJFrame.robots.getRobot(i) != null){
                  z =  Math.abs(EARTH_RADIUS * Math.cos(NewJFrame.robots.getRobot(i).Latitude()));

                  }  
               }
               return z;
           }

                
               
}




