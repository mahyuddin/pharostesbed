package pharoslabut.util;

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
		return average + " ± " + conf95;
	}
}
