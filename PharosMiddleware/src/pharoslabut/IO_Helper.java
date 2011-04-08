package pharoslabut;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/***
 * Assignment 1 - If you are not on this assignment you have the wrong file.
 * 
 * DO NOT MODIFY THIS FILE! Contact the TAs if you feel you have found an error.
 */
public class IO_Helper {

	/***
	 * This function reads the input from the file location given by the first
	 * command line argument to your assignment template program ProgramX.java.
	 * 
	 * The object types of the input are determined by what we provide as input.
	 * For example, FIELD X comment sections below will give the type and format
	 * example of each input.
	 * 
	 * ************** Format Example ************** 
	 * x1 y1 
	 * x2 y2 
	 * ...
	 * 
	 * The returned object will be a List of Lists. You can think of the outer
	 * list as being the "rows" and the inner lists as being the "columns".
	 * 
	 * @param inputFilePath
	 * @return
	 * @throws FileNotFoundException
	 */
	public static List<Double> readInput(String inputFilePath)
			throws FileNotFoundException {
		File inputFile = new File(inputFilePath);
		if (!inputFile.exists()) {
			throw new FileNotFoundException(
					inputFilePath
							+ " does not exist! Please double check your command line inputs and verify the file does exist.");
		}

		List<Double> input = new LinkedList<Double>();

		// read line-by-line
		// fields expected to be separated by whitespace
		Scanner s = new Scanner(inputFile);
		while (s.hasNextLine()) {
			// check for skipping empty lines
			Scanner ss = new Scanner(s.nextLine());
			if (!ss.hasNext()) {
				continue; // skip blank lines
			}

			/*
			 * get element
			 */
			double x = ss.nextDouble();
		
			input.add(x);
		}

		return input;
	}
	
	
	


	/***
	 * This function writes the output to the file location given by the second
	 * command line argument to your assignment template program ProgramX.java.
	 * 
	 * The output format is again a List of Lists, meaning for example that each
	 * column of a line goes into a List for that row, and each line itself is
	 * store in some master List that stores all rows.
	 * 
	 * ************** Format Example ************** 
	 * x1 y1 
	 * x2 y2 
	 * ...
	 * 
	 * WARNING: If a file exists at outputFilePath, it will be deleted!
	 * 
	 * @param outputFilePath
	 * @param output
	 * @throws IOException
	 *
	public static void writeOutput(String outputFilePath,
			List<List<Integer>> output) throws IOException {
		// create a new clean file
		File outputFile = new File(outputFilePath);
		if (outputFile.exists()) {
			outputFile.delete();

		} else {
			outputFile.createNewFile();
		}

		// write output
		FileWriter fstream = new FileWriter(outputFile);
		BufferedWriter out = new BufferedWriter(fstream);
		for (List<Integer> line : output) {
			for (Object col : line) {
				out.append(col + " "); // output column followed by one space
			}
			out.newLine(); // output newline
		}
		out.close();
	}
	
	*/
}

