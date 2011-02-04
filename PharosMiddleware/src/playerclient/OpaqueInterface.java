package playerclient;

import java.util.*;
import playerclient.structures.PlayerMsgHdr;

/**
 * The OpaqueInterface is used to exchange custom data between the PlayerServer
 * and the Player Client.
 * 
 * @author Chien-Liang Fok
 *
 */
public class OpaqueInterface extends PlayerDevice {

	public OpaqueInterface(PlayerClient pc) { 
		super(pc); 
	}
	
    protected void handleResponse (PlayerMsgHdr header) {
        
    }
	
}
