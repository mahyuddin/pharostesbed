package pharoslabut.logger.analyzer.cbl;


//import java.util.Locale; // Liang: removed because unused
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import java.util.Arrays;
//import java.util.Comparator; // Liang: removed because unused

// result object returned by procrustes.
class procrustes2D_transform {

    double[][] T;
    double b;
    double[] c;
    // constructor

    public procrustes2D_transform() {
        T = new double[2][2];
        c = new double[2];
    }
}

class Utils {

    static int nDim = 2;

    /**
     * Prints a table indicating the connectivity of the robots aggregated over time.
     * 
     * The table is organized as follows:  
     *  - The first column is the index of the reference robot.
     *  - Each column after that contains the index of another robot,
     *    followed by the number of time-steps in which the reference
     *    robot is connected with the other robot.
     * 
     * @param nRobots The number of robots in the system
     * @param nTimesteps The number of time steps
     * @param pairwiseDist pairwiseDist[i][j][t] is the distance between robots i and j at time t.
     */
    public static void printConnInfo(int nRobots, int nTimesteps, double[][][] pairwiseDist) {
        int[] cnt = new int[nRobots];

        for (int i = 0; i < nRobots; i++) { // for each robot...
            Arrays.fill(cnt, 0);
            
            for (int t = 0; t < nTimesteps; t++) { // For each time step...
            	
                for (int j = 0; j < i; j++) { // For each previous robot...
                	
                    if (pairwiseDist[i][j][t] > 0) { 
                    	// If a connection exists between the previous robot and current robot, increment the cnt of the previous robot
                        cnt[j]++;
                    }
                }
                for (int j = i + 1; j < nRobots; j++) { // For each robot after the current robot...
                    if (pairwiseDist[i][j][t] > 0) {
                    	// If a connection exists between the current robot and later robot,
                    	// increment the cnt of the later robot
                        cnt[j]++;
                    }
                }
            }
            
            System.out.printf("Robot %2d: ", i);
            for (int j = 0; j < nRobots; j++) {
                System.out.printf("| #%d/%3d ", j, cnt[j]);
            }
            System.out.printf(" \n");

        }
    }

    /**
     * Takes as input the number of robots, number of time steps, and the (X,Y) coordinates of each robot
     * at every time step, and computes the distance and heading of the robots.
     * 
     * This is called by Demo1.main
     * 
     * @param nRobots The number of robots.
     * @param nTimesteps The number of time steps
     * @param X The X coordinates of each robot at each unit time.  Organized as follows:
     * [robot 0's x coordinates over time][robot 1's x coordinates over time]...[robot nRobots-1's x coordinates over time] 
     * @param Y The Y coordinates of each robot at each unit time.
     * @param Dist Dist[i][t] is the distance robot i moved between times t and t-1 (
     * @param Thet Thet[i][t] is the change in heading of the robot i between times t and t-1 (0 when t=0)
     */
    public static void coords2odom(int nRobots,
            int nTimesteps,
            double[] X,
            double[] Y,
            double[][] Dist,
            double[][] Thet) {

        int i, t, nA, T;
        double dx, dy;
        double s;

        T = nTimesteps;
        nA = nRobots;

        for (i = 0; i < nA; i++) { // for each robot...
            s = 0;
            Dist[i][0] = 0; // the distance moved at time 0 is zero
            Thet[i][0] = 0; // the angle changed at time 0 is zero
            
            for (t = 1; t < T; t++) { // for each time step...
            	
                dx = X[i * T + t] - X[i * T + (t - 1)]; // x_t - x_{t-1} for i-th robot
                dy = Y[i * T + t] - Y[i * T + (t - 1)]; // y_t - y_{t-1} for i-th robot

                /* dist_{i,t} = sqrt[ (x_t-x_{t-1})^2 + (y_t-y_{t-1})^2 ] , i=1..nRobots, t=2..nTimesteps */
                Dist[i][t] = Math.sqrt(dx * dx + dy * dy);

                /* thet_{i,t} = atan2(y_t-y_{t-1},x_t-x_{t-1}) , i=1..nRobots, t=2..nTimesteps */
                Thet[i][t] = Math.atan2(dy, dx);

                // Save the angle at time = 1
                if (t == 1) {
                    s = Thet[i][t];
                }

                /* normalize time 1 (initial offset) */
                Thet[i][t] -= s; // normalize all theta values to angle at time = 1
            }
        }
    }

