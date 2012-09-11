package pharoslabut.context;

import java.util.List;
import java.util.Random;

import pharoslabut.RobotIPAssignments;
import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.GPSDataBuffer;
import edu.utexas.ece.mpc.context.ContextHandler;
import edu.utexas.ece.mpc.context.ContextHandler.WireSummaryType;
import edu.utexas.ece.mpc.context.logger.ContextLoggingDelegate;
import edu.utexas.ece.mpc.context.summary.ContextSummary;
import edu.utexas.ece.mpc.context.summary.HashMapContextSummary;

public class ContextSubsystem extends Thread implements ContextLoggingDelegate {

    public class Key {

        public static final String CURRENT_SYSTEM_TIME = "current system time";
        public static final String COMPASS_HEADING = "compass heading";
        public static final String GPS_LONGITUDE = "gps longitude";
        public static final String GPS_LATITUDE = "gps latitude";
        public static final String FILL_SEED = "fill seed";
        public static final String FILL_AMOUNT = "fill amount";
        public static final String FILL_PREFIX = "fill: ";

    }

    private static final int COMPASS_MEDIAN_FILTER_LENGTH = 3;

    private static final double ENCODE_DOUBLE_SCALAR = 1000;

    private static final double FAKED_LONGITUDE = -97.631;
    private static final double FAKED_LATITUDE = 30.527;

    private static final double FAKED_COMPASS_HEADING = 3.097;

    private GPSDataBuffer gpsDataBuffer;
    private CompassDataBuffer compassDataBuffer;
    private ContextHandler handler;
    private HashMapContextSummary summary;

    private int id;
    
    private int contextPadding = 0;

    public ContextSubsystem(GPSDataBuffer gpsDataBuffer, CompassDataBuffer compassDataBuffer) {
        Logger.logDbg("Retrieving context handler instance");
        handler = ContextHandler.getInstance();
        
        Logger.logDbg("Configuring context handler logging delegate");
        handler.setLoggerDelegate(this);
        
        Logger.logDbg("Creating local context summary");
        try {
            id = RobotIPAssignments.getID();
            summary = new HashMapContextSummary(id);
        } catch (PharosException e) {
            Logger.logErr("Could not determine ID programmatically, falling back to MAC address-based ID");
            summary = new HashMapContextSummary();
            id = summary.getId();
        }
        Logger.log("Created local context summary with id=" + id);

        if (compassDataBuffer != null) {
            this.compassDataBuffer = compassDataBuffer;
            Logger.logDbg("Compass data available");
        } else {
            Logger.log("Compass data not available");
        }

        if (gpsDataBuffer != null) {
            this.gpsDataBuffer = gpsDataBuffer;
            Logger.logDbg("GPS data available");
        } else {
            Logger.log("GPS data not available");
        }
        
        Logger.logDbg("Notifying handler of context summary");
        handler.updateLocalSummary(summary);
    }

    @Override
    public void run() {
        while (true) {
            Logger.logDbg("Generating updated local context summary");

            summary.clear();
            
            if (contextPadding != 0) { // Zero is a special case = don't send local context
    
                summary.put(Key.CURRENT_SYSTEM_TIME,
                            Integer.valueOf((int) (System.currentTimeMillis() / 1000.0)));
    
                if (compassDataBuffer != null) {
                    try {
                        Logger.logDbg("Updating compass-based context data");
                        summary.put(Key.COMPASS_HEADING,
                                    encode(compassDataBuffer.getMedian(COMPASS_MEDIAN_FILTER_LENGTH)));
                    } catch (NoNewDataException e) {
                        Logger.logErr("Could not get compass heading (faking it instead): " + e.getMessage());
                        summary.put(Key.COMPASS_HEADING, encode(FAKED_COMPASS_HEADING));
                    }
                }
    
                if (gpsDataBuffer != null) {
                    try {
                        Logger.logDbg("Updating GPS-based context data");
                        Location currLoc = new Location(gpsDataBuffer.getCurrLoc());
                        summary.put(Key.GPS_LONGITUDE, encode(currLoc.longitude()));
                        summary.put(Key.GPS_LATITUDE, encode(currLoc.latitude()));
                    } catch (NoNewDataException e) {
                        Logger.logErr("Could not get longitude/latitude (faking it instead): " + e.getMessage());
                        summary.put(Key.GPS_LONGITUDE, encode(FAKED_LONGITUDE));
                        summary.put(Key.GPS_LATITUDE, encode(FAKED_LATITUDE));
                    }
                }
    
                int contextPaddingRequired = Math.max(0, contextPadding - summary.size());
                if (contextPaddingRequired > 0) {
                    if (contextPaddingRequired > 1) {
                        summary.put(Key.FILL_AMOUNT, Math.max(0, contextPaddingRequired - 2));
                    }
                    
                    if (contextPaddingRequired > 2) {
                        summary.put(Key.FILL_SEED, id);
                    }
                    
                    if (contextPaddingRequired > 3) {
                        Random rand = new Random(id);
                        for (int i = 0; i < contextPaddingRequired-2; i++) {
                            summary.put(Key.FILL_PREFIX + i, rand.nextInt());
                        }
                    }
                }

                handler.updateLocalSummary(summary);
            }
            

            List<ContextSummary> receivedSummaries = handler.getReceivedSummaries();
            Logger.logDbg("Current received summary count: " + receivedSummaries.size());
            for (ContextSummary receivedSummary: receivedSummaries) {
                Logger.logDbg(receivedSummary.toString());
                Integer systemTime = receivedSummary.get(Key.CURRENT_SYSTEM_TIME);

                if (systemTime != null) {
                    Logger.logDbg("System timestamp: " + systemTime);
                }
                Integer encodedHeading = receivedSummary.get(Key.COMPASS_HEADING);
                Integer encodedLatitude = receivedSummary.get(Key.GPS_LATITUDE);
                Integer encodedLongitude = receivedSummary.get(Key.GPS_LONGITUDE);
                
                if (encodedHeading != null) {
                    Logger.logDbg("Compass heading: " + decode(encodedHeading));
                }
                    
                if (encodedLatitude != null) {
                    Logger.logDbg("GPS latitude: " + decode(encodedLatitude));
                }
                
                if (encodedLongitude != null) {
                    Logger.logDbg("GPS longitude: " + decode(encodedLongitude));
                }
                
                Integer receivedFillAmount = receivedSummary.get(Key.FILL_AMOUNT);
                if (receivedFillAmount != null) {
                    Logger.logDbg("Fill amount: " + receivedFillAmount);
                }
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.logDbg("Sleep interrupted, resuming early");
            }
        }
    }

    private int encode(double value) {
        return ((int) (value * ENCODE_DOUBLE_SCALAR));
    }

    private double decode(int value) {
        return Double.valueOf(value) / ENCODE_DOUBLE_SCALAR;
    }

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
        return System.getProperty("PharosMiddleware.debug") != null;
    }

    public synchronized void setContextPadding(int padding) {
        if (padding == 0) {
            // Special case, no local context should be sent
            handler.removeLocalSummary();
        }
        contextPadding = padding;
    }
    
    public synchronized int getContextPadding() {
        return contextPadding;
    }

    public void resetAllContext() {
        handler.resetAllSummaryData();
    }

    public void setTau(int tau) {
        handler.setTau(tau);
    }

    public void setSummaryType(WireSummaryType summaryType) {
        handler.setWireSummaryType(summaryType);
        // Need to store summary again so that the correct wire type is created
        handler.updateLocalSummary(summary);
    }
}
