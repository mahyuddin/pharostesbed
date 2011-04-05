package pharoslabut;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;



public class Mapping {

	public int Width = -1;
	public int Height = -1;
	public boolean map[][];
	
	public void parse(File ascii) throws IOException {
		// TODO Auto-generated method stub
		int y = 0;
		int x = 0;
		int w = 1000;
		int h = 1000;
		char c = 0;
		BufferedReader in = new BufferedReader(new FileReader(ascii));
		Writer output = new BufferedWriter(new FileWriter("test.txt"));
		
		map = new boolean[w][h];
		while(c != (char) -1)
		{
			c = (char)in.read();
			if(c == ' ')
			{
				map[y][x] = false;
				output.write("0");
				x++;
			}
			else if(c == '\n')
			{
				map[y][x] = true;
				output.write("\r\n");
				Width = x;
				x=0;
				y++;
			}
			else
			{
				map[y][x] = true;
				output.write("1");
				x++;
			}
		}
		Height = y;
		output.close();
		in.close();
	}
}
