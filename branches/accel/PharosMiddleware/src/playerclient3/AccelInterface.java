
package playerclient3;

import java.io.IOException;
import java.util.logging.Logger;

import playerclient3.structures.PlayerMsgHdr;
import playerclient3.structures.accel.PlayerAccelData;
import playerclient3.xdr.OncRpcException;
import playerclient3.xdr.XdrBufferDecodingStream;

/**
 * The ir interface provides access to an array of infrared (IR) range sensors.
 * @deprecated Use the ranger instead.
 * @author Radu Bogdan Rusu, Maxim Batalin
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class AccelInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    // Logging support
    private Logger logger = Logger.getLogger (AccelInterface.class.getName ());

    private PlayerAccelData acceldata;
    private boolean      readyAcceldata = false;
    
    /**
     * Constructor for IRInterface.
     * @param pc a reference to the PlayerClient object
     */
    public AccelInterface (PlayerClient pc) { super(pc); }

    /**
     * Read the IR values.
     */
    public synchronized void readData (PlayerMsgHdr header) {
        try {
            switch (header.getSubtype ()) {
                case PLAYER_IMU_DATA_CALIB: {
                    this.timestamp = header.getTimestamp();
                    
                    acceldata = new PlayerAccelData ();
                    
                    // Buffer for reading voltages_count
                    byte[] buffer = new byte[2];

                    // Read voltages_count
                    is.readFully (buffer, 0, 2);

                    // Begin decoding the XDR buffer
                    XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
                    xdr.beginDecoding ();         
                    acceldata.setX_axis  (xdr.xdrDecodeShort ());
                    acceldata.setY_axis  (xdr.xdrDecodeShort ());
                    acceldata.setZ_axis  (xdr.xdrDecodeShort ());
                    xdr.endDecoding   ();
                    xdr.close ();

                    
                   // acceldata.setVoltages (accelArray);
                    readyAcceldata = true;     
                    break;
                }
            }
        } catch (IOException e) {
            throw new PlayerException
                ("[Accel] : Error reading payload: " +
                        e.toString(), e);
        } catch (OncRpcException e) {
            throw new PlayerException
                ("[Accel] : Error while XDR-decoding payload: " +
                        e.toString(), e);
        }
    }

    /**
     * Get the state data.
     * @return an object of type PlayerIrData containing the requested data
     */
    public PlayerAccelData getData () { return this.acceldata; }

   
    /**
     * Check if data is available.
     * @return true if ready, false if not ready
     */
    public boolean isDataReady () {
        if (readyAcceldata) {
            readyAcceldata = false;
            return true;
        }
        return false;
    }

}
