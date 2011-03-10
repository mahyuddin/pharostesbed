package aim.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 *
 * @author ut
 */
public class DisplayQueue extends JFrame
{
    private QueueCell[] queueCellPanel;

    public DisplayQueue()
    {
        super("Robots Priority Queue");
        Container content = getContentPane();
        content.setBackground(Color.lightGray);

        int maxSize = aim.RobotsPriorityQueue.getDefaultCapacity();
        queueCellPanel = new QueueCell[maxSize];
        for(int i=0; i<maxSize; i++)
        {
            queueCellPanel[i] = new QueueCell();
            queueCellPanel[i].setBounds(0, 0, cellWidth, cellHeight);
            queueCellPanel[i].add(this);
        }
        queuePanel.setLayout( new GridLayout( 1, 2 ) );

        content.add( queuePanel, BorderLayout.CENTER );

        setSize( 300, 150 );
        pack();
        setVisible(true);
    }

    public static void main(String [] args)
    {
        DisplayQueue dq = new DisplayQueue();

        dq.addWindowListener(
            new WindowAdapter() {
                public void windowClosing( WindowEvent e )
                {
                    System.exit( 0 );
                }
            }
        );
    }

}




class QueueCell extends JPanel
{
    private final int cellWidth = 500;
    private final int cellHeight = 100;

    public void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        g.fillRect( 10, 10, cellWidth, cellHeight );
    }

    public void draw()
    {
        repaint();
    }
}
