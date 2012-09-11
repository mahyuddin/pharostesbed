package pharoslabut.util;

/**
 * Shared methods for generating random values.
 * 
 * @author Chien-Liang Fok
 */
public class Random {

	/**
     * Generates a random value between minPeriod and maxPeriod.
     * 
     * @param minPeriod The minimum period.
     * @param maxPeriod The maximum period.
     * @return a random value between minPeriod and maxPeriod.
     */
    public static long randPeriod(long minPeriod, long maxPeriod) {
    	long diff = maxPeriod - minPeriod;
    	double random = Math.random();
    	return (long)(random * diff + minPeriod);
    }
}
