package pharoslabut.logger.analyzer.cbl;


public class Workspace {

    /* Nested class defines rows in connArr. Describes connection between <from> and <to> at time t */
    class CONNITEM {

        int t;      // time index connection refers to
        int from;   // index in all-nodes-array
        int to;     // index in all-nodes-array
        double observedDistance;
        double deltaX;
        double deltaY;
        double derivedDistance;
    }
// array of connitem (dynamically allocated at the start)
    CONNITEM[] connArr;
    int nConn;
// derived position Array: possArrX[i][t]= x-location of node i at time t (index i in all-nodes-array)
    double[][] posArrX;
    double[][] posArrY;
    double[][] posArrZ;
// observed odometry: Dist[i][t]= distance between t-1 and t of node i (index i in all-nodes-array)
    double[][] odomDist;
    double[][] odomThet;
// storing some additional info
    double[][] alpha;
    double[][] alpha_sum;
    double[][] beta;
    double[][] beta_sum;
// all-nodes-array
    int nNodes;
    byte[] RobotType;
// Type-I: mobile nodes with unknown location
    int nTypeI;
    int[] indexTypeI; // index of Type-I nodes in all-nodes-array
// Type-II: stationary nodes with unknown location
    int nTypeII;
    int[] indexTypeII; // index of Type-II nodes in all-nodes-array
// Type-III: mobile nodes with known location
// e.g. if all nodes are mobile with unknown location, one of them can become the anchor
    int nTypeIII;
    int[] indexTypeIII; // index of Type-III nodes in all-nodes-array
// Type-IV: stationary nodes with known location
    int nTypeIV;
    int[] indexTypeIV; // index of Type-IV nodes in all-nodes-array
    int nTimesteps;

// constructor 
    public Workspace(int _nRobots,
            int _nTimesteps,
            byte[] _RobotType,
            double[][][] _pairwiseDist,
            double[][] _odomDist,
            double[][] _odomThet,
            double[][] _posArrX,
            double[][] _posArrY,
            double[][] _posArrZ) {
        nNodes = _nRobots;
        nTimesteps = _nTimesteps;

        // allocate position arrays
        posArrX = new double[nNodes][nTimesteps];
        posArrY = new double[nNodes][nTimesteps];
        posArrZ = new double[nNodes][nTimesteps];

        // allocate remaining arrays
        alpha = new double[nNodes][nTimesteps];
        beta = new double[nNodes][nTimesteps];
        alpha_sum = new double[nNodes][nTimesteps];
        beta_sum = new double[nNodes][nTimesteps];

        odomDist = new double[nNodes][nTimesteps];
        odomThet = new double[nNodes][nTimesteps];


        RobotType = new byte[nNodes];

        // Copy data
        for (int i = 0; i < _nRobots; i++) {
            System.arraycopy(_odomThet[i], 0, odomThet[i], 0, nTimesteps);
            System.arraycopy(_odomDist[i], 0, odomDist[i], 0, nTimesteps);
            System.arraycopy(_posArrZ[i], 0, posArrZ[i], 0, nTimesteps); // also copy Z-coordinates. Not altered during optimization.
        }

        // Copy robot type
        System.arraycopy(_RobotType, 0, RobotType, 0, nNodes);

        // allocate index arrays (maximally)
        indexTypeI = new int[nNodes];
        indexTypeII = new int[nNodes];
        indexTypeIII = new int[nNodes];
        indexTypeIV = new int[nNodes];


        // count the node types and fill index arrays
        nTypeI = 0;
        nTypeII = 0;
        nTypeIII = 0;
        nTypeIV = 0;
        for (int i = 0; i < nNodes; i++) {
            if (RobotType[i] == 1) {
                indexTypeI[nTypeI++] = i;
            } else if (RobotType[i] == 2) {
                indexTypeII[nTypeII++] = i;
            } else if (RobotType[i] == 3) {
                indexTypeIII[nTypeIII++] = i;
            } else if (RobotType[i] == 4) {
                indexTypeIV[nTypeIV++] = i;
            } else {
                System.out.println("Error: unknown node type. Exiting now.");
                System.exit(-1);
            }
        }


        // count number of total connections and find node with most connections
        // note that we only consider connections between nodes where at least one of them is unknown (i.e. type-I or type-II)
        nConn = 0;
        int nNodeConnCurr = 0;
        int nNodeConnBest = 0;
        int ixBest = 0;
        for (int i = 0; i < nNodes - 1; i++) {
            nNodeConnCurr = 0;
            for (int j = i + 1; j < nNodes; j++) {
                for (int t = 0; t < nTimesteps; t++) {
                    // consider connections between i and j only if either i or j is type-I or type-II
                    if (_pairwiseDist[i][j][t] > 0 && (RobotType[i] < 3 || RobotType[j] < 3)) {
                        nConn++;
                        nNodeConnCurr++;
                    }
                }
            }
            // find node with most connections over all timesteps
            // ideally, we only want type-I nodes upgraded to type-III
            if (nNodeConnCurr > nNodeConnBest && RobotType[i] == 1) {
                ixBest = i;
                nNodeConnBest = nNodeConnCurr;
            }
        }

        // allocate connection array
        connArr = new CONNITEM[nConn];
        for (int i = 0; i < nConn; i++) {
            connArr[i] = new CONNITEM();
        }
        // fill connection array
        int pos = 0;
        for (int t = 0; t < nTimesteps; t++) {
            for (int i = 0; i < nNodes - 1; i++) {
                for (int j = i + 1; j < nNodes; j++) {
                    // consider connections between i and j only if either i or j is type-I or type-II
                    if (_pairwiseDist[i][j][t] > 0 && (RobotType[i] < 3 || RobotType[j] < 3)) {
                        connArr[pos].observedDistance = _pairwiseDist[i][j][t];
                        connArr[pos].from = i;
                        connArr[pos].to = j;
                        connArr[pos].t = t;
                        pos++;
                    }
                }
            }
        }

        // Case 1:
        // if we only got type-I or type-II nodes (i.e., nodes with unknown locations), pick the best-connected type-I node
        // and place it somewhere (default 0,0), effectively removing it from the optimization problem and turning it into a
        // type-III node
        if (nTypeIII == 0 && nTypeIV == 0) {

            // node ixBest with most connections becomes type-III node
            nTypeI = nTypeI - 1;

            // rebuild indexTypeI, leaving out ixBest
            pos = 0;
            for (int i = 0; i < nNodes; i++) {
                if (i != ixBest && _RobotType[i] == 1) {
                    indexTypeI[pos++] = i;
                }
            }

            nTypeIII = 1;
            indexTypeIII[0] = ixBest;

            nTypeIV = 0;

            // resize arrays


            // Compute locations of type-III nodes once which are not altered during optimization
            posArrX[ixBest][0] = 0; // initial location is (x,y,rot)=(0,0,0)
            posArrY[ixBest][0] = 0;
            for (int t = 1; t < nTimesteps; t++) {
                posArrX[ixBest][t] = posArrX[ixBest][t - 1] + _odomDist[ixBest][t] * Math.cos(_odomThet[ixBest][t]);
                posArrY[ixBest][t] = posArrY[ixBest][t - 1] + _odomDist[ixBest][t] * Math.sin(_odomThet[ixBest][t]);
            }
        }
        //
        // Case 2: Additionally have nodes with known location
        //
        else {
            // Compute locations of type-III nodes once (not altered during optimization)
            for (int i = 0; i < nTypeIII; i++) {
                // just copy function argument
                for (int t = 0; t < nTimesteps; t++) {
                    posArrX[indexTypeIII[i]][t] = _posArrX[indexTypeIII[i]][t];
                    posArrY[indexTypeIII[i]][t] = _posArrY[indexTypeIII[i]][t];
                }
            }
            // Same for locations of type-IV
            for (int i = 0; i < nTypeIV; i++) {
                // just copy function argument
                for (int t = 0; t < nTimesteps; t++) {
                    posArrX[indexTypeIV[i]][t] = _posArrX[indexTypeIV[i]][t];
                    posArrY[indexTypeIV[i]][t] = _posArrY[indexTypeIV[i]][t];
                }
            }
        }

        // compute alpha,beta,alpha_sum,beta_sum for all nodes in advance
        // (only values corresponding to type-I values will be changed 
        //  in recomputeFrom(Xstart) )
        for (int i = 0; i < nNodes; i++) {
            alpha[i][0] = 0;
            alpha_sum[i][0] = 0;
            beta[i][0] = 0;
            beta_sum[i][0] = 0;
            for (int t = 1; t < nTimesteps; t++) {
                alpha[i][t] = odomDist[i][t] * Math.cos(odomThet[i][t] + 0);
                alpha_sum[i][t] = alpha_sum[i][t - 1] + alpha[i][t];
                beta[i][t] = odomDist[i][t] * Math.sin(odomThet[i][t] + 0);
                beta_sum[i][t] = beta_sum[i][t - 1] + beta[i][t];
            }
        }




    }
//
// given array with startlocations from optimizer, recompute derived locations and distances
//

