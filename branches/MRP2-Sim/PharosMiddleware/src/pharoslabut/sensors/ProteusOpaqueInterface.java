package pharoslabut.sensors;

import java.util.Enumeration;
import java.util.Vector;

import playerclient3.structures.PlayerMsgHdr;
import playerclient3.xdr.XdrBufferDecodingStream;
import playerclient3.OpaqueInterface;
import playerclient3.PlayerClient;

/**
 * An implementation of the Opaque interface for the Proteus robotic platform.
 * 
 * @author Chien-Liang Fok
 */
public class ProteusOpaqueInterface extends OpaqueInterface {

	private Vector<ProteusOpaqueListener> listeners = new Vector<ProteusOpaqueListener>();
	
	/**
	 * The constructor.
	 * @param pc
	 */
	public ProteusOpaqueInterface(PlayerClient pc) {
		super(pc); 
	}
	
	public synchronized void readData (PlayerMsgHdr header) { 
		//System.out.println("OpaqueInterface: readData called, header:\n" + header.toString());

		//if (header.getSubtype() == PLAYER_OPAQUE_DATA_STATE) {
		int dataCount = 0;
		byte[] data = null;

		// Read data_count
		try {
			byte[] buffer = new byte[header.getSize()];
			is.readFully (buffer, 0, buffer.length);

			/*System.out.print("Bytes received: [" + buffer[0]);
				for (int i=1; i < buffer.length; i++) {
					System.out.print(", " + buffer[i]);
				}
				System.out.println("]");*/


			XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
			xdr.beginDecoding ();

			// read in the data count variable
			dataCount = xdr.xdrDecodeInt();

			// read in the array of data
			int arraySize = xdr.xdrDecodeInt();
			data = xdr.xdrDecodeOpaque(arraySize);

			xdr.endDecoding();
			xdr.close ();

			ProteusOpaqueData pod = new ProteusOpaqueData(dataCount, data);
			notifyListeners(pod);

		} catch (Exception e) {
			e.printStackTrace();
		}

		/*System.out.println("dataCount = " + dataCount);
			System.out.print("Data: [" + data[0]);
			for (int i=1; i < data.length; i++) {
				System.out.print(", " + data[i]);
			}
			System.out.println("]");
			String s = new String(data);
			System.out.println("String: \"" + s + "\"");*/			
		//}
	}
	
	public void addOpaqueListener(ProteusOpaqueListener l) {
		listeners.add(l);
	}
	
    /**
     * Notifies each of the registered GPSListener objects that a new PlayerGpsData is available.
     */
    private void notifyListeners(ProteusOpaqueData pod) {
    	Enumeration<ProteusOpaqueListener> e = listeners.elements();
    	while (e.hasMoreElements()) {
    		e.nextElement().newOpaqueData(pod);
    	}
    }

}
