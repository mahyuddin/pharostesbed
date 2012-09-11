package pharoslabut.demo.autoIntersection.server.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import pharoslabut.demo.autoIntersection.LaneSpecs;
import pharoslabut.demo.autoIntersection.server.AutoIntersectionServer;

public class IntersectionPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = -3902232490372498704L;
	private static final int carWidth = 90;
	private static final int carHeight = 50;
	private BufferedImage intersectionImage;
	private static Set<Car> carsApproaching = new HashSet<Car>();
	private static Set<Car> carsExiting = new HashSet<Car>();
	Thread myThread;
	
	public IntersectionPanel() {
	    setBackground(Color.BLACK);
		myThread = new Thread(this);
		myThread.start();
	}
	
	public Dimension getPreferredSize() {
	    return new Dimension((int) (IntersectionGUI.frameHeight*0.7), (int) (IntersectionGUI.frameHeight*0.7));
	}
	
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		if( (AutoIntersectionServer.getNWays()==4)  &&  (AutoIntersectionServer.getNLanes()==2) ) {
			paint2L4WGUI();
		}
		g2.drawImage(intersectionImage, 0, 0, this.getPreferredSize().width, this.getPreferredSize().height, null);
		
		Iterator<Car> iterator = carsApproaching.iterator();
        while(iterator. hasNext())
        {
            Car car = iterator.next();
            g2.drawImage(car.getCarImage(), car.getX(), car.getY(), carWidth, carHeight, null);
        }
        iterator = carsExiting.iterator();
        while(iterator. hasNext())
        {
            Car car = iterator.next();
            g2.drawImage(car.getCarImage(), car.getX(), car.getY(), carWidth, carHeight, null);
        }
	}
	
	public void paint2L4WGUI() {
		try {                
            intersectionImage = ImageIO.read(new File("images/2lane_4way_intersection.jpg"));
         } catch (IOException ex) {
              System.out.println("ERROR: image not found");
         }
	}
	
	public static void approachingCar(InetAddress ipAddress, LaneSpecs laneSpecs) {
		//TODO take the lane specs and conver it to start xposition and start yposition and car orientation
		Car car = new Car(ipAddress, 0, 186);
		if(! carsApproaching.contains(car) ) {
			car.setID(carsApproaching.size());
			carsApproaching.add(car);
	//		car.setID( carsApproaching. .indexOf(car) );
			try {                
				BufferedImage CarImage = ImageIO.read(new File("images/car-" + car.getID() + ".png"));
				car.setCarImage(CarImage);
	         } catch (IOException ex) {
	              System.out.println("ERROR: image not found");
	         }
		}
	}
	
	public static void exitingCar(InetAddress ipAddress) {
		System.out.println("Cars Approaching" + carsApproaching);
		System.out.println("Cars Exiting" + carsExiting);
		Car tempCar = new Car(ipAddress);
		Iterator<Car> iterator = carsApproaching.iterator();
        while(iterator. hasNext())
        {
        	Car car = iterator.next();
        	if( car.equals(tempCar) ) {
        		carsExiting.add(car);
        		tempCar = car;
        		break;
        	}
        }
        carsApproaching.remove(tempCar);
        
        System.out.println("Cars Approaching" + carsApproaching);
		System.out.println("Cars Exiting" + carsExiting);
	}
	
	public void run() {
		while(true) {
			try{
				if(! carsApproaching.isEmpty() ) {
					Iterator<Car> iterator = carsApproaching.iterator();
			        while(iterator. hasNext())
			        {
			            Car car = iterator.next();
			            if( car.getX() < 80 ) {
			            	car.incrementX();
			            }
			            repaint();
			            Thread.sleep(30);
			        }
				}
				if(! carsExiting.isEmpty() ) {
					Car removeCar = null;
					boolean removeFlag = false;
					Iterator<Car> iterator = carsExiting.iterator();
			        while(iterator. hasNext())
			        {
			            Car car = iterator.next();
			            car.incrementX();
			            if( car.getX() > 400 ) {
			            	removeCar = car;
			            	removeFlag = true;
			            }
			            repaint();
			            Thread.sleep(10);
			        }
			        if( removeFlag ) {
			        	removeFlag = false;
			        	carsExiting.remove(removeCar);
			        	removeCar = null;
			        }
				} else {
					System.out.print("");
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}