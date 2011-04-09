package pharoslabut;
import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import javax.imageio.ImageIO;


public class MapReader {

	/**
	 * @param args
	 */
	
	public static int[][] ReadImage(BufferedImage image){
		int width,height;
		width = image.getWidth();
		height = image.getHeight();
		int [][] map = new int[width][height];
		for (int w = 0; w<width; w++){
			for(int h = 0; h<height; h++){
				if(image.getRGB(w,h) == -1){
					map[w][h] = 1;								//white(path)
				}else{
					map[w][h] = 0;								//black(obstacle)
				}
//				System.out.println(map[w][h]+" "+w+" "+h);
			}
		}
		return map;
	}
	
	public static Node[][] CreateNode(int [][] map){
		int width = map.length;
		int height = map[0].length;
		Node[][] nodemap = new Node[width][height];
		for(int w=0;w<width;w++)
			for(int h=0;h<height;h++)
				nodemap[w][h] = new Node();
		for(int w=0;w<width;w++){
			for(int h=0;h<height;h++){		
				nodemap[w][h].setCoordinate(w, h);
				nodemap[w][h].setObstacle(map[w][h]);
				int tempu, tempd, templ, tempr;
				tempu = Math.abs(h-1);
				tempd = (h+1)%(height);
				templ = Math.abs(w-1);
				tempr = (w+1)%(width);
				nodemap[w][h].setNeighbor(nodemap[w][tempu], nodemap[w][tempd], nodemap[templ][h], nodemap[tempr][h]);
				if(w==0)
					nodemap[w][h].setNeighbor(nodemap[w][h].getUp(), nodemap[w][h].getDown(), null,nodemap[w][h].getRight());
				if(w==(width-1))
					nodemap[w][h].setNeighbor(nodemap[w][h].getUp(), nodemap[w][h].getUp(), nodemap[w][h].getLeft(),null);
				if(h==0)
					nodemap[w][h].setNeighbor(null, nodemap[w][h].getDown(), nodemap[w][h].getLeft(), nodemap[w][h].getRight());
				if(h==(height-1))
					nodemap[w][h].setNeighbor(nodemap[w][h].getUp(), null, nodemap[w][h].getLeft(),nodemap[w][h].getRight());
				if(nodemap[w][h].getRight()!=null&&nodemap[w][h].getUp()!=null)
					nodemap[w][h].setCorner(nodemap[w+1][h-1], nodemap[w][h].getUpleft(), nodemap[w][h].getBottomright(), nodemap[w][h].getBottomleft());
				if(nodemap[w][h].getLeft()!=null&&nodemap[w][h].getUp()!=null)
					nodemap[w][h].setCorner(nodemap[w][h].getUpright(), nodemap[w-1][h-1], nodemap[w][h].getBottomright(), nodemap[w][h].getBottomleft());
				if(nodemap[w][h].getRight()!=null&&nodemap[w][h].getDown()!=null)
					nodemap[w][h].setCorner(nodemap[w][h].getUpright(), nodemap[w][h].getUpleft(), nodemap[w+1][h+1], nodemap[w][h].getBottomleft());
				if(nodemap[w][h].getLeft()!=null&&nodemap[w][h].getDown()!=null)
					nodemap[w][h].setCorner(nodemap[w][h].getUpright(), nodemap[w][h].getUpleft(), nodemap[w][h].getBottomright(), nodemap[w-1][h+1]);
			}
		}
		return nodemap;
	}
	
/*	public static void main(String[] args) {
		// TODO Auto-generated method stub
			BufferedImage image = null;
			try{
				image = ImageIO.read(new File("test.bmp"));
			}
			catch(IOException e){}
			int [][] map = ReadImage(image);
			Node [][] nodemap = CreateNode(map);
			System.out.println("Width:"+" "+nodemap.length);
			System.out.println("Height:"+" "+nodemap[0].length);
			return;
	}
	
*/

}
