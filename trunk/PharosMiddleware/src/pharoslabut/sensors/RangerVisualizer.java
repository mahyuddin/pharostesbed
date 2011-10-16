package pharoslabut.sensors;

//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import playerclient3.structures.ranger.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
	
	private long startTime = System.currentTimeMillis();
	
	private double distFC = 0;
	private double distFR = 0;
	private double distFL = 0;
	private double distRC = 0;
	private double distRR = 0;
	private double distRL = 0;
	
	GraphPanel graphPanel;
	
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
    	
    	// Create the IR panel
        irPanel = new IRPanel();
        window.getContentPane().add(irPanel, BorderLayout.CENTER);
        
        // Create the graph panel
        graphPanel = new GraphPanel();
        window.getContentPane().add(graphPanel, BorderLayout.EAST);
        
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
    		double timeS = (System.currentTimeMillis() - startTime) / 1000.0;
    		double[] data = rangeData.getRanges();
    		distFL = data[0];
    		distFC = data[1];
    		distFR = data[2];
    		distRL = data[3];
    		distRC = data[4];
    		distRR = data[5];
    		javax.swing.SwingUtilities.invokeLater(irPanel); // repaints the IR panel
    		
    		if (graphPanel != null) {
    			if (distFR != 0xffff) graphPanel.updateFR(timeS, distFR);
    			if (distFC != 0xffff) graphPanel.updateFC(timeS, distFC);
    			if (distFL != 0xffff) graphPanel.updateFL(timeS, distFL);
    			if (distRR != 0xffff) graphPanel.updateRR(timeS, distRR);
    			if (distRC != 0xffff) graphPanel.updateRC(timeS, distRC);
    			if (distRL != 0xffff) graphPanel.updateRL(timeS, distRL);
    		}
    	} else {
    		Logger.logErr("Expected 6 sensors, instead got " + numSensors);
    	}
    }
    
    private class GraphPanel extends JPanel {
    	int NUM_ROWS = 6;
    	int NUM_COLS = 1;
    	int GRAPH_WIDTH = 350;
    	
    	XYSeries dataSeriesRR;
    	XYSeries dataSeriesRC;
    	XYSeries dataSeriesRL;
    	XYSeries dataSeriesFR;
    	XYSeries dataSeriesFC;
    	XYSeries dataSeriesFL;
    	
    	public GraphPanel() {
    		setLayout(new GridLayout(NUM_ROWS, NUM_COLS));
    		
    		dataSeriesFR = new XYSeries("Front Right");
    		dataSeriesFC = new XYSeries("Front Center");
    		dataSeriesFL = new XYSeries("Front Left");
    		dataSeriesRR = new XYSeries("Rear Right");
    		dataSeriesRC = new XYSeries("Rear Center");
    		dataSeriesRL = new XYSeries("Rear Left");
    		
    		XYSeriesCollection datasetFR = new XYSeriesCollection();
            datasetFR.addSeries(dataSeriesFR);
            
            XYSeriesCollection datasetFC = new XYSeriesCollection();
            datasetFC.addSeries(dataSeriesFC);
            
            XYSeriesCollection datasetFL = new XYSeriesCollection();
            datasetFL.addSeries(dataSeriesFL);
            
            XYSeriesCollection datasetRR = new XYSeriesCollection();
            datasetRR.addSeries(dataSeriesRR);
            
            XYSeriesCollection datasetRC = new XYSeriesCollection();
            datasetRC.addSeries(dataSeriesRC);
            
            XYSeriesCollection datasetRL = new XYSeriesCollection();
            datasetRL.addSeries(dataSeriesRL);
            
            final JFreeChart chartFR = createChart("Front Right Ranger", datasetFR);
            final JFreeChart chartFC = createChart("Front Center Ranger", datasetFC);
            final JFreeChart chartFL = createChart("Front Left Ranger", datasetFL);
            final JFreeChart chartRR = createChart("Rear Right Ranger", datasetRR);
            final JFreeChart chartRC = createChart("Rear Center Ranger", datasetRC);
            final JFreeChart chartRL = createChart("Rear Left Ranger", datasetRL);
            
            setFixedDomainRange(chartFR);
            setFixedDomainRange(chartFC);
            setFixedDomainRange(chartFL);
            setFixedDomainRange(chartRR);
            setFixedDomainRange(chartRC);
            setFixedDomainRange(chartRL);
            
            final ChartPanel chartPanelFR = new ChartPanel(chartFR);
            chartPanelFR.setPreferredSize(new java.awt.Dimension(GRAPH_WIDTH, 100));
            final ChartPanel chartPanelFC = new ChartPanel(chartFC);
            chartPanelFC.setPreferredSize(new java.awt.Dimension(GRAPH_WIDTH, 100));
            final ChartPanel chartPanelFL = new ChartPanel(chartFL);
            chartPanelFL.setPreferredSize(new java.awt.Dimension(GRAPH_WIDTH, 100));
            final ChartPanel chartPanelRR = new ChartPanel(chartRR);
            chartPanelRR.setPreferredSize(new java.awt.Dimension(GRAPH_WIDTH, 100));
            final ChartPanel chartPanelRC = new ChartPanel(chartRC);
            chartPanelRC.setPreferredSize(new java.awt.Dimension(GRAPH_WIDTH, 100));
            final ChartPanel chartPanelRL = new ChartPanel(chartRL);
            chartPanelRL.setPreferredSize(new java.awt.Dimension(GRAPH_WIDTH, 100));
            
            add(chartPanelFR);
            add(chartPanelFC);
            add(chartPanelFL);
            add(chartPanelRR);
            add(chartPanelRC);
            add(chartPanelRL);
    	}
    	
    	public void updateFR(double time, double dist) {
    		dataSeriesFR.add(time, dist);
    	}
    	
    	public void updateFC(double time, double dist) {
    		dataSeriesFC.add(time, dist);
    	}
    	
    	public void updateFL(double time, double dist) {
    		dataSeriesFL.add(time, dist);
    	}
    	
    	public void updateRR(double time, double dist) {
    		dataSeriesRR.add(time, dist);
    	}
    	
    	public void updateRC(double time, double dist) {
    		dataSeriesRC.add(time, dist);
    	}
    	
    	public void updateRL(double time, double dist) {
    		dataSeriesRL.add(time, dist);
    	}
    }
    
    /**
     * Sets the range domain of the chart to be fixed.
     * @param chart
     */
    private void setFixedDomainRange(JFreeChart chart) {
    	final XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60);  // 60 seconds
    }
    
    /**
     * Creates the chart.
     * 
     * @param dataset The dataset.
     * @return The newly created chart.
     */
    private JFreeChart createChart(String title, XYSeriesCollection dataset) {
    	 // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
            title,      // chart title
            "Time (s)",                      // x axis label
            "Distance (m)",                  // y axis label
            dataset,                         // data
            PlotOrientation.VERTICAL,
            false,                     // include legend
            true,                     // tooltips
            false                     // urls
        );
        
        chart.setBackgroundPaint(Color.white);
        
        // get a reference to the plot for further customization...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);
        
        final NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis();
        domainAxis.setRange(new Range(0,140));

        
//        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//        rangeAxis.setRange(new Range(-Math.PI, Math.PI));
        
        return chart;
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
