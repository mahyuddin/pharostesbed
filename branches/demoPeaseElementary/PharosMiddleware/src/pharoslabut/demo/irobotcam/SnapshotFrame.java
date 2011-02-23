package pharoslabut.demo.irobotcam;

/**
 * CameraFrame.java
 * creates frames for pics, requests pics from Camera
 * 2/20/11
 */

import java.awt.*;
import java.awt.event.*;
//import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Displays a snapshot from the camera.
 * 
 * @author Chien-Liang Fok
 */
public class SnapshotFrame extends JFrame {
	
	private static final long serialVersionUID = 3325123392308489256L;

	//private boolean closedThreadState = false;
	
	// IMAGE RELATED OBJECTS
	//private Image cameraStream;
	//private BufferedImage bufferedCamera;
	
	// CAMERA CONTROL FIELDS
	//private boolean error = false;

	// The following components are GUI-level objects used in the
	// actual application GUI.
	//private JButton closeButton = null;
	//private JTextField mouseCoordinates = null;
	
	/**
	 * This is used to pause the thread that creates this frame until the frame closes.
	 */
	private Object lock = new Object();
	
	/**
	 * Create a new CameraFrame object.
	 * 
	 * @param camera
	 */
	public SnapshotFrame(Image img) {

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);	//disposes frame upon exiting
		//this.setSize(680, 480);
		
		// Create the close button and add an action listener
		// to it.
		//this.closeButton = new JButton("Close");
		//this.closeButton.setActionCommand("close");
		//this.closeButton.addActionListener(this);

		/*
		 * The following block of code builds and attaches all GUI components to
		 * the CameraFrame.
		 */
		JPanel imagPanel = new ImagePanel(img);
		imagPanel.setPreferredSize(new Dimension(DemoServer.IMAGE_WIDTH, DemoServer.IMAGE_HEIGHT));
		//content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		//content.add(this);	//paints CameraFrame
		//content.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

//		this.mouseCoordinates = new JTextField("Mouse Coordinates:  (0,0)");
//		this.mouseCoordinates.setPreferredSize(new Dimension(20, 20));
//		this.mouseCoordinates.setEditable(false);
//		this.mouseCoordinates.setHorizontalAlignment(JTextField.RIGHT);
//		this.mouseCoordinates.setFont(new Font("Arial", Font.BOLD, 12));
//		this.mouseCoordinates.setBackground(new Color(200, 200, 200));

//		JPanel controlPane = new JPanel();
//		controlPane.add(this.closeButton);
//		controlPane.add(Box.createRigidArea(new Dimension(100, 0)));
//
//		controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.LINE_AXIS));
//		controlPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
//		controlPane.add(Box.createHorizontalGlue());

		//this.getContentPane().add(this.mouseCoordinates,
		//		BorderLayout.PAGE_START);
		this.getContentPane().add(imagPanel, BorderLayout.CENTER);
//		this.getContentPane().add(controlPane, BorderLayout.PAGE_END);

		// Set the size of the window.
		//this.setSize(650, 590);

		// Set the window's location.
//		this.setLocation(100, 100);
//		this.setResizable(false);

		// For the (x,y) coordinates which are displayed in the Camera Window.
		// Add the action listener to the frame.
//		this.addMouseListener(this);
//		this.addMouseMotionListener(this);
//		
//		this.addKeyListener( new KeyAdapter() {
//			@Override
//			public void keyPressed(KeyEvent ke) {
//				// Do something cool here, I guess
//			}
//		});
		
		setLocationRelativeTo(null); // center frame
		pack();
		setVisible(true);
		
		// Add a window close listener
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent we) {
				synchronized(lock) {
					lock.notifyAll();
				}
			}
		});
	} // end public CameraFrame ( Camera camera )
	
	public void waitTillClosed() {
		// Pause the execution thread until the window closes.
		synchronized(lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private class ImagePanel extends JPanel{
		private static final long serialVersionUID = -2924283478087894058L;
		private Image image;

	    public ImagePanel(Image image) {
	       this.image = image;
	    }

	    @Override
	    public void paintComponent(Graphics g) {
	        g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters

	    }

	}

	
	
	/**
	 * Paint the camera stream to the JInternalFrame.
	 */
//	@Override
//	public void paint(Graphics g) {
//		int counter = 0; //debug
//		
//		// Print loading the window canvas....
//		if (this.cameraStream == null) {
//			
//			String debugString = "" + counter; //debug
//			g.setColor(new Color(0, 0, 0));
//			g.fillRect(0, 0, 640, 480);
//
//			g.setColor(new Color(255, 255, 255));
//			g.setFont(new Font("Arial", Font.BOLD, 50));
//			g.drawString(debugString, 175, 250); //used to be BUFFERING
//			
//			counter++; //debug
//			return;
//
//		} // end if camerastream is null
//		
//		
//		counter = 0; //debug
//		// Create a new BufferedImage, the Silhouette buffered camera.
//		this.bufferedCamera = new BufferedImage(this.cameraStream
//				.getWidth(null), this.cameraStream.getHeight(null),
//				BufferedImage.TYPE_INT_RGB);
//		
//		// Create the graphics object.
//		Graphics2D g2 = this.bufferedCamera.createGraphics();
//		g2.drawImage(this.cameraStream, null, null);
//
//		g.drawImage(this.bufferedCamera, 0, 0, null);
//
//
//		// If we have an error, show the red error strip.
//		if (this.error) {
//
//			g.setColor(new Color(255, 0, 0, 90));
//			g.fillRect(0, 456, 640, 20);
//
//			g.setColor(new Color(255, 255, 255));
//			g.setFont(new Font("Arial", Font.BOLD, 15));
//			g.drawString("I/O ERROR:  Check network connection.", 3, 471);
//
//		} // end if error
//
//	} /* print */

	/** Handles all actions triggered by various buttons and other GUI components
	 * with the camera frame.
	 */
	public void actionPerformed(ActionEvent e) {

		// If the close button was clicked.
		if ("close".equals(e.getActionCommand())) {
				//this.close();
		}

	} // end public void actionPerformed

//	/**
//	 * Handle the action of mouse movement within the Camera frame window.
//	 */
//	public void mouseMoved(MouseEvent e) {
//
//		// Get the x and y coords of the mouse click.
//		int x = e.getX() - 5;
//		int y = e.getY() - 50;
//
//		if (x >= 0 && y >= 0) {
//
//			// Set the mouse coordinates in the JTextField when the mouse is
//			// moved.
//			this.mouseCoordinates.setText("Mouse Coordinates:  (" + x + "," + y
//					+ ")");
//
//		} // end if
//
//	} // end public void mouseMoved ( ... )
//
//	public void mouseDragged(MouseEvent e) {
//	}
//
//	public void mouseClicked(MouseEvent e) {
//	}
//
//	public void mousePressed(MouseEvent e) {
//	}
//
//	public void mouseReleased(MouseEvent e) {
//	}
//
//	public void mouseEntered(MouseEvent e) {
//	}
//
//	public void mouseExited(MouseEvent e) {
//	}

	public void itemStateChanged(ItemEvent e) {
	}


} // end class CameraFrame

