package pharoslabut.logger.analyzer.cbl;

import pharoslabut.navigate.Location;
import pharoslabut.logger.*;

public class CBL {
	
	private int nRobots;
	private int[] robotIDs; // map between array index and actual robot ID
	private int nTimesteps;
	private byte[] robotType; 
	private double[] Xtrue;
	private double[] Ytrue;
	private double[] Ztrue;
	
	
	/**
	 * The constructor.
	 * 
	 * @param gt The ground truth.
	 */
	public CBL(GroundTruth gt) {
		nRobots = gt.getNumRobots();
		robotIDs = gt.getRobotIDs();
		nTimesteps = gt.getNumTimeSteps();
		robotType = gt.getRobotType();
		Xtrue = gt.getLongitudeTruth();
		Ytrue = gt.getLatitudeTruth();
		Ztrue = gt.getElevationTruth();
	}
	
	/**
	 * Returns the distance between robots i and j at time t.
	 * 
	 * @param i Robot i
	 * @param j Robot j
	 * @param t Time t
	 * @return The distance in meters.
	 */
	private double getDist(int i, int j, int t) {
		// Get the locations of robots i and j at time t
		Location loci = new Location(Ytrue[i*nTimesteps + t], Xtrue[i*nTimesteps + t]);
		Location locj = new Location(Ytrue[j*nTimesteps + t], Xtrue[j*nTimesteps + t]);
		return loci.distanceTo(locj);
	}
	
	/**
	 * Performs connectivity-based localization.
	 * 
	 * @param max_comm The maximum range between two robots in meters. 
	 * No range data is provided for nodes separated by more than this distance.
	 * A value of -1 indicates infinite range, meaning the distances between nodes
	 * are fully specified over all time intervals.
	 * @param outFlogger The logger in which the results are saved.  This may be null
	 * if the results should only be printed to standard out.
	 */
	public void doCBL(double max_comm, FileLogger outFlogger) {
		
		log("Max range: " + max_comm, outFlogger);
		
		//
		// 1. Preparation and assembling data
		//

		// compute all pairwise distances from true coordinates
		double[][][] pairwiseDist = new double[nRobots][nRobots][nTimesteps];
		Utils.coords2pairdist(nRobots, nTimesteps, Xtrue, Ytrue, Ztrue, pairwiseDist);

		// simulate restricted comm range (no range info for inter-robot distances greater than max_comm).
		for (int i = 0; i < nRobots - 1; i++) {
			for (int j = i + 1; j < nRobots; j++) {
				for (int t = 0; t < nTimesteps; t++) {
					if (max_comm != -1 && getDist(i, j, t) > max_comm) {
						//log("Robots " + i + " and " + j + " are out of range...", outFlogger);
						pairwiseDist[i][j][t] = -1; // '-1' means out of range
					}
				}
			}
		}

		// compute all odometry from true coordinates
		double[][] odomDist = new double[nRobots][nTimesteps];
		double[][] odomThet = new double[nRobots][nTimesteps];
		Utils.coords2odom(nRobots, nTimesteps, Xtrue, Ytrue, odomDist, odomThet);

		// convert 1d arrays xtrue,ytrue to 2d arrays posArrX,posArrY,posArrZ
		double[][] posArrX = new double[nRobots][nTimesteps];
		double[][] posArrY = new double[nRobots][nTimesteps];
		double[][] posArrZ = new double[nRobots][nTimesteps];
		for (int i = 0; i < nRobots; i++) {
			for (int t = 0; t < nTimesteps; t++) {
				if (robotType[i] < 3) { // if location of robot is unknown...

					/*
					 * Liang's comment: This will not lead to confusion even if the robot 
					 * is located at (-1,-1) because the Driver.Pharos2_range also takes in
					 * the robot type so it knows whether the location is known.
					 */
					posArrX[i][t] = -1; // type-I and type-II means unknown XY-location
					posArrY[i][t] = -1; // type-I and type-II means unknown XY-location
				} else {
					posArrX[i][t] = Xtrue[i * nTimesteps + t]; // type-III and type-IV means known XY location
					posArrY[i][t] = Ytrue[i * nTimesteps + t]; // type-III and type-IV means known XY location
				}
				posArrZ[i][t] = Ztrue[i * nTimesteps + t]; // Z locations are always known
			}
		}

		//
		// 2. Do localization
		//
		int max_trials = 100;
		double tol0 = 0;
		LocalizationResult result = Driver.Phase2_range(
				nRobots, 		// Number of robots
				nTimesteps, 	// Number of time steps
				robotType, 		// RobotType[i] = type of robot i
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
}
