package pharoslabut.io;

import java.io.*;
import java.net.*;

//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

public class TCPNetworkInterface extends NetworkInterface {
	
	private TCPReceiver rcvr;
	
    /**
	 * The server socket.
	 */
    private ServerSocket ss = null;
    
    /**
     * The port on which to listen for incoming message.  If -1, then select a random port that is available.
     */
	private int port;
	
    /**
     * Creates a TCPNetworkInterface that listens on a random port.
     */
	public TCPNetworkInterface() {
		this(-1);
	}
	
	/**
     * Creates a UDPNetworkInterface that listens on a specific port.
     * 
     * @param port The port on which to listen.
     */
	public TCPNetworkInterface(int port) {
		this.port = port;
		openSocket();
	}
	
	/**
	 * Creates the server socket.
	 */
	private void openSocket() {
		while (ss == null) {
			try {
				if (port != -1)
					ss = new ServerSocket(port);
				else
					ss = new ServerSocket(0); // use any free port
				Logger.log("Server socket listening on port " + getLocalPort());
			} catch (IOException e) {
				//e.printStackTrace();
				Logger.logErr("Unable to open server socket, waiting a couple seconds before trying again...");
				try {
					synchronized(this) { wait(1000*2); }
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		rcvr = new TCPReceiver();
	}
	
	@Override
	public int getLocalPort() {
		if (ss == null)
			openSocket();
		
		return ss.getLocalPort();
	}

	@Override
	public boolean sendMessage(InetAddress address, int port, Message m) {
        // open a TCP socket to the destination host
        try {
        	Logger.log("Opening TCP socket to " + address + ":" + port);
            Socket socket = new Socket(address, port);
            socket.setTcpNoDelay(true);
            
            OutputStream os = socket.getOutputStream();
//            InputStream is = socket.getInputStream();
            
            ObjectOutputStream oos = new ObjectOutputStream(os);
//            ObjectInputStream ois = new ObjectInputStream(is);
            
            Logger.log("Sending the object to the destination.");
            oos.writeObject(m);
            oos.flush();
            os.flush();
            
            Logger.log("Closing the socket to the destination host.");
            socket.shutdownOutput();
			socket.shutdownInput();
            socket.close();
            
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
	}

	@Override
	public void stop() {
		rcvr.stop();
	}
	
	/**
	 * Listens for incoming messages.  Executes using its own thread.
	 */
	private class TCPReceiver implements Runnable {
		boolean running = true;
		
		public TCPReceiver() {
			new Thread(this).start();
		}
		
		/**
		 * Stops this UDPReceiver.
		 */
		public void stop() {
			running = false;
		}
		
		public void run() {
			
			while(running) {
				if (ss == null) 
					openSocket();
				if (ss != null) {
					try {   // continue to loop until an IOException is thrown
						while(true) {
							Socket s = null;
							Logger.log("Waiting for a connection on port " + getLocalPort());
							if ((s = ss.accept()) != null) {
								Logger.log("Connection accepted, passing to client handler.");
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
			}
			
			if (ss != null) {
				try { ss.close(); } catch (IOException e) {}  // Do this regardless of exception...
				ss = null;
			}
			
			Logger.log("thread exiting...");
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
				Logger.log("Reading in object...");
				Object o = in.readObject();
				
				if (o!= null && o instanceof Message) {
					Message msg = (Message)o;
					newMessage(msg);
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
    }
	
//	private void log(String msg) {
//		log(msg, true);
//	}
//	
//	private void log(String msg, boolean isDebugMsg) {
//		String result = "TCPNetworkInterface: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null || !isDebugMsg) {
//			System.out.println(result);
//			System.out.flush();
//		}
//		if (flogger != null)
//			flogger.log(result);
//	}
}
