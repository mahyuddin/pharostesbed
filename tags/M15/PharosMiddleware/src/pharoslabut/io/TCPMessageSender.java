package pharoslabut.io;

import java.io.*;
import java.net.*;

/**
 * Sends messages to other LimeLiteServers using TCP.
 * A TCP connection to another host is kept alive until a
 * disconnect occurs <i>and</i> a message is sent to the
 * disconnected agent.
 *
 * @author Chien-Liang Fok
 * @version 3/17/2003
 */
public class TCPMessageSender implements MessageSender {
    
    /**
     * Creates a TCPMessageSender.
     */
    public TCPMessageSender() {
    }
    
    /**
     * Forces the TCPMessageSender to close all sockets and stop
     * functioning.
     */
    public void kill() {
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }
    
    /**
     * Sends a message via a TCP socket.
     *
     * @param msg the message to be sent.
     */
    public void sendMessage(InetAddress address, int port, Message msg) {

            
            // open a TCP socket to the destination host
            try {
				log("Opening TCP socket to " + address + ":" + port);
                Socket socket = new Socket(address, port);
                socket.setTcpNoDelay(true);
                
                OutputStream os = socket.getOutputStream();
//                InputStream is = socket.getInputStream();
                
                ObjectOutputStream oos = new ObjectOutputStream(os);
//                ObjectInputStream ois = new ObjectInputStream(is);
                
				log("Sending the object to the destination.");
                oos.writeObject(msg);
                oos.flush();
                os.flush();
                
				log("Closing the socket to the destination host.");
                socket.close();
                
            } catch(Exception e) {
                e.printStackTrace();
            }
    }
	
	void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("TCPMessageSender: " + msg);
	}
}
