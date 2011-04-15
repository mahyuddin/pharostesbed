package pharoslabut.demo.autoIntersection.AIM.src.aim;

/**
 *
 * @author Michael Hanna
 */
public class Receive extends Thread
{
    private UDPClient client;
    private int port;
    private IntersectionManager IM;

    public Receive(IntersectionManager IM, int port)
    {
        this.IM = IM;
        this.port = port;
        this.client = new UDPClient(port);
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
                    IM.robotsCompleted.remove(robot);
                }
                else
                {
                    if(! robot.isEnqueued() )
                    {
                        RobotsPriorityQueue.enqueue(robot);
                        robot.setEnqueued(true);
                    }
                }
                System.out.println(robot);
            }
        }
    }
}
