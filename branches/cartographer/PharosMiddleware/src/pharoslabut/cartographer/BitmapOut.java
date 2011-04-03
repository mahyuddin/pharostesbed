package pharoslabut.cartographer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class BitmapOut {
	public static final int	TYPE_3BYTE_BGR = 5;
	BufferedImage image;
	public static final int [] grayscale = {0x00ffffff,0x00e0e0e0,0x00c4c4c4,0x00a8a8a8,0x008c8c8c,
											0x00707070,0x00545454,0x00383838,0x001c1c1c,0x0000000};
	
	

	/*public BitmapOut(int x, int y){
		int a = 0;
		image = new BufferedImage(x*5,y*5,TYPE_3BYTE_BGR);
		for (int i = 0; i < image.getHeight(); i+=5){
			for (int j = 0; j < image.getWidth(); j+=5) {
				for(int k = i; k < i+5; k++){
					for(int l=j; l < j+5; l++ ){
						image.setRGB(k, l, grayscale[a%10]);						
					}
					a++;
				}
			}
		}
	}*/
	/**BitmapOut Constructor
	 * 
	 * @param x width of world
	 * @param y height of world
	 * @throws IOException 
	 */	
	public BitmapOut(int x, int y) throws IOException{
		image = new BufferedImage(x*5,y*5,TYPE_3BYTE_BGR);
		for (int i = 0; i < image.getHeight(); i+=5){
			for (int j = 0; j < image.getWidth(); j+=5) {
				for(int k = i; k < i+5; k++){
					for(int l=j; l < j+5; l++ ){
						int index;
						if ((int)WorldView.readConfidence(i,j) == 1){
							index = 9;
						}
						else{
							index = (int)(WorldView.readConfidence(i,j)*10);
						}
						image.setRGB(k, l, grayscale[index]);						
					}
				}
			}
		}
		ImageIO.write(this.image, "BMP", new File("map.bmp"));
	}
}

