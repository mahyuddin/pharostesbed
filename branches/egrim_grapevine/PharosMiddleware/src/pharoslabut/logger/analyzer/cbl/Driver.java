package pharoslabut.logger.analyzer.cbl;

import pharoslabut.logger.*;

public class Driver {

    /*
     *   Phase2_range is the main computational routine
     *
     *   Inputs:
     *
     *                       nRobots : number of robots
     *
     *                    nTimesteps : number of time steps
     *
     *                  RobotType[i] : type of i-th robot (nRobots x 1 array)
     *
     *        observedPairDist[i][j][t] : distance between robot i and j at time t
     *                                    entries <0 indicate no measurement
     *                                    symmetric
     * 	                                 (i=0..nRobots-2; j=i+1..nRobots-1;  t=0..nTimesteps-1)
     *
     *               odomDist[i][t] : translation of i-th robot between t and t-1
     *                                   (odomDist[i][t] := 0 for t=0)
     *	 				 (i=0..nRobots-1; t=0..nTimesteps-1)
     *
     *		      odomThet[i][t] : rotation of i-th robot between t and t-1
     *				         (i=0..nRobots-1; t=0..nTimesteps-1)
     *                                   (odomThet[i][t] := 0 for t=0)
     *
     *                 posArrX[i][t] : ground truth X-location of i-th robot at time t
     *                                    (entry *must* be given if i-th robot is of type-III or type-IV)
     *                                    (entry is ignored if i-th robot is of type-I or type-II)
     *                                    (i=0..nRobots-1; t=0..nTimesteps-1)
     *
     *                 posArrY[i][t] : same as posArrX
     *
     *                 posArrZ[i][t] : ground truth Z-location of i-th robot at time t
     *                                    (height has same scale as the xy axis)
     *                                    (entry *must* be given for all robots for all timesteps)
     *                                    (i=0..nRobots-1; t=0..nTimesteps-1)
     *
     *                   max_trials : maximum number of trials in random search for start locations
     *                                    (typical values are max_trials = 100, 500, ...)
     *
     *                         tol0 : stopping criterion in random search for start locations
     *                                    (typical values are 1e-2, 1e-3, ...)
     *
     *
     *   Returns: a localization_result object
     *          
     *            result.Xhat[i][t] : estimated X-location of i-th robot at time t
     *                                   (i=0..nRobots-1; t=0..nTimesteps-1)
     * 
     *            result.Yhat[i][t] : same as result.Xhat
     * 
     *            result.Zhat[i][t] : directly copied from input posArrZ 
     *                                  (not part of the optimization)
     *
     *                        score : residual of minimization.
     *                                  (in general, lower is better)
     *
     *                       Xstart : vector of assiated start locations 
     *                                  (see below)
     *
     *
     * Remarks:
     *
     *       - Possible robot types are
     *                     1 = mobile + unknown locations (type-I)
     *                     2 = stationary + unknown locations (type-II)
     *                     3 = mobile + known locations (type-III)
     *                     4 = stationary + known locations (type-IV)
     *
     *         If only robots of type-I and type-II are available, the 'best-connected'
     *         type-I robot is upgraded to type-III and, as a default, placed in (0,0) facing east.
     *
     *      - The routine no longer performs registration of the coordinate frame via procrustes.
     *        Instead, estimated locations are directly returned. If the data contained a sufficient
     *        number of genuine type-III or type-IV nodes, the result will be in ground truth coordinates.
     *
     *      - Data in posArrXYZ must be supplied for all time steps, even if the associated robot was
     *        stationary or the Z-coordinate (height) is constant (which we assume).
     *
     *      - Parameter max_trials and tol0 strongly impact run time (which is proportional to the number of trials).
     *        Setting max_trials is rather straightforward and should be set as high as possible (typical values are
     *        max_trials=100 or max_trials=500). However, sometimes we discover a good solution early on, in which case
     *        we no longer need to continue looking for a better solution. This is what parameter tol0 is for.
     *        Unfortunately, setting this parameter right is difficult and requires experimentation; the residual during
     *        optimization is not scale-free and depends on the actual problem. What constitutes a low score in one case
     *        might only be mediocre in another. Proceed with care.
     * 
     *      - The routine returns the vector of initial locations which, together with the odometry, can be used to 
     *        estimate the locations for all time steps. Xstart depends on a concrete robot configuration (their ordering 
     *        in the index set, the number of type-I, type-II, type-III, type-IV nodes, and so on.) The contents of Xstart are
     *        as follows (array of length is 3*nType-I + 2*nType-II):
     * 
     *                X-locations at t=0 for all type-I nodes  (nType-I entries)
     *                Y-locations at t=0 for all type-I nodes  (nType-I entries)
     *                Heading at t=0 for all type-I nodes      (nType-I entries)
     *                X-locations at t=0 for all type-II nodes (nType-II entries)
     *                Y-locations at t=0 for all type-II nodes (nType-II entries)
     *    
     *
     */
	public static LocalizationResult Phase2_range(
			int nRobots, // total number of robots
			int nTimesteps, // total number of time steps
			byte[] robotType, // 1=mobile+unknown, 2=stationary+unknown, 3=mobile+known, 4=mobile+stationary
			double[][][] observedPairDist, // observedPairDist[i][j][t]: distance between robot i and j at time t
			double[][] odomDist, // odomDist[i][t]: translation of i-th robot between t and t-1 (0 when t=0)
			double[][] odomThet, // odomThet[i][t]: rotation of i-th robot between t and t-1 (0 when t=0)
			double[][] posArrX, // posArrX[i][t]: ground-truth X-location of robot i at time t, ignored if robot is type I or II
			double[][] posArrY, // ditto
			double[][] posArrZ, // posArrZ[i][t]: ground-truth Z-location of i-th robot at time t
			int MAX_TRIALS, // The maximum number of restarts from completely random x vector
			double TOL0, // Error threshold, once errors falls less than this, do not continue to search for optimum value
			FileLogger flogger
	) {

		// create task object for optimizer
		Phase2_range_data the_task = new Phase2_range_data(nRobots, nTimesteps, robotType, 
				observedPairDist, odomDist, odomThet, posArrX, posArrY, posArrZ);


		// create optimizer object (with options 200 iterations)
		QuasiNewton_Optimizer optim = new QuasiNewton_Optimizer(200);
		// optim.setTerminationNoMoreProgress(1e-1, 1e-2);

		// run optimization with different initial values for Xstart
		double[] Xstart_curr = new double[the_task.nVariables];
		double[] Xstart_best = new double[the_task.nVariables];

		/*
		 * Liang: lower value is better.  Once the error becomes less than TOL0, 
		 * do not continue search for better Xstart vector.
		 */
		double best_score = 10000000;

		/*
		 * Liang: This is the number of restarts.  Each restart starts with a completely
		 * random Xstart_curr array.  This idea is that by restarting numerous times, we can
		 * avoid local minima.
		 */
		for (int trial = 0; trial < MAX_TRIALS; trial++) {

			/*
			 * Liang's comment:
			 * 
			 * Here's a wikipedia definition of Latin Hypercube:
			 * Latin hypercube sampling (LHS) is a statistical method for generating 
			 * a distribution of plausible collections of parameter values from a 
			 * multidimensional distribution. The sampling method is often applied 
			 * in uncertainty analysis.
			 * 
			 * @see http://en.wikipedia.org/wiki/Latin_hypercube_sampling
			 */
			// generate random initial values (to do: Latin Hypercube samples)
			for (int i = 0; i < the_task.nVariables; i++) {
				Xstart_curr[i] = 20 * Math.random();
			}

			// optimize
			Optimizer_result oresult = optim.optimize(Xstart_curr, the_task);

			log("Trial: " + trial + ", score " + oresult.res, flogger);
			// remember best score
			if (oresult.res < best_score) {
				best_score = oresult.res;
				System.arraycopy(oresult.x, 0, Xstart_best, 0, the_task.nVariables);
			}

			if (best_score < TOL0) {
				break;
			}
		}


		// having found best Xstart, compute locations from Xstart_best + odometry
		Workspace W = new Workspace(nRobots, nTimesteps, robotType, observedPairDist, 
				odomDist, odomThet, posArrX, posArrY, posArrZ);
		W.recomputeFrom(Xstart_best);

		// assemble return argument
		LocalizationResult result = new LocalizationResult(nRobots, nTimesteps, the_task.nVariables);

		// copy estimated locations to result
		for (int i = 0; i < nRobots; i++) {
			for (int t = 0; t < nTimesteps; t++) {
				result.Xhat[i][t] = W.posArrX[i][t];
				result.Yhat[i][t] = W.posArrY[i][t];
				result.Zhat[i][t] = W.posArrZ[i][t];
			}
		}

		// copy Xstart_best to result
		System.arraycopy(Xstart_best, 0, result.Xstart, 0, the_task.nVariables);

		// copy associated score to result (goodness-of-fit)
		result.score = best_score;

		// no longer perform procrustes registration at this point
		result.relative_frame = false;

		return result;
	}


