package aim.GUI;

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
    private JPanel queuePanel;
    private final int cellWidth = 300;
    private final int cellHeight = 100;
    private final int maxSize = aim.RobotsPriorityQueue.getDefaultCapacity();
    private GridLayout gridLayout;
    private int xPosition;
    private int yPosition;
//    private final int maxSize = 6;

    public DisplayQueue()
    {
        super("Robots Priority Queue");
        setPreferredSize(new Dimension(1500,700) );
        setBackground(Color.WHITE);
/*
        Container content = this.getContentPane();
        content.setBackground(Color.lightGray);
        content.setLayout(gridLayout);

        
        JLabel queueTitle = new JLabel("Robots Queue");
        content.add(queueTitle);
        add(queueTitle);
        content.add(queueTitle, BorderLayout.NORTH);
*/
        gridLayout = new GridLayout(maxSize,1);

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
 //       super.paintComponent(g);
        xPosition = 10;
        yPosition = 70;
        Graphics2D graphics2 = (Graphics2D) g;
        Font font = new Font("Arial", Font.BOLD, 22);
        graphics2.setFont(font);
        graphics2.drawString("Robots Queue", xPosition + 80, 55);

        font = new Font("Arial", Font.PLAIN, 12);
        graphics2.setFont(font);
        PriorityQueue<aim.Robot> queue = aim.RobotsPriorityQueue.getQueueCopy();
        String s = "";

        while(! queue.isEmpty() )
        {
            Rectangle2D rectangle = new Rectangle2D.Float(xPosition, yPosition, cellWidth, cellHeight);
            graphics2.draw(rectangle);

            aim.Robot robot = queue.remove();
            s = "Robot ID " + robot.getID();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*0);
            s = "Lane Specifications: " + robot.getLaneSpecs();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*1);
            s = "Estimated time of arrival (ETA): " + robot.getETA();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*2);
            s = "Estimated time of clearance (ETC): " + robot.getETC();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*3);
            s = "Velocity: " + robot.getVelociy();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*4);
            s = "Enqueued: " + robot.isEnqueued();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*5);
            s = "Allowed: " + robot.isAllowed();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*6);
            
            yPosition += cellHeight;
        }


        xPosition = 350;
        yPosition = 70;
        font = new Font("Arial", Font.BOLD, 22);
        graphics2.setFont(font);
        graphics2.drawString("Robots Completed", xPosition + 80, 55);

        font = new Font("Arial", Font.PLAIN, 12);
        graphics2.setFont(font);
        s = "";
        LinkedList<aim.Robot> robotsCompleted = aim.IntersectionManager.getRobotsCompleted();

        while(! robotsCompleted.isEmpty() )
        {
            Rectangle2D rectangle = new Rectangle2D.Float(xPosition, yPosition, cellWidth, cellHeight);
            graphics2.draw(rectangle);

            aim.Robot robot = robotsCompleted.removeFirst();
            s = "Robot ID " + robot.getID();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*0);
            s = "Lane Specifications: " + robot.getLaneSpecs();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*1);
            s = "Estimated time of arrival (ETA): " + robot.getETA();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*2);
            s = "Estimated time of clearance (ETC): " + robot.getETC();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*3);
            s = "Velocity: " + robot.getVelociy();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*4);
            s = "Enqueued: " + robot.isEnqueued();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*5);
            s = "Allowed: " + robot.isAllowed();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*6);

            yPosition += cellHeight;
        }


        xPosition = 690;
        yPosition = 70;
        font = new Font("Arial", Font.BOLD, 22);
        graphics2.setFont(font);
        graphics2.drawString("Robots Pending", xPosition + 80, 55);

        font = new Font("Arial", Font.PLAIN, 12);
        graphics2.setFont(font);
        s = "";
        LinkedList<aim.Robot> robotsPending = aim.IntersectionManager.getRobotsPending();

        while(! robotsPending.isEmpty() )
        {
            Rectangle2D rectangle = new Rectangle2D.Float(xPosition, yPosition, cellWidth, cellHeight);
            graphics2.draw(rectangle);

            aim.Robot robot = robotsPending.removeFirst();
            s = "Robot ID " + robot.getID();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*0);
            s = "Lane Specifications: " + robot.getLaneSpecs();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*1);
            s = "Estimated time of arrival (ETA): " + robot.getETA();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*2);
            s = "Estimated time of clearance (ETC): " + robot.getETC();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*3);
            s = "Velocity: " + robot.getVelociy();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*4);
            s = "Enqueued: " + robot.isEnqueued();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*5);
            s = "Allowed: " + robot.isAllowed();
            graphics2.drawString(s, xPosition + 20, yPosition + 20 + font.getSize()*6);

            yPosition += cellHeight;
        }
    }

    public void draw()
    {
        repaint();
    }

}
