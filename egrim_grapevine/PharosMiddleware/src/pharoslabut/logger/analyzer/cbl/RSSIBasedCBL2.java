package pharoslabut.logger.analyzer.cbl;

import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.Location;

/**
 * Performs connectivity-based localization using beacons to determine connectivity
 * and RSSI to determine range.
 * 
 * @author Chien-Liang Fok
 */
public class RSSIBasedCBL2 {
	private GroundTruth2 groundTruth;
	private FileLogger outFlogger = null;
	//private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 */
	public RSSIBasedCBL2(GroundTruth2 groundTruth, FileLogger outFlogger, FileLogger flogger) {
		this.groundTruth = groundTruth;
		this.outFlogger = outFlogger;
		doCBL();
	}
	
	private void doCBL() {
		int nRobots = groundTruth.getNumRobots();
		int nTimesteps = groundTruth.getNumTimeSteps();
		
		// TEST CODE: Get the true distances
		// based on GPS.
		double[][][] pairwiseDistGPS = new double[nRobots][nRobots][nTimesteps];
		Utils.coords2pairdist(nRobots, nTimesteps, groundTruth.getLongitudeTruth(), groundTruth.getLatitudeTruth(), 
				groundTruth.getElevationTruth(), pairwiseDistGPS);
		
		
		// Compute the pairwise distances based on RSSI values.
		double[][][] pairwiseDist = new double[nRobots][nRobots][nTimesteps];
		for (int t = 0; t <  nTimesteps; t++) {
            for (int i = 0; i < nRobots - 1; i++) {
                for (int j = i + 1; j < nRobots; j++) {
                    pairwiseDist[i][j][t] = groundTruth.getRSSIDistGlobal(i, j, t);
                    log("pairwiseDist[" + i + "][" + j + "][" + t + "] = " + pairwiseDist[i][j][t], outFlogger);
                    
                    
                    // TEST CASE:
                    // If we got an estimate, replace it with the actual distance
                    // based off of GPS...
                    //if (pairwiseDist[i][j][t] != -1) {
                    //	pairwiseDist[i][j][t] = pairwiseDistGPS[i][j][t];
                    //}
                    
                }
            }
        }
		
		
		// TEST CODE: Determine the error of the RSSI-based distances vs. the true distances
		// based on GPS.
		int rejectCntr = 0;
		int totalCntr = 0;
		for (int t = 0; t <  nTimesteps; t++) {
            for (int i = 0; i < nRobots - 1; i++) {
                for (int j = i + 1; j < nRobots; j++) {
                	if (pairwiseDist[i][j][t] != -1) {
            
                		double error = pairwiseDistGPS[i][j][t] - pairwiseDist[i][j][t];
                		log("DistanceError[" + i + "][" + j + "][" + t + "] = " + error, outFlogger);
                	} else {
                		log("DistanceError[" + i + "][" + j + "][" + t + "] = N/A", outFlogger);	
                	}
//                    if (Math.abs(error) > 6) { 
//                    	pairwiseDist[i][j][t] = -1; // remove all "bad" data from pairwise distance matrix.
//                    	rejectCntr++;
//                    }
                    totalCntr++;
                }
            }
        }
		log("Rejected " + rejectCntr + " data points out of " + totalCntr, outFlogger);
		
		// compute all odometry from true coordinates
		double[][] odomDist = new double[nRobots][nTimesteps];
		double[][] odomThet = new double[nRobots][nTimesteps];
		Utils.coords2odom(nRobots, nTimesteps, 
				groundTruth.getLongitudeTruth(), groundTruth.getLatitudeTruth(), odomDist, odomThet);
		
		// create 2D arrays posArrX,posArrY,posArrZ
		double[][] posArrX = new double[nRobots][groundTruth.getNumTimeSteps()];
		double[][] posArrY = new double[nRobots][groundTruth.getNumTimeSteps()];
		double[][] posArrZ = new double[nRobots][groundTruth.getNumTimeSteps()];
		for (int i = 0; i < nRobots; i++) {
			for (int t = 0; t < groundTruth.getNumTimeSteps(); t++) {
				posArrX[i][t] = -1; // unknown XY-location
				posArrY[i][t] = -1; // unknown XY-location
				posArrZ[i][t] = 0; // elevation of all robots assumed to be the same
			}
		}
		
		int max_trials = 100;
		double tol0 = 0;
		LocalizationResult result = Driver.Phase2_range(
				groundTruth.getNumRobots(), 		// Number of robots
				groundTruth.getNumTimeSteps(), 		// Number of time steps
				groundTruth.getRobotType(), 		// RobotType[i] = type of robot i
				pairwiseDist, 	// pairwiseDist[i][j][t] is the distance between robots i and j at time t
				odomDist, 		// odomDist[i][t] = distance robot i moved between times t-1 and t, where i=0...numRobots-1 and t=0...nTimesteps-1 (odomDist[i][0] = 0)
				odomThet, 		// odomTheta[i][t] = angle of robot i at time t, where i=0...numRobots-1 and t=0...numTimesteps-1  (all angles are normalized to odomTheta[i][1], meaning odomTheta[i][1]=0)
				posArrX,		// posArrX[i][t] = the X coordinate of robot i at time t, -1 if unknown
				posArrY, 		// posArrY[i][t] = the Y coordinate of robot i at time t, -1 if unknown
				posArrZ,		// posArrZ[i][t] = the Z coordinate of robot i at time t (this is always known)
				max_trials, 	// The maximum number of optimization iterations (ensures optimizer does not run forever).
				tol0,			// The error threshold.  Once the estimated error becomes less than tol0, return.
				outFlogger
		);
		
		//
		// 3. Registration of coordinate system via procrustes
		//

		// 1D arrays to hold result of procrustes
		double[] Xhat = new double[nRobots * nTimesteps];
		double[] Yhat = new double[nRobots * nTimesteps];
		for (int i = 0; i < nRobots; i++) {
			for (int t = 0; t < nTimesteps; t++) {
				Xhat[i * nTimesteps + t] = result.Xhat[i][t];
				Yhat[i * nTimesteps + t] = result.Yhat[i][t];
			}
		}

		int[] robotIDs = groundTruth.getRobotIDs();
		double[] Xtrue = groundTruth.getLongitudeTruth();
		double[] Ytrue = groundTruth.getLatitudeTruth();
		double[] Ztrue = groundTruth.getElevationTruth();
		
		// Procrustes fits via translation, scaling, rotation, and reflection
		// Xhat, Yhat to Xtrue, Ytrue such that the L2-norm is minimized.
		procrustes2D_transform p = Utils.procrustes2D(nRobots * nTimesteps, Xtrue, Ytrue, Xhat, Yhat); // in-place


		// print results
		double error = 0;
		double errorM = 0;
		double dx, dy;
		
		log("Results: (localization score=" + result.score + ")", outFlogger);
		log("  ", outFlogger);
		
		for (int i = 0; i < nRobots; i++) {
			for (int t = 0; t < nTimesteps; t++) {
				log("Robot " + robotIDs[i] + " (floor " + ((int) Ztrue[i * nTimesteps + t]) + ") | Time " + t + " |  xtrue=" + (Xtrue[i * nTimesteps + t])
						+ " ytrue=" + (Ytrue[i * nTimesteps + t]) + " | xhat=" + (Xhat[i * nTimesteps + t])
						+ " yhat=" + (Yhat[i * nTimesteps + t]), outFlogger);
				dx = Xtrue[i * nTimesteps + t] - Xhat[i * nTimesteps + t];
				dy = Ytrue[i * nTimesteps + t] - Yhat[i * nTimesteps + t];
				error += Math.sqrt(dx * dx + dy * dy);
				
				Location trueLoc = new Location(Ytrue[i * nTimesteps + t], Xtrue[i * nTimesteps + t]);
				Location estLoc = new Location(Yhat[i * nTimesteps + t], Xhat[i * nTimesteps + t]);
				errorM += trueLoc.distanceTo(estLoc);
			}
		}
		log("\nEstimation error: " + error + " sum-of-squares (" + errorM + "m)", outFlogger);
	}
	
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
//	private void log(String msg) {
//		boolean isDebugStmt = false;
//		GPSBasedCBL.log(msg, this.flogger, isDebugStmt);
//	}
	
//	private static void log(String msg, FileLogger flogger, boolean isDebugStmt) {
//		String result = "ExtractTrueLocation: " + msg;
//		if (!isDebugStmt || System.getProperty ("PharosMiddleware.debug") != null) 
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
	
//	private static void print(String msg, FileLogger flogger) {
//		System.out.println(msg);
//		if (flogger != null) {
//			flogger.log(msg);
//		}
//	}

	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage(String msg) {
		System.setProperty ("PharosMiddleware.debug", "true");
		print(msg);
		usage();
	}

	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.logger.analyzer.cbl.RSSIBasedCBL <options>\n");
		print("Where <options> include:");
		print("\t-expDir <experiment data directory>: The directory containing experiment data (required)");
		print("\t-output <output file>: The file in which to save the results (required)");
		print("\t-timeStepSize <time step size>: The length of the time step in ms (default 10000)");
		print("\t-numTimeSteps <number of time steps>: The number of time steps (default 50)");
		print("\t-range <range>: The maximum detectable inter-robot distance in meters (default infinity)");
		print("\t-log <log file name>: The file in which to log debug statements (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		String outFile = null;
		FileLogger flogger = null; // for saving debug output
		long timeStepSize = 10000;
		int numTimeSteps = 50;
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-log"))
					flogger = new FileLogger(args[++i], false);
				else if (args[i].equals("-expDir"))
					expDir = args[++i];
				else if (args[i].equals("-output"))
					outFile = args[++i];
				else if (args[i].equals("-timeStepSize"))
					timeStepSize = Long.valueOf(args[++i]);
				else if (args[i].equals("-numTimeSteps"))
					numTimeSteps = Integer.valueOf(args[++i]);
				else if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else {
					usage("Unknown argument " + args[i]);
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (expDir == null || outFile == null) {
			usage();
			System.exit(1);
		}
		
		FileLogger outFlogger = new FileLogger(outFile, false);
		
		GroundTruth2 groundTruth = new GroundTruth2(expDir, timeStepSize, numTimeSteps);
		new RSSIBasedCBL2(groundTruth, outFlogger, flogger);
	}
}
