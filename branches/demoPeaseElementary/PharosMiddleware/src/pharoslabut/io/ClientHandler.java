package pharoslabut.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Handles incoming messages from a particular client.
 */
public class ClientHandler implements Runnable {
	/**
	 * The socket to the client.
	 */
	private Socket socket;
	
	/**
	 * The output streams.
	 */
	private ObjectOutputStream out;

	/**
	 * The input streams.
	 */
	private ObjectInputStream in;
	
	/**
	 * The message receiver.
	 */
	private MessageReceiver receiver;
	
	/**
	 * Whether to continue to read in data.
	 */
	boolean done = false;
	
	/**
	 * The thread that reads messages sent from the client.
	 */
//	Thread chThread;
	
	/**
	 * Creates a clientHandler.
	 * 
	 * @param socket The socket connection to the client.
	 * @param receiver The message receiver.
	 */
	public ClientHandler(Socket socket, MessageReceiver receiver) {
		this.socket = socket;
		this.receiver = receiver;
		
		// extract the input and output streams
		try {
			// be sure to create the output stream before the input stream
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		
		new Thread(this).start();
	}
	
	/**
	 * Sends a message to the client.
	 * 
	 * @param msg The message to send.
	 */
	public void sendMsg(Message msg) {
		try {
			out.writeObject(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sits in a loop listening for incoming messages.
	 */
	public void run() {
		try {
			while(!done) {
				log("Reading in object");
				Object o = in.readObject();
			
				if (o!= null) {
					if (o instanceof Message) {
						Message msg = (Message)o;
						msg.setClientHandler(this);
						receiver.newMessage(msg);
					}
					else if (o instanceof StopMsg) {
						done = true;
					}
				}
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