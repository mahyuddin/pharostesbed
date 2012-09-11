package pharoslabut.util;

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Encapsulates the average and 95% confidence interval of a population of doubles.
 * 
 * @author Chien-Liang Fok
 */
public class AverageStatistic {
	/**
	 * The average value.
	 */
	private double average;
	
	/**
	 * The 95% confidence interval.
	 */
	private double conf95;
	
	/**
	 * The number of decimal places to print.
	 */
	private int numDecimalPlaces = 2;
	
	public AverageStatistic(double average, double conf95, int numDecimalPlaces) {
		this(average, conf95);
		this.numDecimalPlaces = numDecimalPlaces;
	}
	
	public AverageStatistic(double average, double conf95) {
		this.average = average;
		this.conf95 = conf95;
	}
	
	public AverageStatistic(Vector<Double> population) {
		this.average = Stats.getAvg(population);
		this.conf95 = Stats.getConf95(population);
	}
	
	public double getAverage() {
		return average;
	}
	
	public double getConf95() {
		return conf95;
	}
	
	public String toString() {
		String format = "#.";
		for (int i=0; i < numDecimalPlaces; i++) {
			format += "#";
		}
		
		DecimalFormat df = new DecimalFormat(format);
		
		return df.format(average) + " ± " + df.format(conf95);
	}
}
