package pharoslabut.demo.autoIntersection.server.GUI;

import java.awt.image.BufferedImage;
import java.net.InetAddress;

/**
 * @author Maykel Hanna
 *
 */
public class Car {
	private static final long serialVersionUID = 3036433447549591215L;
	
	private BufferedImage carImage;
	public InetAddress ipAddress;
	private int ID;
	public int xPosition;
	public int yPosition;
	
	public Car(InetAddress ipAddress, int xPosition, int yPosition) {
		this.ipAddress = ipAddress;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
	}
	
	public Car(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public BufferedImage getCarImage() {
		return this.carImage;
	}
	
	public void setCarImage(BufferedImage image) {
		this.carImage = image;
	}
	
	public int getID() {
		return this.ID;
	}
	
	public void setID(int id) {
		this.ID = id;
	}
	
	public int getX() {
		return this.xPosition;
	}
	
	public int getY() {
		return this.yPosition;
	}
	
	public void incrementX() {
		xPosition += 1;
	}
	
	public void incrementY() {
		yPosition += 1;
	}
	
	public void decrementX() {
		xPosition -= 1;
	}
	
	public void decrementY() {
		yPosition -= 1;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
    		return false;
    	if(! (o instanceof Car) )
    		return false;
    	
    	Car car = (Car) o;
    	return this.ipAddress.equals(car.ipAddress);
	}
	
	@Override
	public String toString() {
		return "\t" + "IP:" + ipAddress + "\t" + "ID:" + ID + "\t" + "X:" + xPosition + "\t" + "Y:" + yPosition;
	}
}
