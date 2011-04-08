package pharoslabut;                                                            
                                                                     
                                                                     
                                             

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;


public class Mapping {

	public int Width = -1;
	public int Height = -1;
	public boolean map[][];
	public ArrayList<StringBuffer> MapBuffer;
	
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
				Width = x-1;
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

	public void createBuffer()
	{
		int row, col;
		StringBuffer lineBuf;
		MapBuffer = new ArrayList<StringBuffer> (Height);
		
		for(col=Height-1; col>=0; col--)
		{
			lineBuf = new StringBuffer();
			for (row=0; row<Width; row++)
			{
				if(map[col][row]) lineBuf.append('#');
				else lineBuf.append(' ');
			}
			MapBuffer.add(lineBuf);
		}
		System.out.println("Width: "+Width+" Height: "+Height);
		
	}

	public void printBuffer(NewJFrame framer)
	{
		int row;
		for(row = Height -1; row >= 0; row--)
		{
			//framer.UpdateMap(MapBuffer.get(row).toString());
			System.out.println(MapBuffer.get(row).toString());
		}
		
	}
	
	public void printMap()
	{
		int col, row;
		String str;
		for(col=Height-1; col>=0; col--)
		{
			str = new String();
			for (row=0; row<Width; row++)
			{
				if(map[col][row]) str = str+"#";
				else str = str+" ";
			}
			System.out.println(str);
		}
		System.out.println("Width: "+Width+" Height: "+Height);
	}

	public void bufAltChar(int col, int row, char c)
	{
		StringBuffer linebuf = MapBuffer.get(row);
		linebuf.setCharAt(col, c);
	}
}