    /*
     *  Alternative version of the above, which instead of doing repeated optimization trials from random initial
     *  conditions, uses only one trial with Xstart as starting point.
     *
     *  Remarks:
     *
     *     - Vector Xstart depends on a given problem configuration. (see above)
     */
//    public static LocalizationResult Phase2_range(int nRobots,
//            int nTimesteps,
//            byte[] RobotType, // 1=mobile+unknown, 2=stationary+unknown, 3=mobile+known, 4=mobile+stationary
//            double[][][] observedPairDist,
//            double[][] odomDist,
//            double[][] odomThet,
//            double[][] posArrX,
//            double[][] posArrY,
//            double[][] posArrZ,
//            double[] Xstart) {
//
//        int i, t;
//
//
//        Phase2_range_data the_task;
//        Optimizer_result oresult;
//        Workspace W;
//
//        // create task object for optimizer
//        the_task = new Phase2_range_data(nRobots, nTimesteps, RobotType, observedPairDist, odomDist, odomThet, posArrX, posArrY, posArrZ);
//
//
//        // create optimizer object (with options 200 iterations)
//        QuasiNewton_Optimizer optim = new QuasiNewton_Optimizer(200);
//
//
//        // run optimization with given initial Xstart
//        oresult = optim.optimize(Xstart, the_task);
//
//        // optimization produces oresult.x
//
//        // compute locations from oresult.x + odometry
//        W = new Workspace(nRobots, nTimesteps, RobotType, observedPairDist, odomDist, odomThet, posArrX, posArrY, posArrZ);
//        W.recomputeFrom(oresult.x);
//
//        // assemble return argument
//        LocalizationResult result = new LocalizationResult(nRobots, nTimesteps, the_task.nVariables);
//
//        // copy estimated locations to result
//        for (i = 0; i < nRobots; i++) {
//            for (t = 0; t < nTimesteps; t++) {
//                result.Xhat[i][t] = W.posArrX[i][t];
//                result.Yhat[i][t] = W.posArrY[i][t];
//            }
//        }
//
//        // copy oresult.x to result
//        System.arraycopy(oresult.x, 0, result.Xstart, 0, the_task.nVariables);
//
//        // copy associated score to result (goodness-of-fit)
//        result.score = oresult.res;
//
//        // no longer perform procrustes registration at this point
//        result.relative_frame = false;
//
//        return (result);
//
//    }
	
	private static void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
}