    /**
     * Calculates the distance between every pair of robots at every unit of time.
     * 
     * @param nRobots
     * @param nTimesteps
     * @param X
     * @param Y
     * @param Z
     * @param pairwiseDist Results are saved here.
     */
    public static void coords2pairdist(int nRobots,
            int nTimesteps,
            double[] X,
            double[] Y,
            double[] Z,
            double[][][] pairwiseDist) {
        int i, j, t;
        double dx, dy, dz;

        for (t = 0; t < nTimesteps; t++) {
            for (i = 0; i < nRobots - 1; i++) {
                for (j = i + 1; j < nRobots; j++) {
                    dx = X[i * nTimesteps + t] - X[j * nTimesteps + t];
                    dy = Y[i * nTimesteps + t] - Y[j * nTimesteps + t];
                    if (Z.length > 0) // do we have Z-coords ?
                    {
                        dz = Z[i * nTimesteps + t] - Z[j * nTimesteps + t];
                    } else {
                        dz = 0;
                    }
                    pairwiseDist[i][j][t] = Math.sqrt(dx * dx + dy * dy + dz * dz);
                }
            }
        }

    }
    
    //
    // Procrustes analysis
    //
    // conforms coordinates [Xhat,Yhat] to [X,Y] via linear 
    // transformation (translation, scaling, reflection, orthogonal rotation)
    // such that L2 error is minimized.  -- Liang: What is L2 error?
    //
    // returns transformation object:

    // Apply transformation (in-place, i.e. overwrite Xhat,Yhat)
    /**
     * Liang's comment:  I made this private because it is only used by method procrustes2D, 
     * which is defined after this method.
     */
    private static void procrustes2D_apply_transform(int N, double[] Xhat, double[] Yhat, procrustes2D_transform T) {
        double a, b;
        for (int i = 0; i < N; i++) {
            a = Xhat[i] * T.T[0][0] + Yhat[i] * T.T[1][0];
            b = Xhat[i] * T.T[0][1] + Yhat[i] * T.T[1][1];

            Xhat[i] = a * T.b + T.c[0];
            Yhat[i] = b * T.b + T.c[1];
        }
    }

