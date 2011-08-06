package pharoslabut.demo.simonsays;

/**
 * CameraFrame.java
 * creates frames for pics, requests pics from Camera
 * 2/20/11
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.io.*;

import pharoslabut.logger.*;

/**
 * Displays a snapshot from the camera.
 * 
 * @author Chien-Liang Fok
 */
public class SnapshotFrame extends JFrame {
	
	private static final long serialVersionUID = 3325123392308489256L;
	
	/**
	 * This is used to pause the thread that creates this frame until the frame closes.
	 */
	private Object lock = new Object();
	
	private BufferedImage img;
	
	private ImageSaver imageSaver = new ImageSaver();
	
	private FileLogger flogger;
	
	/**
	 * Create a new CameraFrame object.
	 * 
	 * @param img The image to display.
	 * @param flogger The file logger for recording debug statements.
	 */
	public SnapshotFrame(BufferedImage img, FileLogger flogger) {
		this.img = img;
		this.flogger = flogger;
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);	//disposes frame upon exiting
		
		/*
		 * The following block of code builds and attaches all GUI components to
		 * the CameraFrame.
		 */
		JPanel imagPanel = new ImagePanel(img);
		imagPanel.setPreferredSize(new Dimension(SimonSaysServer.IMAGE_WIDTH, SimonSaysServer.IMAGE_HEIGHT));

		this.getContentPane().add(imagPanel, BorderLayout.CENTER);

		setLocationRelativeTo(null); // center frame
		pack();
		setVisible(true);
		
		// Create a menu bar
		JMenuItem saveImageMI = new JMenuItem("Save");
		saveImageMI.addActionListener(imageSaver);
		JMenu robotMenu = new JMenu("File");
		robotMenu.add(saveImageMI);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(robotMenu);
		setJMenuBar(menuBar);
		
		// Add a window close listener
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent we) {
				synchronized(lock) {
					lock.notifyAll();
				}
			}
		});
		
		// save image for historical record
		saveImage();
	} // end public CameraFrame ( Camera camera )
	
	/**
	 * Save the image to a file.  This is done automatically when the SnapshotFrame appears.
	 * It is done to create a historical record of the images taken by the users.
	 */
	private void saveImage() {
		
		String fileName = "SimonSaysImage";
		int indx = 0;
		String actualFileName = fileName;
		// If file exists, find an extension number to avoid deleting files
		File f = new File(actualFileName + ".jpg");
		while (f.exists()) {
			actualFileName = fileName + "-" + (indx++) + ".jpg";
			f = new File(actualFileName);
		}
		
		log("Saving image to: " + f.getName());
		try {
			f.createNewFile();
			ImageIO.write(img, "jpg", f);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private class ImageSaver implements ActionListener {
		JFileChooser fc;

		public ImageSaver() {
			fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}
		
		@Override
		public void actionPerformed(ActionEvent ae) {
			int returnVal = fc.showSaveDialog(SnapshotFrame.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                
                if (!file.exists()) {
                	try {
						file.createNewFile();
						ImageIO.write(img, "jpg", file);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(SnapshotFrame.this, "Error while saving file: " + e.getMessage());
						e.printStackTrace();
					}
                } else {
                	JOptionPane.showMessageDialog(SnapshotFrame.this, "File already exists, will not overwrite!");
                }
                
            } else {
                log("User cancelled save image operation.");
            }
		}
		
	}
	
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

	private void log(String msg) {
		String result = "SnapshotFrame: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
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

