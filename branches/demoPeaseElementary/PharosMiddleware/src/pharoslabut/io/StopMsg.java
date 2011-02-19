package pharoslabut.io;

import java.io.Serializable;

/**
 * Sent by the client to the server to tell the server to close the TCP connection.
 * 
 * @author Chien-Liang Fok
 */
public class StopMsg implements Serializable {

	private static final long serialVersionUID = -6834916043208881478L;

	public StopMsg() {
		
	}
	
}
