package pharoslabut;






import java.io.File;
import java.io.IOException;

public class testmain {
	
	public static void main(String[] args) throws IOException {
		
		File ascii_map = new File("/home/danny/demo.txt");	
		Mapping map  = new Mapping();
		map.parse(ascii_map);
		map.printMap();
		PathFind pf  = new PathFind(map,1);
		pf.A_path(1, 1, 8, 8, 0);
		pf.result.printPath();
		pf.result.printMov();
	}

}