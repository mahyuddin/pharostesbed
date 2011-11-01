package pharoslabut.demo.indoorMRPatrol;

import pharoslabut.logger.Logger;
import edu.utexas.ece.mpc.context.logger.ContextLoggingDelegate;

public class PharosLoggingDelegate implements ContextLoggingDelegate {
    @Override
    public void log(String msg) {
        Logger.log(msg);
    }

    @Override
    public void logError(String msg) {
        Logger.logErr(msg);
    }

    @Override
    public void logDebug(String msg) {
        Logger.logDbg(msg);
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

}
