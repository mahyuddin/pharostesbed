package pharoslabut.sensors;

//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import playerclient3.structures.ranger.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.font.TextAttribute;
import java.text.*;

/**
 * Provides a visualization of the IR sensor readings.
 * 
 * @author Chien-Liang Fok
 * @see http://pharos.ece.utexas.edu/wiki/index.php/Accessing_the_IR_sensor_plane
 */
public class RangerVisualizer {	
	private JFrame window;
	private IRPanel irPanel;
	private boolean isClosed = false;
	
	private double distFC = 0;
	private double distFR = 0;
	private double distFL = 0;
	private double distRC = 0;
	private double distRR = 0;
	private double distRL = 0;
	
	public RangerVisualizer() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
            }
        });
	}
	
    /**
     * @return whether this visualizer is closed.
     */
	public boolean isClosed() {
		return isClosed;
	}
	
	/**
	 * Show this visualizer.
	 */
	public void show() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                window.setVisible(true);
            }
        });
	}
	
	/**
	 * Hide the CPP Table.
	 */
	public void hide() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                window.setVisible(false);
            }
        });
	}
	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createGUI() {
    	window = new JFrame("IR Visualizer");
    	window.setResizable(false);
    	//window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    	window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
    	// Create the table and add it to a scroll pane...
        irPanel = new IRPanel();
        window.getContentPane().add(irPanel, BorderLayout.CENTER);
        
        
        window.addWindowListener(new WindowAdapter() {
        	public void windowClosed(WindowEvent we) {
        		isClosed = true;
        	}
        });
        
        window.pack();
        window.setLocationRelativeTo(null); // center frame
    }
    
    public void updateDistances(PlayerRangerData rangeData) {
    	int numSensors = rangeData.getRanges_count();
    	if (numSensors == 6) {
    		double[] data = rangeData.getRanges();
    		distFL = data[0];
    		distFC = data[1];
    		distFR = data[2];
    		distRL = data[3];
    		distRC = data[4];
    		distRR = data[5];
    		javax.swing.SwingUtilities.invokeLater(irPanel); // repaints the IR panel
    	} else {
    		Logger.logErr("Expected 6 sensors, instead got " + numSensors);
    	}
    }
    
    /**
     * Displays a graphic showing the current IR measurements.
     * 
     * @author Chien-Liang Fok
     *
     */
    private class IRPanel extends JComponent implements Runnable {
    	
		private static final long serialVersionUID = -8471920724257172768L;
		private static final int FRONT_LABEL_SIZE = 40;
		private static final int DISTANCE_LABEL_SIZE = 18;
    	
    	private static final double ANGLE_FC = 0;
    	private static final double ANGLE_FR = Math.PI/4;
    	private static final double ANGLE_FL = -Math.PI/4;
    	private static final double ANGLE_RC = 0;
    	private static final double ANGLE_RR = -Math.PI/4;
    	private static final double ANGLE_RL = Math.PI/4;
    	
    	public IRPanel() {
    		setPreferredSize(new Dimension(500, 600));
    	}
    	
    	public void paint(Graphics g) {
    		
    		// calculate the dimensions and location of the box representing
    		// the sensor plane.
    		int spWidth = (int)(getWidth() * 0.75);
    		int spHeight = (int)(getHeight() * 0.75);
    		int spX = (getWidth() - spWidth) / 2;
    		int spY = (getHeight() - spHeight) / 2;
    		
    		// draw the sensor plane
    		g.setColor(Color.LIGHT_GRAY);
    		g.fillRect(spX, spY, spWidth, spHeight);
    		
    		
    		// Calculate the dimensions and location of each sensor on the 
    		// sensor plane.
    		int sWidth = spWidth / 4;
    		int sHeight = sWidth /2;
    		
    		FontMetrics fm;
    		Rectangle2D textsize;
    		AttributedCharacterIterator aci;
    		
    		// Display "FRONT"
    		Graphics2D g2d = (Graphics2D)g;
    		g.setColor(Color.BLACK);
    	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	    AttributedString as1 = new AttributedString("FRONT");
    	    as1.addAttribute(TextAttribute.SIZE, FRONT_LABEL_SIZE);
    	    fm = getFontMetrics(g.getFont().deriveFont((float)FRONT_LABEL_SIZE));
    	    aci = as1.getIterator();
    	    textsize = fm.getStringBounds(aci, 0, aci.getEndIndex(), g);
    	    g2d.drawString(as1.getIterator(), (int)(spWidth/2 + spX - textsize.getWidth() / 2), spY + sHeight/2 + fm.getAscent());
    	    
    	    AffineTransform origAT = g2d.getTransform(); // save the original affine transform
    	    
    	    Point centerPoint = null; // The center point of the sensor rectangle 
    	    Point origPoint = null; // The upper-left coordinate of the sensor rectangle
    	    
    	    // Front Center sensor
    	    centerPoint = new Point(getWidth()/2, spY);
    	    origPoint = new Point(centerPoint.x - sWidth / 2, centerPoint.y - sHeight / 2);
    	    as1 = new AttributedString(String.valueOf(distFC));
    	    as1.addAttribute(TextAttribute.SIZE, DISTANCE_LABEL_SIZE);
    	    g2d.rotate(ANGLE_FC, centerPoint.x, centerPoint.y);
    	    g2d.setColor(Color.DARK_GRAY);
    	    g2d.fillRect(origPoint.x, origPoint.y, sWidth, sHeight);
    	    g2d.setColor(Color.BLACK);
    	    fm = getFontMetrics(g.getFont().deriveFont((float)DISTANCE_LABEL_SIZE));
    	    aci = as1.getIterator();
    	    textsize = fm.getStringBounds(aci, 0, aci.getEndIndex(), g);
    	    g2d.drawString(as1.getIterator(), centerPoint.x - (int)(textsize.getWidth()/2), origPoint.y - (int)textsize.getHeight());
    	    g2d.setTransform(origAT);
    	    
    	    // Front Right Sensor
    	    centerPoint = new Point(spWidth + spX - sHeight / 2, spY + sHeight / 2); 
    	    origPoint = new Point(centerPoint.x - sWidth / 2, centerPoint.y - sHeight / 2);
    	    as1 = new AttributedString(String.valueOf(distFR));
    	    as1.addAttribute(TextAttribute.SIZE, DISTANCE_LABEL_SIZE);
    	    g2d.rotate(ANGLE_FR, centerPoint.x, centerPoint.y);
    	    g2d.setColor(Color.DARK_GRAY);
    	    g2d.fillRect(origPoint.x, origPoint.y, sWidth, sHeight);
    	    g2d.setColor(Color.BLACK);
    	    //fm = getFontMetrics(g.getFont().deriveFont((float)DISTANCE_LABEL_SIZE));
    	    aci = as1.getIterator();
    	    textsize = fm.getStringBounds(aci, 0, aci.getEndIndex(), g);
    	    g2d.drawString(as1.getIterator(), centerPoint.x - (int)(textsize.getWidth()/2), origPoint.y - (int)textsize.getHeight());
    	    g2d.setTransform(origAT);
    	    
    	    // Front left sensor
    	    centerPoint = new Point(spX + sHeight / 2, spY + sHeight / 2); 
    	    origPoint = new Point(centerPoint.x - sWidth / 2, centerPoint.y - sHeight / 2);
    	    as1 = new AttributedString(String.valueOf(distFL));
    	    as1.addAttribute(TextAttribute.SIZE, DISTANCE_LABEL_SIZE);
    	    g2d.rotate(ANGLE_FL, centerPoint.x, centerPoint.y);
    	    g2d.setColor(Color.DARK_GRAY);
    	    g2d.fillRect(origPoint.x, origPoint.y, sWidth, sHeight);
    	    g2d.setColor(Color.BLACK);
    	    //fm = getFontMetrics(g.getFont().deriveFont((float)DISTANCE_LABEL_SIZE));
    	    aci = as1.getIterator();
    	    textsize = fm.getStringBounds(aci, 0, aci.getEndIndex(), g);
    	    g2d.drawString(as1.getIterator(), centerPoint.x - (int)(textsize.getWidth()/2), origPoint.y - (int)textsize.getHeight());
    	    g2d.setTransform(origAT);
    	    
    	    // Rear Center sensor
    	    centerPoint = new Point(getWidth()/2, spY + spHeight);
    	    origPoint = new Point(centerPoint.x - sWidth / 2, centerPoint.y - sHeight / 2);
    	    as1 = new AttributedString(String.valueOf(distRC));
    	    as1.addAttribute(TextAttribute.SIZE, DISTANCE_LABEL_SIZE);
    	    g2d.rotate(ANGLE_RC, centerPoint.x, centerPoint.y);
    	    g2d.setColor(Color.DARK_GRAY);
    	    g2d.fillRect(origPoint.x, origPoint.y, sWidth, sHeight);
    	    g2d.setColor(Color.BLACK);
    	    //fm = getFontMetrics(g.getFont().deriveFont((float)DISTANCE_LABEL_SIZE));
    	    aci = as1.getIterator();
    	    textsize = fm.getStringBounds(aci, 0, aci.getEndIndex(), g);
    	    g2d.drawString(as1.getIterator(), centerPoint.x - (int)(textsize.getWidth()/2), origPoint.y + sHeight + (int)textsize.getHeight() + 10);
    	    g2d.setTransform(origAT);
    	    
    	    // Rear Right Sensor
    	    centerPoint = new Point(spWidth + spX - sHeight / 2, spY + spHeight - sHeight / 2); 
    	    origPoint = new Point(centerPoint.x - sWidth / 2, centerPoint.y - sHeight / 2);
    	    as1 = new AttributedString(String.valueOf(distRR));
    	    as1.addAttribute(TextAttribute.SIZE, DISTANCE_LABEL_SIZE);
    	    g2d.rotate(ANGLE_RR, centerPoint.x, centerPoint.y);
    	    g2d.setColor(Color.DARK_GRAY);
    	    g2d.fillRect(origPoint.x, origPoint.y, sWidth, sHeight);
    	    g2d.setColor(Color.BLACK);
    	    //fm = getFontMetrics(g.getFont().deriveFont((float)DISTANCE_LABEL_SIZE));
    	    aci = as1.getIterator();
    	    textsize = fm.getStringBounds(aci, 0, aci.getEndIndex(), g);
    	    g2d.drawString(as1.getIterator(), centerPoint.x - (int)(textsize.getWidth()/2), origPoint.y + sHeight + (int)textsize.getHeight() + 10);
    	    //g2d.drawString(distString, origPoint.x, origPoint.y + sHeight + 15);
    	    g2d.setTransform(origAT);
    	    
    	    // Rear left sensor
    	    centerPoint = new Point(spX + sHeight / 2, spY + spHeight - sHeight / 2); 
    	    origPoint = new Point(centerPoint.x - sWidth / 2, centerPoint.y - sHeight / 2);
    	    as1 = new AttributedString(String.valueOf(distRL));
    	    as1.addAttribute(TextAttribute.SIZE, DISTANCE_LABEL_SIZE);
    	    g2d.rotate(ANGLE_RL, centerPoint.x, centerPoint.y);
    	    g2d.setColor(Color.DARK_GRAY);
    	    g2d.fillRect(origPoint.x, origPoint.y, sWidth, sHeight);
    	    g2d.setColor(Color.BLACK);
    	    //fm = getFontMetrics(g.getFont().deriveFont((float)DISTANCE_LABEL_SIZE));
    	    aci = as1.getIterator();
    	    textsize = fm.getStringBounds(aci, 0, aci.getEndIndex(), g);
    	    g2d.drawString(as1.getIterator(), centerPoint.x - (int)(textsize.getWidth()/2), origPoint.y + sHeight + (int)textsize.getHeight() + 10);
    	    //g2d.drawString(distString, origPoint.x, origPoint.y + sHeight + 15);
    	    g2d.setTransform(origAT);
    	    
    	}
    	
    	public void run() {
    		repaint();
    	}
    }
    
//	private void log(String msg) {
//		String result = "RangerVisualizer: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
	
	
//	public static void main(String[] args) {
//		System.setProperty ("PharosMiddleware.debug", "true");
//		IRVisualizer v = new IRVisualizer();
//		v.show();
//	}
}
