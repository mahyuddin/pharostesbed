package pharoslabut.io;

import java.io.*;
import java.net.*;

import pharoslabut.logger.FileLogger;

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
    private InetAddress address;
    private int port;
    private FileLogger flogger = null;
    
	private Socket socket;
	
	private OutputStream os;
	private InputStream is;
	
	private ObjectOutputStream oos;
	
	private ObjectInputStream ois;
	
    /**
     * Creates a TCPMessageSender.
     */
    public TCPMessageSender(InetAddress address, int port, FileLogger flogger) throws IOException {
    	this.address = address;
    	this.port = port;
    	this.flogger = flogger;
    	connect();
    }
    
    private void connect() throws IOException {
    	log("Opening TCP socket to " + address + ":" + port);
    	socket = new Socket(address, port);
    	socket.setTcpNoDelay(true);

    	os = socket.getOutputStream();
    	is = socket.getInputStream();

    	oos = new ObjectOutputStream(os);
    	ois = new ObjectInputStream(is);
    }
    
    /**
     * Forces the TCPMessageSender to close all sockets and stop
     * functioning.
     */
    public void kill() {
    	try {
    		socket.close();
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	socket = null;
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }
    
    /**
     * Sends a message via a TCP socket.
     *
     * @param msg the message to be sent.
     * @return Whether the send was successful.
     */
    public boolean sendMessage( Message msg) {
    	
    	// If a connection to the server was not established, establish it.
    	while (socket == null) {
    		try {
				connect();
			} catch (IOException e) {
				log("Unable to connect to server.  Pausing 1 second and then trying again...");
				//e.printStackTrace();
				synchronized(this) {
					try {
						wait(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
    	}
    	
    	try {                
    		log("Sending the object to the destination.");
    		oos.writeObject(msg);
    		oos.flush();
    		os.flush();
    		return true;
    	} catch(Exception e) {
    		kill();
    		e.printStackTrace();
    		return false;
    	}
    }
    
    /**
     * Receives a message over TCP.
     * 
     * @return The message received or null if error.
     */
    public Message receiveMessage() {
    	Object o = null;
    	
    	try {
			o = ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    	return (Message)o;
    }
	
	void log(String msg) {
		String result = "TCPMessageSender: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
