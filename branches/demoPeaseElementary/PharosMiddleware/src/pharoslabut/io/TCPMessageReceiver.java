package pharoslabut.io;

import java.io.*;
import java.net.*;

/**
 * Accepts clients connects and listens for messages of these clients.
 *
 * @author Chien-Liang Fok
 * @version 3/17/2003
 */
public class TCPMessageReceiver implements Runnable {
    /**
	 * A reference to the object using this receiver.
	 */
    private MessageReceiver receiver;
    
    /**
	 * The server socket.
	 */
    private ServerSocket ss;
    
    /**
	 * The thread that accepts incoming connections.
	 */
    private Thread acceptThread;
	
	/**
	 * The port being listened to.
	 */
	private int port;
    
    /**
	 * This message receiver listens for TCP connections
	 * and reads in commands from them.
	 *
	 * @param receiver The message receiver two which received messages should be sent.
	 * @param port the port on which to listen for connections
	 */
    public TCPMessageReceiver(MessageReceiver receiver, int port){
    	this.receiver = receiver;
		this.port = port;
		
		// create the server socket
		while (ss == null) {
			try {
				ss = new ServerSocket(port);
			} catch (IOException e) {
				//e.printStackTrace();
				log("Unable to open server socket, waiting a couple seconds before trying again...");
				try {
					synchronized(this) { wait(1000*2); }
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		// start the thread that accepts connections
		acceptThread = new Thread(this);
		acceptThread.start();
    }
    
    /**
	 * Stop the operation of the Message Receiver.
	 * Once the MessageReceiver has been terminated, it can no
	 * longer resume operation and should be discarded.
	 */
    public void kill() {
		try {
			ss.close();
			acceptThread.join();
		} catch(Exception e){
			e.printStackTrace();
		};
    }
	
	void log(String msg) {
		String result = "TCPMessageReceiver: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
	}
    
    /**
	 * Sits in a loop waiting for clients to connect.  When a client connects,
	 * it creates a ClientHandler for it.
	 */
    public void run() {
		// continue to loop until an IOException is thrown
		try {
			while(true) {
				Socket s = null;
				log("Waiting for a connection on port " + port);
				if ((s = ss.accept()) != null) {
					log("Connection accepted from " + s.getInetAddress());
					s.setTcpNoDelay(true);
					new ClientHandler(s, receiver);
				}
			}
		} catch(IOException e) {
			//if (!ss.isClosed())
			//	e.printStackTrace();
		} finally {
			try {
				ss.close();
			} catch(Exception e){}
		}
    }
}
