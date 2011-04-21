package pharoslabut.cartographer;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.IOException;

public class RealtimeImgOut extends Thread {

	public static Frame frame;
	public static MyCanvas canvas;
	
	static public void main(String[] args) {
		new RealtimeImgOut();
	}
	
	
	public RealtimeImgOut() {
		frame = new Frame("Map Output");
		canvas = new MyCanvas();
		frame.add(canvas);
		frame.setSize(5*WorldView.WORLD_SIZE, 5*WorldView.WORLD_SIZE); // use worldview size
		frame.setVisible(true);
		
	}
	
	public static void refreshFrame(){
		canvas.redrawMap();
	}

	class MyCanvas extends Canvas {
		BitmapOut bit;
		Component c;
		MyCanvas() {
			// Add a listener for resize events
			addComponentListener(new ComponentAdapter() {
				// This method is called when the component's size changes
				public void componentResized(ComponentEvent evt) {
					c = (Component)evt.getSource();

					// Get new size
					Dimension newSize = c.getSize();

					// Regenerate the image
					try {
						bit = new BitmapOut(WorldView.WORLD_SIZE,WorldView.WORLD_SIZE);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//mandelbrot = new Mandelbrot(newSize.width, newSize.height);
					c.repaint();
				}
			});
		}
		
		void redrawMap(){
			try {
				bit = new BitmapOut(WorldView.WORLD_SIZE,WorldView.WORLD_SIZE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//mandelbrot = new Mandelbrot(newSize.width, newSize.height);
			c.repaint();
			
			
		}
		
		

		public void paint(Graphics g) {
			if(bit != null)
				bit.draw(g, 0, 0);
		}
	}

	@Override
	public void run() {
		while (true) {
			refreshFrame();
			// call refresh img fctn here
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}

