package pharoslabut.util;

import java.util.*;

/**
 * Implements various methods that provides statistics on data sets.
 * 
 * @author Chien-Liang Fok
 */
public class Stats {
	
	/**
	 * 
	 * @param v A vector of doubles.
	 * @return The average value.
	 */
	public static double getAvg(Vector<Double> v) {
		double total = 0;
		Enumeration<Double> e = v.elements();
		while (e.hasMoreElements()) {
			total += e.nextElement();
		}
		return total / v.size();
	}
	
	/**
	 * 
	 * @param v A vector of doubles.
	 * @return The minimum value.
	 */
	public static double getMin(Vector<Double> v) {
		double result = Double.MAX_VALUE;
		Enumeration<Double> e = v.elements();
		while (e.hasMoreElements()) {
			double currVal = e.nextElement();
			if (currVal < result)
				result = currVal;
		}
		return result;
	}
	
	/**
	 * 
	 * @param v A vector of doubles.
	 * @return The maximum value.
	 */
	public static double getMax(Vector<Double> v) {
		double result = Double.MIN_VALUE;
		Enumeration<Double> e = v.elements();
		while (e.hasMoreElements()) {
			double currVal = e.nextElement();
			if (currVal > result)
				result = currVal;
		}
		return result;
	}
	
	/**
	 * 
	 * @param v A vector of doubles.
	 * @return The population standard deviation.
	 */
	public static double getSampleStdDev(Vector<Double> v) {
		double avg = getAvg(v);
		
		double diffSqrd = 0;
		Enumeration<Double> e = v.elements();
		while (e.hasMoreElements()) {
			double currVal = e.nextElement();
			diffSqrd += Math.pow(currVal - avg, 2);
		}
		
		return Math.sqrt(diffSqrd/(v.size()-1));
	}
	
	/**
	 * Calculates the 95% confidence interval of a population.
	 * 
	 * @param stdev The population standard deviation.
	 * @param popSize The population size.
	 * @return The 95% confidence interval.
	 */
	public static double getConf95(double stdev, int popSize) {
		return stdev / Math.sqrt(popSize) * 1.96;	
	}
}
