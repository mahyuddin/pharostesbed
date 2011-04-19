package pharoslabut.demo.simonsays;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CPPTable {
	
	//private boolean isDisplayed = true;
	
	private JFrame frame = new JFrame("Cyber-physical Command Executions");
	
	private CPPData tableModel;
	
	private JFrame parentFrame;
	
	private ProgramEntryGUI mainGUI;
	
	/**
	 * The constructor.
	 * 
	 * @param tableModel The table model.
	 */
	public CPPTable(ProgramEntryGUI mainGUI, CPPData tableModel) {
		this.mainGUI = mainGUI;
		this.tableModel = tableModel;

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
            }
        });
	}
	
	/**
	 * Show the CPP Table.
	 */
	public void show() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        });
	}
	
	/**
	 * Hide the CPP Table.
	 */
	public void hide() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(false);
            }
        });
	}
	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createGUI() {
    	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	
    	// Create the table and add it to a scroll pane...
        final JTable table = new JTable(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(500, 450));
        table.setFillsViewportHeight(true);
        JScrollPane tableScrollPane = new JScrollPane(table);
        frame.getContentPane().add(tableScrollPane, BorderLayout.CENTER);
        
        
        frame.addWindowListener(new WindowAdapter() {
        	public void windowClosed(WindowEvent we) {
        		mainGUI.CPPTableClosed();
        	}
        });
        
    	frame.pack();
    	frame.setLocationRelativeTo(mainGUI.getFrame()); // center frame
    }
}
