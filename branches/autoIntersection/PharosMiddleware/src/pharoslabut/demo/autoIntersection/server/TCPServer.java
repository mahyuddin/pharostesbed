package pharoslabut.demo.autoIntersection.server;

import java.io.*;
import java.net.*;
import pharoslabut.demo.autoIntersection.*;


/////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Class Connect //////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/**
 * 
 * NOTE: THIS SHOULD NO LONGER BE USED!!!
 * 
 * The server to which the robots will connect
 * @author Michael Hanna
 */
public class TCPServer extends Thread
{
    private ServerSocket srvr;

    public static void main(String [] args)
    {
        new TCPServer();
    }

    /**
     * The server starts a new socket on port 6665
     */
    public TCPServer()
    {
        try {
            srvr = new ServerSocket(6665);
        }
        catch(Exception e) {
            System.out.print("Whoops! It didn't work!\n");
            System.out.println(e.getMessage());
        }
        System.out.println("Server listening on port 6665.");
        this.start();

    }

    public void run()
    {
//        while(true)
//        {
            try
            {
                System.out.println("Waiting for connections.");
                Socket client = srvr.accept();
                System.out.println("Accepted a connection from: " + client.getInetAddress());
                Connect c = new Connect(client);

                /*
                String data = "HELOO AIMMM";
                System.out.print("TCPServer has connected!\n");
                PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
                System.out.print("Sending string: '" + data + "'\n");
                out.print(data);
                out.close();
                skt.close();
                srvr.close();
                 *
                 */
            }
            catch(Exception e)
            {
                System.out.print("Whoops! It didn't work!!!\n");
                System.out.println(e.getMessage() + e);
            }
 //       }
   }


}



class Connect extends Thread
{
    private Socket clientSkt = null;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;

    public Connect() {}


    /**
     * sends a Robot object over the socket
     * @param clientSkt
     */
    public Connect(Socket clientSkt)
    {
        this.clientSkt = clientSkt;
        try
        {
            ois = new ObjectInputStream(clientSkt.getInputStream());
            oos = new ObjectOutputStream(clientSkt.getOutputStream());
        }
        catch(Exception e1) {
            System.out.print("Whoops! It didn't work!\n");
            System.out.println(e1.getMessage());
            try {
                clientSkt.close();
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
            }
            return;
        }
        this.start();
    }


    /**
     * Staring a new Thread
     * Sending the Robot object over the Socket
     */
    public void run()
    {
        try {
         oos.writeObject(new Robot(null, 0, new LaneSpecs(), (long)1.2, (long)1.3 ) );
         oos.flush();
         // close streams and connections
         ois.close();
         oos.close();
         clientSkt.close();
        }
        catch(Exception e) {
            System.out.print("Whoops! It didn't work!\n");
            System.out.println(e.getMessage());
        }
    }

}

