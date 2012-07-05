import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Displays a compass.
 * 
 * Some of this code was taken from: http://www.applettalk.com/image-vp33891.html
 * 
 * @author Chien-Liang Fok
 */
public class CompassGUI extends JPanel implements ActionListener {
	private static final long serialVersionUID = 5533727986397454074L;

	final static RenderingHints ANTI_ALIAS = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

	boolean enabledState = false;

	javax.swing.Timer timer;
	volatile double heading = 0;  // in degrees, 0 = North, 180 = South, 90 = East, 270 = West
	int direction;

	public CompassGUI() {

		setLayout(null);

		setPreferredSize(new Dimension(300,200));
		setLocation(0,450);

		timer = new javax.swing.Timer(15,this); // calls actionPerformed(...) every 15ms
		timer.setInitialDelay(500);
		timer.start();
		enabledState = true;
	}

//	public double getHeading() {
//		heading %= 360;
//		if (heading < 0)
//			heading += 360;
//		return heading;
//	}

	/**
	 * Sets the heading.
	 * 
	 * @param heading The heading in radians (0 = north, pi/2 = West, -pi/2 = East)
	 */
	public void setHeading(double heading) {
		double headingDeg = heading * 180 / Math.PI;
		if (heading < 0)
			this.heading = -headingDeg;
		else
			this.heading = 360 - headingDeg;
		if (CompassTester.debug())
			System.out.println("Setting heading to be " + heading + "(" + this.heading + ")");
		//this.heading = heading;
		//repaint();
	}

	public void actionPerformed(ActionEvent ae) {
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
//		int width = getWidth();
//		int height = getHeight();

		Graphics2D g2D = (Graphics2D)g;
		g2D.addRenderingHints(ANTI_ALIAS);

		g2D.setColor(Color.BLACK);
		//g2D.setFont(font);

		// Draw the circle representing the compass' border
		g.drawOval(80,30,140,140);

		// Draw the degree markers
		for (int i=0; i<360; i+=5) {
			int x1 = (int)(70.0*Math.cos(Math.toRadians(i)));
			int y1 = (int)(70.0*Math.sin(Math.toRadians(i)));
			int x2 =
				(int)((i%10==0?80.0:75.0)*Math.cos(Math.toRadians(i)));
			int y2 =
				(int)((i%10==0?80.0:75.0)*Math.sin(Math.toRadians(i)));
			g2D.drawLine(x1+150,y1+100,x2+150,y2+100);
		}
		
		// draw the labels
		g2D.drawString("N",145,15);
		g2D.drawString("S",147,195);
		g2D.drawString("E",235,105);
		g2D.drawString("W",55,105);
		//g2D.drawString("045",205,40);
		//g2D.drawString("135",205,170);
		//g2D.drawString("225",63,170);
		//g2D.drawString("315",67,40);

		// draw the arrow
		int x = (int)(70.0*Math.cos(Math.toRadians(heading-90)));
		int y = (int)(70.0*Math.sin(Math.toRadians(heading-90)));
		g2D.drawLine(150,100,x+150,y+100); // arrow body

		int x2 = (int)(10.0*Math.cos(Math.toRadians(heading-60)));
		int y2 = (int)(10.0*Math.sin(Math.toRadians(heading-60)));
		int x3 = (int)(10.0*Math.cos(Math.toRadians(heading-120)));
		int y3 = (int)(10.0*Math.sin(Math.toRadians(heading-120)));
		g2D.drawLine(x+150,y+100,x+150-x2,y+100-y2); // arrow head
		g2D.drawLine(x+150,y+100,x+150-x3,y+100-y3);


//		heading %= 360;
//		if (heading < 0)
//			heading += 360;
//		DecimalFormat df = new DecimalFormat("000");
//		String headingStr = df.format(heading);
//		g2D.drawString(headingStr,120,109);
	}
	
//	public static void main(String[] args) {
//		CompassGUI compassPanel = new CompassGUI();
//		compassPanel.setHeading(90);
//		JFrame frame = new JFrame();
//		frame.getContentPane().add(compassPanel);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.pack();
//		frame.setLocationRelativeTo(frame.getRootPane());
//		frame.setVisible(true);
//		
//		while(true) {
//			for (double heading = 0; heading > -Math.PI; heading -= 0.05) {
//				compassPanel.setHeading(heading);
//				synchronized(compassPanel) {
//					try {
//						compassPanel.wait(50);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//
//			for (double heading = Math.PI; heading > 0; heading -= 0.05) {
//				compassPanel.setHeading(heading);
//				synchronized(compassPanel) {
//					try {
//						compassPanel.wait(50);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//	}
} 