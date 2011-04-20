package pharoslabut.demo.autoIntersection.server.GUI;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.PriorityQueue;
import javax.swing.*;

/**
 *
 * @author Michael Hanna
 */
public class DisplayQueue extends JFrame
{
	private static final long serialVersionUID = 1L;
	private final int cellWidth = 300;
    private final int cellHeight = 100;
//    private final int maxSize = pharoslabut.demo.autoIntersection.AIM.src.aim.RobotsPriorityQueue.getDefaultCapacity();
    private int xPosition;
    private int yPosition;
//    private final int maxSize = 6;

    public DisplayQueue()
    {
        super("Robots Priority Queue");
        setPreferredSize(new Dimension(700,700) );
        setBackground(Color.WHITE);
        this.repaint();
/*
        Container content = this.getContentPane();
        content.setBackground(Color.lightGray);
        content.setLayout(gridLayout);

        
        JLabel queueTitle = new JLabel("Robots Queue");
        content.add(queueTitle);
        add(queueTitle);
        content.add(queueTitle, BorderLayout.NORTH);
*/

    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method is invoked from the
     * event dispatch thread.
     */
    public static void createAndShowGUI()
    {
        //Create and set up the window by caaling the constructor
        DisplayQueue frame = new DisplayQueue();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /*
    public static void test()
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
     *
     */


    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        xPosition = 10;
        yPosition = 70;
        Graphics2D graphics2 = (Graphics2D) g;
        Font font = new Font("Arial", Font.BOLD, 22);
        graphics2.setFont(font);
        graphics2.drawString("Robots Priority Queue", xPosition + 40, 55);

        font = new Font("Arial", Font.PLAIN, 12);
        graphics2.setFont(font);
        PriorityQueue<pharoslabut.demo.autoIntersection.server.Robot> queue = pharoslabut.demo.autoIntersection.server.RobotsPriorityQueue.getQueueCopy();
        String s = "";

        while(! queue.isEmpty() )
        {
            Rectangle2D rectangle = new Rectangle2D.Float(xPosition, yPosition, cellWidth, cellHeight);
            graphics2.draw(rectangle);

            pharoslabut.demo.autoIntersection.server.Robot robot = queue.remove();
            s = "Robot ID " + robot.getIP() + ":" + robot.getPort();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*0);
            s = "Lane Specifications: " + robot.getLaneSpecs();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*1);
            s = "Estimated time of arrival (ETA): " + robot.getETA();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*2);
            s = "Estimated time of clearance (ETC): " + robot.getETC();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*3);
            s = "Enqueued: " + robot.isEnqueued();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*4);
            s = "Exited: " + robot.isExited();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*5);
            s = "Acknowledged: " + robot.isAcknowledged();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*6);
            
            yPosition += cellHeight;
        }


        xPosition = 350;
        yPosition = 70;
        font = new Font("Arial", Font.BOLD, 22);
        graphics2.setFont(font);
        graphics2.drawString("Robots Completed", xPosition + 60, 55);

        font = new Font("Arial", Font.PLAIN, 12);
        graphics2.setFont(font);
        s = "";
        LinkedList<pharoslabut.demo.autoIntersection.server.Robot> robotsCompleted = pharoslabut.demo.autoIntersection.server.IntersectionManager.getRobotsCompletedCopy();

        while(! robotsCompleted.isEmpty() )
        {
            Rectangle2D rectangle = new Rectangle2D.Float(xPosition, yPosition, cellWidth, cellHeight);
            graphics2.draw(rectangle);

            pharoslabut.demo.autoIntersection.server.Robot robot = robotsCompleted.removeFirst();
            s = "Robot ID " + robot.getIP() + ":" + robot.getPort();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*0);
            s = "Lane Specifications: " + robot.getLaneSpecs();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*1);
            s = "Estimated time of arrival (ETA): " + robot.getETA();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*2);
            s = "Estimated time of clearance (ETC): " + robot.getETC();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*3);
            s = "Enqueued: " + robot.isEnqueued();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*4);
            s = "Exited: " + robot.isExited();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*5);
            s = "Acknowledged: " + robot.isAcknowledged();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*6);

            yPosition += cellHeight;
        }

        this.repaint(1000);
    }

    public void draw()
    {
        repaint();
    }

}
