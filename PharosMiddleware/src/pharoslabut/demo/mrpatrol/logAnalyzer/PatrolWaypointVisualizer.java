package pharoslabut.demo.mrpatrol.logAnalyzer;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;

/**
 * Displays the route taken by a robot.
 * 
 * @author Chien-Liang Fok
 *
 */
public class PatrolWaypointVisualizer extends JPanel {
	private static final long serialVersionUID = 1757014830109113998L;
	private Vector<Location> waypoints;
	private CoordinateConverter coordConv;
	
	/**
	 * The constructor.
	 * 
	 * @param waypoints The waypoints to visualize.
	 */
	public PatrolWaypointVisualizer(String title, Vector<Location> waypoints) {
		this.waypoints = waypoints;
		
		coordConv = new CoordinateConverter(waypoints);
		
		JFrame frame = new JFrame();
	    frame.setTitle(title);
	    frame.addWindowListener(new WindowAdapter() {
	      public void windowClosing(WindowEvent e) {
	        System.exit(0);
	      }
	    });
	    frame.addComponentListener(new ComponentAdapter() {
//	    	@Override
//	    	public void componentHidden(ComponentEvent arg0) {
//	    		// TODO Auto-generated method stub
//	    		
//	    	}
//
//	    	@Override
//	    	public void componentMoved(ComponentEvent arg0) {
//	    		// TODO Auto-generated method stub
//	    		
//	    	}

	    	@Override
	    	public void componentResized(ComponentEvent arg0) {
//	    		Logger.log("panel resized, height=" + getHeight() + ", width=" + getWidth());
//	    		repaint();
	    	}

//	    	@Override
//	    	public void componentShown(ComponentEvent arg0) {
//	    		// TODO Auto-generated method stub
//	    		
//	    	}
	    });
	    Container contentPane = frame.getContentPane();
	    contentPane.add(this);

	    // Make this frame half the height and width of the screen...
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    int height = screenSize.height;
	    int width = screenSize.width;
	    frame.setSize(width/2, height/2);

	    // Center the frame on the screen...
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);
	}
	
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D)g;
	    
//	    g.setColor(Color.blue);
//	    
//	    int panelWidth = getWidth();
//	    int panelHeight = getHeight();
//	    g.drawRect(10, 10, (int)(panelWidth * 0.80), (int)(panelHeight * 0.80));
//	    g.drawRoundRect(100, 10, 80, 30, 15, 15);
//
//	    int thickness = 4;
//
//	    for (int i = 0; i <= thickness; i++)
//	      g.draw3DRect(200 - i, 10 - i, 80 + 2 * i, 30 + 2 * i, true);
//	    for (int i = 0; i < thickness; i++)
//	      g.draw3DRect(200 - i, 50 - i, 80 + 2 * i, 30 + 2 * i, false);
//
//	    g.drawOval(10, 100, 80, 30);
	    
	    // Draw the waypoints
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("SansSerif", Font.BOLD, 14);
        g2.setFont(font);
        
	    for (int i=0; i < waypoints.size(); i++) {
	    	Location currLoc = waypoints.get(i);
	    	Coordinate currCoord = coordConv.getCoordinate(currLoc);
	    	g.setColor(Color.BLACK);
	    	g2.drawString(""+ i, currCoord.x-5, currCoord.y-10); 
	    	g.setColor(Color.GREEN.darker());
	    	g.fillRect(currCoord.x-5, currCoord.y-5, 10, 10);
	    }    
	  }
	
	/**
	 * Converts between physical lat/lon coordinates and logical pixel
	 * coordinates that can be displayed on the RouteGUI panel.
	 */
	private class CoordinateConverter {
		
		/**
		 * This is the amount of padding to add to the outskirts of the map.  
		 * It prevents the robot from touching the edge of the panel.
		 */
		private double paddingPct = 0.1;
		private Vector<Location> waypoints;
		double minLat, maxLat, minLon, maxLon;
		double geoWidth, geoHeight;
		
		public CoordinateConverter(Vector<Location> waypoints) {
			this.waypoints = waypoints;
			updateConvParams();
		}
		
		public void setPadding(double paddingPct) {
			this.paddingPct = paddingPct;
			updateConvParams();
		}
		
		private double getMaxLat() {
			if (waypoints.size() == 0) {
				Logger.logErr("No waypoints.");
				System.exit(1);
			}
			double result = waypoints.get(0).latitude();
			for (int i=0; i < waypoints.size(); i++) {
				Location currLoc = waypoints.get(i);
				if (currLoc.latitude() > result) 
					result = currLoc.latitude();
			}
			return result;
		}
		
		private double getMinLat() {
			if (waypoints.size() == 0) {
				Logger.logErr("No waypoints.");
				System.exit(1);
			}
			double result = waypoints.get(0).latitude();
			for (int i=0; i < waypoints.size(); i++) {
				Location currLoc = waypoints.get(i);
				if (currLoc.latitude() < result) 
					result = currLoc.latitude();
			}
			return result;
		}
		
		private double getMaxLon() {
			if (waypoints.size() == 0) {
				Logger.logErr("No waypoints.");
				System.exit(1);
			}
			double result = waypoints.get(0).longitude();
			for (int i=0; i < waypoints.size(); i++) {
				Location currLoc = waypoints.get(i);
				if (currLoc.longitude() > result) 
					result = currLoc.longitude();
			}
			return result;
		}
		
		private double getMinLon() {
			if (waypoints.size() == 0) {
				Logger.logErr("No waypoints.");
				System.exit(1);
			}
			double result = waypoints.get(0).longitude();
			for (int i=0; i < waypoints.size(); i++) {
				Location currLoc = waypoints.get(i);
				if (currLoc.longitude() < result) 
					result = currLoc.longitude();
			}
			return result;
		}
		
		private void updateConvParams() {
			double latPadding = (getMaxLat() - getMinLat()) * paddingPct / 2.0;
			double lonPadding = (getMaxLon() - getMinLon()) * paddingPct / 2.0;
			minLat = getMinLat() - latPadding;
			maxLat = getMaxLat() + latPadding;
			minLon = getMinLon() - lonPadding;
			maxLon = getMaxLon() + lonPadding;
		}
		
		/**
		 * Converts a physical location to a pixel coordinate that can be displayed on
		 * the RouteGUI panel.
		 * 
		 * @param l The location
		 * @return the pixel coordinate
		 */
		public Coordinate getCoordinate(Location l) {
			double x = (l.longitude() - minLon) / (maxLon - minLon) * getWidth();
			double y = getHeight() - (l.latitude() - minLat) / (maxLat - minLat) * getHeight();
			return new Coordinate((int)x,(int)y);
		}
	}
	
	private class Coordinate {
		int x, y;
		
		public Coordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}