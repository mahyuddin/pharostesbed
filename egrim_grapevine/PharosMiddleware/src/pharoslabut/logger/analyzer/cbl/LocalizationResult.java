package pharoslabut.logger.analyzer.cbl;


/**
 * Contains the results of the localization algorithm.
 * This is returned by Driver.Pharos2_range(...).
 * 
 * @see Driver
 */
public class LocalizationResult {

    double[][] Xhat; // Xhat[i][j] = estimated X coordinate for robot i at time step j
    double[][] Yhat; // Yhat[i][j] = estimated Y coordinate for robot i at time step j
    double[][] Zhat; // Zhat[i][j] = estimated Z coordinate for robot i at time step j
    double[] Xstart; // Xstart[i] = 
    double score;
    boolean relative_frame;

    public LocalizationResult(int nRobots, int nTimesteps, int nVariables) {
        Xhat = new double[nRobots][nTimesteps];
        Yhat = new double[nRobots][nTimesteps];
        Zhat = new double[nRobots][nTimesteps];
        
        /*
         * Liang's Question: Why also store the Xstart array when all we need is the estimated
         * locations of the robots?  Is it because Xstart also contains the estimated heading?
         * In that case, are we storing the estimated locations twice, once in the variables
         * above and once in Xstart below?
         */
        Xstart = new double[nVariables];
    }
}
