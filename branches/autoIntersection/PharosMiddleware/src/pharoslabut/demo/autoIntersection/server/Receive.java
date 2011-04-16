package pharoslabut.demo.autoIntersection.server;

/**
 *
 * @author Michael Hanna
 */
public class Receive extends Thread
{
    private UDPReceiver client;
    private int port;

    public Receive(int port)
    {
        this.port = port;
        this.client = new UDPReceiver(this.port);
    }

    @Override
    public void run()
    {
        while(true)
        {
            Object object = client.receive();
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
            }
        }
    }
}
