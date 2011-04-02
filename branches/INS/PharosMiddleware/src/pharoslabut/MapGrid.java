package pharoslabut;
public class MapGrid {

	int [][] testmap;
		
	public int grid_size;
	public int map_size;
	
	public MapGrid(int [][] n_testmap, int n_grid_size, int n_map_size)
	{
		testmap = n_testmap;
		grid_size = n_grid_size;
		map_size = n_map_size;
	}
	
	public void PrintGrid()
	{
		int x, y;
		for (y = grid_size-1; y >= 0; y--)
		{
			for (x = 0; x < grid_size; x++)
				System.out.print(testmap[y][x]);
			System.out.println();
		}
	}
	
}