    public void recomputeFrom(double[] Xstart) {
        // Entries of array Xstart are ordered as follows:
        //    [typeI-X{1..nTypeI), typeI-Y(1..nTypeI), typeI-rot(1..nTypeI),
        //     typeII-X(1..nTypeII), typeII-Y(1..nTypeII)


// new: dist und thet sind jetzt nTimesteps arrays (anstatt wie frueher (nTimesteps-1)), deren erster Eintrag 0 ist
        // damit das jetzt mit den Indizes besser klappt

        // compute rotations for type-I nodes
        double rot;
        int ix;
        for (int i = 0; i < nTypeI; i++) {
            // Extract heading from Xstart
            rot = Xstart[i + 2 * nTypeI];
            // Get index
            ix = indexTypeI[i];
            alpha[ix][0] = 0;
            alpha_sum[ix][0] = 0;
            beta[ix][0] = 0;
            beta_sum[ix][0] = 0;
            for (int t = 1; t < nTimesteps; t++) {
                alpha[ix][t] = odomDist[ix][t] * Math.cos(odomThet[ix][t] +/*Rot[i]*/ rot);
                alpha_sum[ix][t] = alpha_sum[ix][t - 1] + alpha[ix][t];
                beta[ix][t] = odomDist[ix][t] * Math.sin(odomThet[ix][t] +/*Rot[i]*/ rot);
                beta_sum[ix][t] = beta_sum[ix][t - 1] + beta[ix][t];
            }
        }

        // Compute derived locations for type-I nodes accordingly
        for (int i = 0; i < nTypeI; i++) {
            // Get index
            ix = indexTypeI[i];
            // Extract location from Xstart
            posArrX[ix][0] = Xstart[i];
            posArrY[ix][0] = Xstart[i + nTypeI];
            for (int t = 1; t < nTimesteps; t++) {
                posArrX[ix][t] = posArrX[ix][t - 1] + alpha[ix][t];
                posArrY[ix][t] = posArrY[ix][t - 1] + beta[ix][t];
            }
        }

        // Compute derived locations for type-II nodes (stationary => copy across time)
        for (int i = 0; i < nTypeII; i++) {
            // Get index
            ix = indexTypeII[i];
            // Extract location from Xstart
            posArrX[ix][0] = Xstart[3 * nTypeI + i];
            posArrY[ix][0] = Xstart[3 * nTypeI + nTypeII + i];
            // Location doesn't change over time (TypeII is stationary node with unknown location)
            for (int t = 1; t < nTimesteps; t++) {
                posArrX[ix][t] = posArrX[ix][t - 1];
                posArrY[ix][t] = posArrY[ix][t - 1];
            }
        }

        // Don't have to recompute locations for type-III or type-IV nodes

        // Compute pairwise distances (new! with Z-coordinate)
        int from, to, t;
        double deltaZ;
        for (int i = 0; i < nConn; i++) {
            from = connArr[i].from;
            to = connArr[i].to;
            t = connArr[i].t;
            connArr[i].deltaX = posArrX[from][t] - posArrX[to][t];
            connArr[i].deltaY = posArrY[from][t] - posArrY[to][t];
            // without Z-coordinate
            // connArr[i].derivedDistance = Math.sqrt(connArr[i].deltaX * connArr[i].deltaX + connArr[i].deltaY * connArr[i].deltaY);
            deltaZ = posArrZ[from][t] - posArrZ[to][t];
            connArr[i].derivedDistance = Math.sqrt(connArr[i].deltaX * connArr[i].deltaX
                    + connArr[i].deltaY * connArr[i].deltaY + deltaZ * deltaZ); // factor in the Z-coordinate (i.e., height of floor the robot is on)
        }


    }
}
