package pharoslabut.logger.analyzer.cbl;

// The problem we want to optimize, consists of a function to compute the gradient, and a function to compute the functionvalue, and the size of the problem
abstract class Base_data {

	/**
	 * Liang's comment:
	 * 
	 * This is the number of unknown variables, which is the number of optimization variables.
	 * 
	 * It is equal to 3 * (Number of type I robots) + 2 * (Number of type II robots),
	 * where type I is moving robot with unknown location and type II is still robot
	 * with unknown location.
	 * 
	 * I believe type I robots have x, y, and theta as unkown variables, while
	 * type II only has x, and y.
	 */
    public int nVariables; 

    public abstract double compute_f(double[] x);

    public abstract void compute_fgrad(double[] x, double[] g);

//    Liang's comment: I got rid of this because it was not used...
//    
//    public void check_grad(double[] x) {
//        int i;
//        double fplus, fmin;
//        double[] xplus;
//        double[] xmin;
//        double[] g_analytic;
//        double[] g_numeric;
//
//        xplus = new double[nVariables];
//        xmin = new double[nVariables];
//        g_analytic = new double[nVariables];
//        g_numeric = new double[nVariables];
//
//        // Compute gradient analytically
//        compute_fgrad(x, g_analytic);
//
//        // Compute gradient numerically (finite differences)
//        for (i = 0; i < nVariables; i++) {
//            System.arraycopy(x, 0, xplus, 0, nVariables);
//            System.arraycopy(x, 0, xmin, 0, nVariables);
//
//            xplus[i] += 1e-6;
//            xmin[i] -= 1e-6;
//
//            fplus = compute_f(xplus);
//            fmin = compute_f(xmin);
//
//            g_numeric[i] = (fplus - fmin) / 2e-6;
//        }
//
//        // compare the two
//        System.out.println("Analytic   Numeric    Difference");
//        for (i = 0; i < nVariables; i++) {
//            System.out.println(g_analytic[i] + "  " + g_numeric[i] + "  " + (g_analytic[i] - g_numeric[i]));
//        }
//
//    }
}

class Phase2_range_data extends Base_data {

    public Workspace w;

    // Constructor 
    Phase2_range_data(int _nRobots,
            int _nTimesteps,
            byte[] _robotType,
            double[][][] _Range,
            double[][] _Dist,
            double[][] _Thet,
            double[][] _posArrX,
            double[][] _posArrY,
            double[][] _posArrZ) {
        // init workspace
        w = new Workspace(_nRobots, _nTimesteps, _robotType, _Range, _Dist, _Thet, _posArrX, _posArrY, _posArrZ);
        // number of variables in optimization
        nVariables = 3 * w.nTypeI + 2 * w.nTypeII;
    }

    public double compute_f(double[] x) {
        // recompute workspace
        w.recomputeFrom(x);
        // loop over all connections and compute error
        double sum = 0;
        double pcx;
        for (int i = 0; i < w.nConn; i++) {
            pcx = w.connArr[i].observedDistance - w.connArr[i].derivedDistance;
            sum += pcx * pcx; // Error=quadratic
        }
        return sum;
    }

    public void compute_fgrad(double[] x, double[] g) {
        // recompute workspace
        w.recomputeFrom(x);
        // temporary
        double[] gradX = new double[w.nNodes];
        double[] gradY = new double[w.nNodes];
        double[] gradRot = new double[w.nNodes];

        double h;

        for (int i = 0; i < w.nNodes; i++) {
            gradX[i] = 0;
            gradY[i] = 0;
            gradRot[i] = 0;
        }

        double delta, gamma_from, gamma_to;
        for (int i = 0; i < w.nConn; i++) {
            delta = 2 * (w.connArr[i].observedDistance - w.connArr[i].derivedDistance) / w.connArr[i].derivedDistance;

            // gradX[w.connArr[i].from] += delta * (-w.connArr[i].deltaX);
            // gradX[w.connArr[i].to] += delta * w.connArr[i].deltaX;
            h = delta * w.connArr[i].deltaX;
            gradX[w.connArr[i].from] -= h;
            gradX[w.connArr[i].to] += h;

            // gradY[w.connArr[i].from] += delta * (-w.connArr[i].deltaY);
            // gradY[w.connArr[i].to] += delta * w.connArr[i].deltaY;
            h = delta * w.connArr[i].deltaY;
            gradY[w.connArr[i].from] -= h;
            gradY[w.connArr[i].to] += h;

            gamma_from = w.connArr[i].deltaX * w.beta_sum[w.connArr[i].from][w.connArr[i].t]
                    - w.connArr[i].deltaY * w.alpha_sum[w.connArr[i].from][w.connArr[i].t];
            gamma_to = w.connArr[i].deltaX * w.beta_sum[w.connArr[i].to][w.connArr[i].t]
                    - w.connArr[i].deltaY * w.alpha_sum[w.connArr[i].to][w.connArr[i].t];

            gradRot[w.connArr[i].from] += delta * gamma_from;
            gradRot[w.connArr[i].to] += -delta * gamma_to;
        }

        // copy to gradient: type-I nodes
        for (int i = 0; i < w.nTypeI; i++) {
            g[i] = gradX[w.indexTypeI[i]];
            g[i + w.nTypeI] = gradY[w.indexTypeI[i]];
            g[i + 2 * w.nTypeI] = gradRot[w.indexTypeI[i]];
        }
        // copy to gradient: type-II nodes (no rotation needed)
        for (int i = 0; i < w.nTypeII; i++) {
            g[3 * w.nTypeI + i] = gradX[w.indexTypeII[i]];
            g[3 * w.nTypeI + i + w.nTypeII] = gradY[w.indexTypeII[i]];
        }

    }
}