    /**
     * Liang:
     * 
     * Copied the following description from Demo1.java:
     * 
     * Procrustes fits via translation, scaling, rotation, and reflection
     * Xhat, Yhat to Xtrue, Ytrue such that the L2-norm is minimized.
     * 
     * @param N
     * @param X
     * @param Y
     * @param Xhat
     * @param Yhat
     * @return
     */
    public static procrustes2D_transform procrustes2D(int N,
            double[] X,
            double[] Y,
            double[] Xhat, // in-place (overwritten by result)
            double[] Yhat) // in-place (overwritten by result)
    {
        int i;
        double muX, muY, muXhat, muYhat;

        double[] X0, Y0, X0hat, Y0hat;

        double normXY0, normXY0hat;

        double[][] A, U, V;
        double[] S;

        double trsqrtAA, a, b;


        procrustes2D_transform result = new procrustes2D_transform();

        double err;

        //
        // center at origin
        //

        // compute means
        muX = muY = muXhat = muYhat = 0;
        for (i = 0; i < N; i++) {
            muX += X[i];
            muY += Y[i];
            muXhat += Xhat[i];
            muYhat += Yhat[i];
        }
        muX /= N;
        muY /= N;
        muXhat /= N;
        muYhat /= N;

        // subtract means
        X0 = new double[N];
        System.arraycopy(X, 0, X0, 0, N);
        Y0 = new double[N];
        System.arraycopy(Y, 0, Y0, 0, N);
        X0hat = new double[N];
        System.arraycopy(Xhat, 0, X0hat, 0, N);
        Y0hat = new double[N];
        System.arraycopy(Yhat, 0, Y0hat, 0, N);
        for (i = 0; i < N; i++) {
            X0[i] -= muX;
            Y0[i] -= muY;
            X0hat[i] -= muXhat;
            Y0hat[i] -= muYhat;
        }

        //
        // compute centered Frobenius norm
        //
        normXY0 = normXY0hat = 0;
        for (i = 0; i < N; i++) {
            normXY0 += X0[i] * X0[i] + Y0[i] * Y0[i];
            normXY0hat += X0hat[i] * X0hat[i] + Y0hat[i] * Y0hat[i];
        }

        // == sqrt(trace([X0 Y0] * [X0 Y0]')
        normXY0 = Math.sqrt(normXY0);

        // ==sqrt(trace([X0hat Y0hat] * [X0hat Y0hat]')
        normXY0hat = Math.sqrt(normXY0hat);

        // scale to equal (unit) norm
        for (i = 0; i < N; i++) {
            X0[i] /= normXY0;
            Y0[i] /= normXY0;
            X0hat[i] /= normXY0hat;
            Y0hat[i] /= normXY0hat;
        }


        //
        // compute transformation: rotation/reflection
        //

        // A= [X0,Y0]' * [X0hat,Y0hat]
        A = new double[2][2];
        A[0][0] = A[0][1] = A[1][0] = A[1][1] = 0;

        for (i = 0; i < N; i++) {
            A[0][0] += X0[i] * X0hat[i];
            A[0][1] += X0[i] * Y0hat[i];
            A[1][0] += Y0[i] * X0hat[i];
            A[1][1] += Y0[i] * Y0hat[i];
        }

        // [U,S,V] = svd(A)
        U = new double[2][2];
        V = new double[2][2];
        S = new double[2];
        try {
            compute_svd2x2(A, U, V, S);
        } catch (NotConvergedException myexc) {
            // do something
        }

        // compute transformation: scaling
        trsqrtAA = S[0] + S[1]; // == trace(sqrtm(A"*A)

        //
        // Assemble return argument
        //

        // Tranform T=V*U'
        result.T[0][0] = V[0][0] * U[0][0] + V[0][1] * U[0][1];
        result.T[0][1] = V[0][0] * U[1][0] + V[0][1] * U[1][1];
        result.T[1][0] = V[1][0] * U[0][0] + V[1][1] * U[0][1];
        result.T[1][1] = V[1][0] * U[1][0] + V[1][1] * U[1][1];

        // the scale component
        result.b = trsqrtAA * normXY0 / normXY0hat;


        // the translation component
        result.c[0] = muX - result.b * (muXhat * result.T[0][0] + muYhat * result.T[1][0]);
        result.c[1] = muY - result.b * (muXhat * result.T[0][1] + muYhat * result.T[1][1]);


        // Apply transformation (in-place, i.e. overwrite Xhat,Yhat)
        Utils.procrustes2D_apply_transform(N, Xhat, Yhat, result);

        // return transformation
        return result;

    }

    private static void compute_svd2x2(double[][] A, double[][] U, double[][] V, double[] S) throws NotConvergedException {
        SVD svd = new SVD(2, 2);
        svd = SVD.factorize(new DenseMatrix(A));


        double[] svals = svd.getS();
        S[0] = svals[0];
        S[1] = svals[1];

        U[0][0] = svd.getU().get(0, 0);
        U[0][1] = svd.getU().get(0, 1);
        U[1][0] = svd.getU().get(1, 0);
        U[1][1] = svd.getU().get(1, 1);

        V[0][0] = svd.getVt().get(0, 0);
        V[0][1] = svd.getVt().get(1, 0);
        V[1][0] = svd.getVt().get(0, 1);
        V[1][1] = svd.getVt().get(1, 1);
    }
}

