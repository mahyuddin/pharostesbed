package aim.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.PriorityQueue;


/**
 *
 * @author ut
 */
public class oldDisplayQueue extends JFrame
{
    private JPanel queuePanel;
    private QueueCell[] queueCellPanel;
    private final int cellWidth = 500;
    private final int cellHeight = 100;
    private final int maxSize = aim.RobotsPriorityQueue.getDefaultCapacity();
    private GridLayout gridLayout = new GridLayout(maxSize,1);
    private int xPosition  = 10;
    private int yPosition  = 10;
//    private final int maxSize = 6;

    public oldDisplayQueue()
    {
        super("Robots Priority Queue");
    }
    public void addQueuetoJFrame(Container pane)
    {
 //       Container content = getContentPane();
 //       content.setBackground(Color.lightGray);
 //       content.setLayout(gridLayout);

        JLabel queueTitle = new JLabel("Robots Queue");
        pane.add(queueTitle, BorderLayout.NORTH);

        queuePanel = new JPanel();
        queuePanel.setLayout(new BorderLayout());
        Dimension queueSize = new Dimension(cellWidth+20, cellHeight*maxSize+200);
        queuePanel.setPreferredSize(queueSize);
        queueCellPanel = new QueueCell[maxSize];

//        JScrollPane verticalScrollBar = new JScrollPane();
//        verticalScrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//        this.getContentPane().add(verticalScrollBar);
        //Set up components preferred size
//        JPanel jp = new JPanel();
 //       Dimension queueCellSize = jp.getPreferredSize();
 //       queuePanel.setPreferredSize(new Dimension((int)(queueCellSize.getWidth() * 2.5)+20,
 //               (int)(queueCellSize.getHeight() * 3.5)+20 * 2));

//        queuePanel.setPreferredSize(new Dimension((int)(queueCellSize.getWidth()), (int)(queueCellSize.getHeight() )));


        for(int i=0; i<maxSize; i++)
        {
            queueCellPanel[i] = new QueueCell();
  //          Dimension cellSize = new Dimension(cellWidth,cellHeight);
  //          queueCellPanel[i].setPreferredSize(cellSize);

//            queueCellPanel[i].setSize(cellWidth, cellHeight);

            String s = "";
            PriorityQueue<aim.Robot> queue = aim.RobotsPriorityQueue.getQueueCopy();
            if(! queue.isEmpty() )
            {
                aim.Robot robot = queue.remove();
                s += "<html>";
                s += "Robot ID " + robot.getID() + "<br>";
                s += "Lane Specifications: " + robot.getLaneSpecs() + "<br>";
                s += "Estimated time of arrival (ETA): " + robot.getETA() + "<br>";
                s += "Estimated time of clearance (ETC): " + robot.getETC() + "<br>";
                s += "Velocity: " + robot.getVelociy();
                s += "</html>";
            }
            JLabel cellData = new JLabel();
            cellData.setText(s);
            queueCellPanel[i].add(cellData);
            System.out.println(yPosition);
            queueCellPanel[i].setLocation(xPosition, yPosition);
            queuePanel.add(queueCellPanel[i]);
            yPosition += cellHeight;
        }
        pane.add(queuePanel, BorderLayout.WEST);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method is invoked from the
     * event dispatch thread.
     */
    public static void createAndShowGUI() {
        //Create and set up the window.
        oldDisplayQueue frame = new oldDisplayQueue();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Set up the content pane.
        frame.addQueuetoJFrame(frame.getContentPane());
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void test()
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}


class QueueCell extends JPanel
{
    private final int cellWidth = 500;
    private final int cellHeight = 100;

    public void paintComponent( Graphics g )
    {
        super.paintComponent(g);
        Graphics2D graphics2 = (Graphics2D) g;
        Rectangle2D rectangle = new Rectangle2D.Float(0, 0, cellWidth, cellHeight);
        graphics2.draw(rectangle);
//        super.paintComponent( g );
//        g.fillRect( 10, 10, cellWidth-10, cellHeight-10 );
    }

    public void draw()
    {
        repaint();
    }
}
