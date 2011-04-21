package pharoslabut.cartographer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class BitmapOut {
	public static final int	TYPE_3BYTE_BGR = 5;
	BufferedImage image,bigimage,sampleimage,bigsampleimage;
	public static final int [] grayscale = {0x00ffffff,0x00e0e0e0,0x00c4c4c4,0x00a8a8a8,0x008c8c8c,
											0x00707070,0x00545454,0x00383838,0x001c1c1c,0x0000000, 
											0x002338BF};	// blue, for frontier
	
	/**BitmapOut Constructor
	 * 
	 * @param x width of world
	 * @param y height of world
	 * @throws IOException 
	 * @author Aaron Chen
	 */	
	public BitmapOut(int x, int y) throws IOException{
		image = new BufferedImage(x,y,TYPE_3BYTE_BGR);
		bigimage = new BufferedImage(5*x,5*y,TYPE_3BYTE_BGR);
		sampleimage = new BufferedImage(x,y,TYPE_3BYTE_BGR);
		bigsampleimage = new BufferedImage(5*x,5*y,TYPE_3BYTE_BGR);
		for (int i = 0; i < x; i++){
			for (int j = y; j > 0; j--) {
						int index;
						if (((WorldView.sampleworld.get(i)).get(y-j)).getConfidence() == 1){
							index = 9;
						}
						else{
							index = (int)(((WorldView.sampleworld.get(i)).get(y-j)).getConfidence()*10);
						}
						//System.out.println(i + "," + j);
						sampleimage.setRGB(i, j-1, grayscale[index]);						
			}
		}
		for (int i = 0; i < bigsampleimage.getHeight(); i+=5){
			for (int j = 0; j < bigsampleimage.getWidth(); j+=5) {
				for(int k = i; k < i+5; k++){
					for(int l=j; l < j+5; l++ ){
						bigsampleimage.setRGB(k, l, sampleimage.getRGB(i/5, j/5));						
					}
				}
			}
		}
		ImageIO.write(this.bigsampleimage, "BMP", new File("samplemap.bmp"));
		
		for (int i = 0; i < x; i++){
			for (int j = y; j > 0; j--) {
						int index;
						if ((int)WorldView.readConfidence(i,y-j) == 1){
							index = 9;
						}
						else{
							index = (int)(WorldView.readConfidence(i,y-j)*10);
						}
						if ((WorldView.world.get(i)).get(y-j).getFrontier()){
							index = 10;	// blue for frontier
						}
						if ((WorldView.world.get(i)).get(y-j).getPath()){
							index = 10;	// blue for Path
						}
						//System.out.println(i + "," + j);
						image.setRGB(i, j-1, grayscale[index]);						
								}
		}
		for (int i = 0; i < bigimage.getHeight(); i+=5){
			for (int j = 0; j < bigimage.getWidth(); j+=5) {
				for(int k = i; k < i+5; k++){
					for(int l=j; l < j+5; l++ ){
						bigimage.setRGB(k, l, image.getRGB(i/5, j/5));						
					}
				}
			}
		}
		ImageIO.write(this.bigimage, "BMP", new File("map.bmp"));
	}
	public void draw(Graphics g, int x, int y) {
        g.drawImage(image, x, y, null);
    }
}

