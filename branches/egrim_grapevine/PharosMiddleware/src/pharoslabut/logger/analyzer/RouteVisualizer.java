package pharoslabut.logger.analyzer;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

//import org.jfree.ui.RefineryUtilities;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.logger.analyzer.motion.AbsoluteDivergenceAnalyzer;
import pharoslabut.logger.analyzer.motion.SpatialDivergence;
import pharoslabut.navigate.Location;

/**
 * Displays the route taken by a robot.
 * 
 * @author Chien-Liang Fok
 *
 */
public class RouteVisualizer extends JPanel {
	private static final long serialVersionUID = 1757014830109113998L;
	private RobotExpData robotData;
	private CoordinateConverter coordConv;
	private boolean showAbsoluteDivergence = true;
	
	/**
	 * The constructor.
	 * 
	 * @param robotData
	 */
	public RouteVisualizer(RobotExpData robotData) {
		this.robotData = robotData;
		coordConv = new CoordinateConverter(robotData);
		
		JFrame frame = new JFrame();
	    frame.setTitle(getClass().getName() + ": " + robotData.getMissionName() + "-" + robotData.getExpName() + "-" + robotData.getRobotName());
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
	    		Logger.log("panel resized, height=" + getHeight() + ", width=" + getWidth());
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
        
	    Vector<Location> waypoints = robotData.getWayPoints();
	    for (int i=0; i < waypoints.size(); i++) {
	    	Location currLoc = waypoints.get(i);
	    	Coordinate currCoord = coordConv.getCoordinate(currLoc);
	    	g.setColor(Color.BLACK);
	    	g2.drawString(""+ i, currCoord.x-5, currCoord.y-10); 
	    	g.setColor(Color.GREEN.darker());
	    	g.fillRect(currCoord.x-5, currCoord.y-5, 10, 10);
	    }
	    
	    // Draw the ideal path
	    g.setColor(Color.BLUE);
	    Vector<PathEdge> edges = robotData.getPathEdges();
	    for (int i=0; i < edges.size(); i++) {
	    	PathEdge currEdge = edges.get(i);
	    	Coordinate startCoord = coordConv.getCoordinate(currEdge.getIdealStartLoc());
	    	Coordinate endCoord = coordConv.getCoordinate(currEdge.getEndLocation());
	    	g.drawLine(startCoord.x, startCoord.y, endCoord.x, endCoord.y);
	    }
	    
	    // Draw the actual path taken by the robot
	    g.setColor(Color.RED);
	    Vector<GPSLocationState> gpsData = robotData.getGPSHistory();
	    for (int i=0; i < gpsData.size(); i++) {
	    	Location currLoc = gpsData.get(i).getLocation();
	    	Coordinate coord = coordConv.getCoordinate(currLoc);
	    	g.fillRect(coord.x-3, coord.y-3, 6, 6);
	    }
	    
	    // Draw the interpolated location of the robot.
	    for (long time = robotData.getStartTime(); time < robotData.getStopTime()-100; time += 100) {
	    	Coordinate currCoord = coordConv.getCoordinate(robotData.getLocation(time));
	    	Coordinate nextCoord = coordConv.getCoordinate(robotData.getLocation(time+100));
	    	g.drawLine(currCoord.x, currCoord.y, nextCoord.x, nextCoord.y);
	    }
	    
	    if (showAbsoluteDivergence) {
	    	FileLogger flogger = new FileLogger("DebugAbsoluteDivergence.txt");
	    	Logger.setFileLogger(flogger);
	    	
	    	// Draw the absolute divergences
	    	long samplingInterval = 1000;
	    	Vector<SpatialDivergence> absDivs = AbsoluteDivergenceAnalyzer.getAnalyzer().getAbsoluteDivergence(robotData, samplingInterval);
	    	for (int i=0; i < absDivs.size(); i++) {
	    		SpatialDivergence currDiv = absDivs.get(i);
	    		PathEdge edge = robotData.getRelevantPathEdge(currDiv.getTimeStamp());
	    		Logger.logDbg("Divergence along path edge " + edge.getSeqNo());
	    		
	    		Coordinate currCoord = coordConv.getCoordinate(currDiv.getCurrLoc());
	    		Coordinate idealCoord = coordConv.getCoordinate(currDiv.getIdealLoc());
	    		boolean absolute = false;
	    		if (currDiv.getDivergence(absolute) < 0)
	    			g.setColor(Color.MAGENTA);
	    		else
	    			g.setColor(Color.CYAN);
	    		g.drawLine(currCoord.x, currCoord.y, idealCoord.x, idealCoord.y);
	    	}
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
		private RobotExpData robotData;
		double minLat, maxLat, minLon, maxLon;
		double geoWidth, geoHeight;
		
		public CoordinateConverter(RobotExpData robotData) {
			this.robotData = robotData;
			updateConvParams();
		}
		
		public void setPadding(double paddingPct) {
			this.paddingPct = paddingPct;
			updateConvParams();
		}
		
		private void updateConvParams() {
			double latPadding = (robotData.getMaxLat() - robotData.getMinLat()) * paddingPct / 2.0;
			double lonPadding = (robotData.getMaxLon() - robotData.getMinLon()) * paddingPct / 2.0;
			minLat = robotData.getMinLat() - latPadding;
			maxLat = robotData.getMaxLat() + latPadding;
			minLon = robotData.getMinLon() - lonPadding;
			maxLon = robotData.getMaxLon() + lonPadding;
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
	
	public static void main(String[] args) {
		System.setProperty ("PharosMiddleware.debug", "true");
		RobotExpData robotData = new RobotExpData("M27-NavCompGPS-Exp1-ZIEGEN-Pharos_20110819071954.log");
		new RouteVisualizer(robotData);
	}
}
