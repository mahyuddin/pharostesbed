package pharoslabut.util;

import java.util.*;

public class Stats {
	
	public static double getAvg(Vector<Double> v) {
		double total = 0;
		Enumeration<Double> e = v.elements();
		while (e.hasMoreElements()) {
			total += e.nextElement();
		}
		return total / v.size();
	}
	
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
