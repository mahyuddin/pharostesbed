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
	 * A constructor for creating a TCPMessageReceiver that listens to any free port.
	 *
	 * @param receiver The message receiver two which received messages should be sent.
	 * @param port the port on which to listen for connections
	 */
    public TCPMessageReceiver(MessageReceiver receiver){
    	this(receiver, 0);
    	port = ss.getLocalPort();
    }
	
    /**
	 * A constructor for creating a TCPMessageReceiver that listens to a specific port.
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
     * @return The port on which this receiver is listening.
     */
    public int getPort() {
    	return port;
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
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("TCPMessageReceiver: " + msg);
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
					log("Connection accepted, passing to client handler.");
					s.setTcpNoDelay(true);
					new ClientHandler(s);
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
    
    /**
	 * Handles incoming messages from a particular client.
	 */
    private class ClientHandler implements Runnable {
		/**
		 * The sock et to the client.
		 */
		Socket socket;
		/**
		 * The output streams.
		 */
//		ObjectOutputStream out;
		/**
		 * The input streams.
		 */
		ObjectInputStream in;
		/**
		 * The thread that reads messages sent from the client.
		 */
//		Thread chThread;
		/**
		 * Creates a clientHandler.
		 */
		public ClientHandler(Socket socket) {
			this.socket = socket;
			
			// extract the input and output streams
			try {
				// be sure to create the output stream before the input stream
//				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
			} catch(IOException e) {
				e.printStackTrace();
				return;
			}
			
			new Thread(this).start();
		}
		
		/**
		 * Sits in a loop listening for incoming messages.
		 */
		public void run() {
			try {
				log("Reading in object");
				Object o = in.readObject();
				
				if (o!= null && o instanceof Message) {
					Message msg = (Message)o;
					receiver.newMessage(msg);
				}
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch(Exception e) {}
			}
		}
		
		void log(String msg) {
			if (System.getProperty ("PharosMiddleware.debug") != null)
				System.out.println("TCPMessageReceiver: ClientHandler: " + msg);
		}
    }
}
