package pharoslabut.sensors;

//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import playerclient3.structures.blobfinder.PlayerBlobfinderBlob;
import playerclient3.structures.blobfinder.PlayerBlobfinderData;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

//import java.awt.geom.AffineTransform;
//import java.awt.geom.Rectangle2D;
//import java.awt.font.TextAttribute;
//import java.text.*;

/**
 * Visualizes the blobs found by the blob finder.
 * 
 * @author Chien-Liang Fok
 */
public class BlobFinderVisualizer extends JFrame {
	private static final long serialVersionUID = 7832971846802654785L;
	
//	private FileLogger flogger;
	private BlobPanel blobPanel;
	
	public BlobFinderVisualizer() {
		super("BlobFinder Visualizer");;
	}
	 
	protected void frameInit() {
		super.frameInit();
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		// Create the table and add it to a scroll pane...
		blobPanel = new BlobPanel();
		getContentPane().add(blobPanel, BorderLayout.CENTER);


		pack();
		setLocationRelativeTo(null); // center frame
		setVisible(true);
	}
	
    /**
     * Show this visualizer.
     */
    public void showVisualizer() {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
    		public void run() {
    			setVisible(true);
    		}
    	});
    }
	
	/**
	 * Visualizes the blobs being reported by the CMUCam2.
	 * 
	 * @param data The blob finder data that contains information about all of the blobs in the
	 * current field of view.
	 */
	public void visualizeBlobs(PlayerBlobfinderData data) {
		// Check to make sure we have valid data...
		if (data != null) {
			int numBlobs = data.getBlobs_count();
			int contextWidth = data.getWidth();
			int contextHeight = data.getHeight();
			Logger.log("Number of blobs: " + numBlobs + ", contextWidth=" + contextWidth + ", contextHeight=" + contextHeight);

			if(numBlobs > 0) {
				PlayerBlobfinderBlob[] blobListCopy = null;
				
				// Create a copy of the blobList array to avoid conflicts with the array reference
				// being changed while it is being read from.
				synchronized(data) {
					PlayerBlobfinderBlob[] blobList = data.getBlobs();
					blobListCopy = new PlayerBlobfinderBlob[blobList.length];
					System.arraycopy(blobList, 0, blobListCopy, 0, blobList.length);
				}
				
				blobPanel.addBlobs(contextWidth, contextHeight, blobListCopy);
			}
			else {
				Logger.logErr("No blobs present...");
				blobPanel.clearBlobs();
			}
		} else {
			Logger.logErr("Blob data is null...");
			blobPanel.clearBlobs();
		}
	}
	
    /**
     * Displays a graphic showing the blobs in the current field of view.
     * 
     * @author Chien-Liang Fok
     *
     */
    private class BlobPanel extends JComponent implements Runnable {
    	
		private static final long serialVersionUID = -8471920724257172768L;
    	
		private Vector<Blob> blobs = new Vector<Blob>();
		
    	public BlobPanel() {
    		setPreferredSize(new Dimension(500, 400));
    	}
    	
    	public void paint(Graphics g) {
    		// draw the field of view
    		Blob fov = getFOV();
    		g.setColor(Color.LIGHT_GRAY);
    		g.fillRect(fov.x, fov.y, fov.width, fov.height);
    		
    		// draw the blobs
    		synchronized(blobs) {
    			for (int i=0; i < blobs.size(); i++) {
    				Blob b = blobs.get(i);
    				
    				// Normalize the dimensions within field of view
    				int blobX = b.x * fov.width / b.contextWidth + fov.x;
    				int blobY = b.y * fov.height / b.contextHeight + fov.y;
    				int blobWidth = b.width * fov.width / b.contextWidth;
    				int blobHeight = b.height * fov.height / b.contextHeight;
    				
    				g.setColor(b.color);
    				g.fillRect(blobX, blobY, blobWidth, blobHeight);
    			}
    		}
    	}
    	
    	/**
    	 * Calculate the dimensions and location of the box representing
    	 * the field of view.
    	 * @return The field of view blob.
    	 */
    	private Blob getFOV() {
    		int contextWidth = getWidth();
    		int contextHeight = getHeight();
    		
    		int fovWidth = (int)(contextWidth * 0.75);
    		int fovHeight = (int)(contextHeight * 0.75);
    		int fovX = (contextWidth - fovWidth) / 2;
    		int fovY = (contextHeight - fovHeight) / 2;
    		
    		return new Blob(fovX, fovY, fovWidth, fovHeight, contextWidth, contextHeight, Color.LIGHT_GRAY);
    	}
    	
    	/**
    	 * Adds blobs to the panel.
    	 * 
    	 * @param width the FOV width.
    	 * @param height The FOV height.
    	 * @param blobList The list of blobs to add.
    	 */
    	public void addBlobs(int width, int height, PlayerBlobfinderBlob[] blobList) {
    		blobs.clear();
    		
    		if (blobList == null) {
    			Logger.log("WARNING: blobList is null");
    			return;
    		}
    		
    		for (int i=0; i < blobList.length; i++) {
    			PlayerBlobfinderBlob blob = blobList[i];
    			if (blob != null) {
    				
    				Logger.log("Blob[" + i + "] area=" + blob.getArea() + ", color=" + blob.getColor() 
    						+ ", left=" + blob.getLeft() + ", right=" + blob.getRight()
    						+ ", centroid x=" + blob.getX() + ", centroid y=" + blob.getY()
    						+ ", top=" + blob.getTop() + ", bottom=" + blob.getBottom());
    				
    				blobs.add(new Blob(blob.getLeft(), height - blob.getTop(), blob.getRight() - blob.getLeft(),
    						blob.getTop() - blob.getBottom(), width, height, new Color(blob.getColor())));
    			} else {
    				Logger.log("WARNING: blobList[" + i + "] is null, ignoring this blob");
    			}
    		}
    		
    		repaint();
    	}
    	
    	/**
    	 * Removes all of the blobs in the panel.
    	 */
    	public void clearBlobs() {
    		blobs.clear();
    		repaint();
    	}
    	
    	public void run() {
    		repaint();
    	}
    }
    
    /**
     * Contains the data for a visualization of a blob.
     * 
     * @author Chien-Liang Fok
     */
    private class Blob {
    	public Color color;
    	public int x, y, width, height;
    	public int contextWidth, contextHeight;
    	
    	/**
    	 * Constructor for a black blob.
    	 * 
    	 * @param x The x coordinate of upper-left coordinate.
    	 * @param y The y coordinate of upper-left coordinate.
    	 * @param width The width of the blob.
    	 * @param height The height of the blob.
    	 */
//    	public Blob(int x, int y, int width, int height, int contextWidth, int contextHeight) {
//    		this(x, y, width, height, contextWidth, contextHeight, Color.BLACK);
//    	}
    	
    	/**
    	 * Constructor for a blob with a custom color.
    	 * 
    	 * @param x The x coordinate of upper-left coordinate.
    	 * @param y The y coordinate of upper-left coordinate.
    	 * @param width The width of the blob.
    	 * @param height The height of the blob.
    	 * @param color The color of the blob.
    	 */
    	public Blob(int x, int y, int width, int height, int contextWidth, int contextHeight, Color color) {
    		this.x = x;
    		this.y = y;
    		this.width = width;
    		this.height = height;
    		this.contextWidth = contextWidth;
    		this.contextHeight = contextHeight;
    		this.color = color;
    	}
    }
    
//	private void log(String msg) {
//		String result = "BlobFinderVisualizer: " + msg;
//		System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
}
