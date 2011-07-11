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
}
