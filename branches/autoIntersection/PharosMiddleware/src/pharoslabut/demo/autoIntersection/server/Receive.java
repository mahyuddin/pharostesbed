package pharoslabut.demo.autoIntersection.server;

import java.net.InetAddress;

/**
 *
 * @author Michael Hanna
 */
public class Receive extends Thread
{
    private UDPReceiver client;
    private int port;

    public Receive(int port, String ipAdd)
    {
        this.port = port;
        this.client = new UDPReceiver(this.port, ipAdd);
    }

    @Override
    public void run()
    {
        while(true)
        {
            /*Object object = client.receive();
            if( (object != null) && (object instanceof Robot) )
            {
                Robot robot = (Robot) object;

                if( robot.isExited() )
                {
                    // destroy robot
                    IntersectionManager.robotsCompleted.remove(robot);
                }
                else
                {
                    if(! robot.isEnqueued() )
                    {
                    	robot.setEnqueued(true);
                        RobotsPriorityQueue.enqueue(robot);
                    }
                }
            }*/
        }
    }
}
