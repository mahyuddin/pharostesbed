package pharoslabut.io;

import java.io.*;
import java.net.*;

import pharoslabut.logger.FileLogger;

/**
 * Accepts clients connects and listens for messages of these clients.
 *
 * @author Chien-Liang Fok
 * @version 08/01/2011
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
	 * A file logger for recording debug data.
	 */
	private FileLogger flogger = null;
    
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
	 * @param port the port on which to listen for connections.  If -1, select a random port that is available.
	 */
    public TCPMessageReceiver(MessageReceiver receiver, int port) {
    	this(receiver, port, null);
    }
    
    /**
	 * A constructor for creating a TCPMessageReceiver that listens to a specific port.
	 *
	 * @param receiver The message receiver two which received messages should be sent.
	 * @param port the port on which to listen for connections.  If -1, select a random port that is available.
	 * @param flogger The file logger  
	 */
    public TCPMessageReceiver(MessageReceiver receiver, int port, FileLogger flogger){
    	this.receiver = receiver;
		this.port = port;
		this.flogger = flogger;
		
		// create the server socket
		while (ss == null) {
			try {
				if (port != -1)
					ss = new ServerSocket(port);
				else {
					ss = new ServerSocket(0);
				}
			} catch (IOException e) {
				logErr("Unable to open server socket, waiting a couple seconds before trying again...");
				try {
					synchronized(this) { wait(1000*2); }
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		log("Starting the thread that accepts connections...");
		acceptThread = new Thread(this);
		acceptThread.start();
    }
    
    /**
     * Sets the file logger.
     */
    public void setFileLogger(FileLogger flogger) {
    	this.flogger = flogger;
    }
    
    /**
     * @return The port on which this receiver is listening.
     */
    public int getPort() {
    	return ss.getLocalPort();
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
    
    /**
	 * Sits in a loop waiting for clients to connect.  When a client connects,
	 * it creates a ClientHandler for it.
	 */
    public void run() {
		
    	while(!ss.isClosed()) {
			log("run: Waiting for a connection on port " + port);
			
    		try {
    			Socket s = ss.accept();
				if (s != null) {
					log("run: Connection accepted, passing to client handler.");
					s.setTcpNoDelay(true);
					new ClientHandler(s);
				} else {
					logErr("run: got a null socket connection.");
				}
    		} catch(IOException e) {
    			e.printStackTrace();
    			logErr("run: IOException while accepting client connections. ServerSocket.isClosed = " + ss.isClosed());
    		}
    	}
    }
    
//    private void pause(long interval) {
//    	synchronized(this) {
//			try {
//				this.wait(interval);
//			} catch (InterruptedException e1) {	}
//		}
//    }
    
    private void logErr(String msg) {
		String result = "TCPMessageReceiver: ERROR: " + msg;
		System.err.println(result);
		
		if (flogger != null)
			flogger.log(result);
	}
    
	private void log(String msg) {
		String result = "TCPMessageReceiver: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		
		if (flogger != null)
			flogger.log(result);
	}
    
    /**
	 * Handles incoming messages from a particular client.
	 */
    private class ClientHandler implements Runnable {
		/**
		 * The sock et to the client.
		 */
		private Socket socket;
		
		/**
		 * The output stream.
		 */
		private OutputStream os;
		
		/**
		 * The object output stream.
		 */
		private ObjectOutputStream oos;

		/**
		 * The object input stream.
		 */
		private ObjectInputStream in;

		/**
		 * Creates a clientHandler.
		 */
		public ClientHandler(Socket socket) {
			this.socket = socket;
			new Thread(this).start();
		}
		
		/**
		 * Sits in a loop listening for incoming messages.
		 */
		public void run() {
			// extract the input and output streams
			// be sure to create the output stream before the input stream
			try {
				os = socket.getOutputStream();
				oos = new ObjectOutputStream(os);
				in = new ObjectInputStream(socket.getInputStream());
			} catch(IOException e) {
				logErr("run: IOException while extracing output and input streams.");
				e.printStackTrace();
				return;
			}
			
			Message msg = null;
			
			try {
				log("run: Awaiting message...");
				Object o = in.readObject();
				
				if (o != null) {
					if (o instanceof Message) {
				
						msg = (Message)o;
					} else {
						logErr("run: received message was not a message!");
					}
				} else
					logErr("run: received message was null!");		
			} catch(ClassNotFoundException e) {
				logErr("run: ClassNotFoundException while receiving.");
				e.printStackTrace();
			} catch(IOException e) {
				logErr("run: IOException while receiving.");
				e.printStackTrace();
			}
			
			if (msg != null) {
				log("run: Received message: " + msg);
				receiver.newMessage(msg);
			
				try {
					if (msg instanceof AckedMsg) {
						log("run: message was an AckedMsg, sending ack.");
						PharosAckMsg ackMsg = PharosAckMsg.getAckMsg();
						oos.writeObject(ackMsg);
						oos.flush();
						os.flush();
						log("run: ack sent!");
					}
				} catch(IOException e) {
					logErr("run: IOException while receiving.");
					e.printStackTrace();
				}
			}
			
			try {
				socket.close();
			} catch(Exception e) {
			}
		}
		
		void logErr(String msg) {
			String result = "TCPMessageReceiver: ClientHandler: ERROR: " + msg;
			System.err.println(result);
			if (flogger != null)
				flogger.log(result);
		}
		
		void log(String msg) {
			String result = "TCPMessageReceiver: ClientHandler: " + msg;
			
			if (System.getProperty ("PharosMiddleware.debug") != null)
				System.out.println(result);
			
			if (flogger != null)
				flogger.log(result);
		}
    }
}
