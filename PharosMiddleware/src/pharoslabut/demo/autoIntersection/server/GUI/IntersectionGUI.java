package pharoslabut.demo.autoIntersection.server.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.border.TitledBorder;

/**
 * @author Maykel Hanna
 *
 */
public class IntersectionGUI {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1656058896690229091L;
	
	public static final int frameWidth = 900;
	public static final int frameHeight = 700;
	
	private static Container contentPane; 
	
	public IntersectionGUI() {
	}

	
	/**
     * Create the GUI and show it.  For thread safety,
     * this method is invoked from the
     * event dispatch thread.
     */
	public static void createAndShowGUI()
    {
		//Create and set up the window by calling the constructor

		JFrame frame = new JFrame("Intersection");
		contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
        
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(frameWidth,frameHeight) );
        
        ((JComponent) contentPane).setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 3), "Autonomous Intersection Manager",
    			TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 22), Color.RED ) );
        
        contentPane.setBackground(Color.BLACK);
        contentPane.add(new IntersectionPanel(), BorderLayout.CENTER);
        contentPane.add(new LogPanel(), BorderLayout.PAGE_END);

        frame.pack();
        frame.setVisible(true);
    }
}