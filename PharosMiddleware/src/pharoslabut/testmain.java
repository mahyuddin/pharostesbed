package pharoslabut;
public class testmain {
	
	public static void main(String[] args) {
		
		boolean [][] testmap = 
		{   { true , true , true , true , true , true , true , true , true , true , true , true , true , true , true , true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , false, false, false, false, false, false, false, false, false, false, false, false, false, false, true },
			{ true , true , true , true , true , true , true , true , true , true , true , true , true , true , true , true } 
		};
			
		Mapping map  = new Mapping();
		map.map = testmap;
		map.Width = 16;
		map.Height = 16;
		PathFind pf  = new PathFind(map,1);
		pf.A_path(1, 1, 4, 4, 0);
		pf.result.printPath();
		pf.result.printMov();
	}

}
