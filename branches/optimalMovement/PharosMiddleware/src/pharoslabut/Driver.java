package pharoslabut;
//import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;

import javax.swing.*;

/*************************************************************************
**PAV GUI Driver														**
**Author: Devin Murphy													**
**Date: 1/24/2011														**
**Version: 1.1															**
**Last Modified: 4/12/2011												**
**About:																**
**																		**
**																		**
*************************************************************************/

class Driver implements ActionListener, WindowStateListener
{
	private GUI pavPanel;
	private JFrame f;
	Driver(){
		f = new JFrame();
		pavPanel = new GUI(f);
		f.setTitle("PAV Control Panel");
		f.setSize(800, 700);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setJMenuBar(createMainMenu());
		f.add(pavPanel);
		Dimension newMinSize = pavPanel.newMapSize();
		f.setMinimumSize(newMinSize);
		f.setVisible(true);
		f.addWindowStateListener(this);
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				pavPanel.canvasMaxRestore(800, 700);
			}
		});
		
	}
	
	private JMenuBar createMainMenu()
	{
		JMenuBar mainMenu = new JMenuBar();
		
		//Create the File menu
		JMenu fileMenu = new JMenu("File");
		//JMenuItem openMI = new JMenuItem("Open");
		//JMenuItem closeMI = new JMenuItem("Close");
		JMenuItem saveMI = new JMenuItem("Save");
		JMenuItem exitMI = new JMenuItem("Exit");
		//fileMenu.add(openMI);
		//fileMenu.add(closeMI);
		fileMenu.add(saveMI);
		fileMenu.add(exitMI);
		
		mainMenu.add(fileMenu);
		
		//Create options menu
		JMenu optionsMenu = new JMenu("Options");
		
		//Create the map sub-menu
		JMenu mapMenu = new JMenu("Map");
		JMenuItem selectMapMI = new JMenuItem("Select Map");
		JMenuItem resetDefaultMapMI = new JMenuItem("Reload Default Map");
		mapMenu.add(selectMapMI);
		mapMenu.add(resetDefaultMapMI);
		
		JMenuItem setInitLocMI = new JMenuItem("Set Initial Location");
		
		optionsMenu.add(mapMenu);
		optionsMenu.add(setInitLocMI);
		
		mainMenu.add(optionsMenu);
		
		//Create help menu
		JMenu helpMenu = new JMenu("Help");
		JMenuItem aboutMI = new JMenuItem("About");
		helpMenu.add(aboutMI);
		
		mainMenu.add(helpMenu);
		
		//add action listeners to menu items
		//openMI.addActionListener(this);
		//closeMI.addActionListener(this);
		saveMI.addActionListener(this);
		exitMI.addActionListener(this);
		selectMapMI.addActionListener(this);
		resetDefaultMapMI.addActionListener(this);
		setInitLocMI.addActionListener(this);
		aboutMI.addActionListener(this);
		
		return mainMenu;
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		String commandString = ae.getActionCommand();
		if(commandString.equals("Exit"))
		{
			System.exit(0);
		}
		else if(commandString.equals("Set Initial Location"))
		{
			pavPanel.initializeLocation();
		}
		else if(commandString.equals("Select Map"))
		{
			pavPanel.selectMap();
			Dimension newMinSize = pavPanel.newMapSize();
			f.setMinimumSize(newMinSize);
			//System.out.println("minimum window size from Driver = "+f.getMinimumSize().getWidth()+" x "+f.getMinimumSize().getHeight()+".");
		}
		else if(commandString.equals("Reload Default Map"))
		{
			pavPanel.setDefaultMap();
		}
		else
		{
			pavPanel.setMessageDisplay(commandString);
		}
	}
	
	public void windowStateChanged(WindowEvent w)
	{
		Dimension windowSize = w.getComponent().getSize();
		final int height = (int)windowSize.getHeight();
		final int width = (int)windowSize.getWidth();
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				pavPanel.canvasMaxRestore(width, height);
			}
		});
		//pavPanel.setMessageText("pavPanel size = "+width+" x "+height+".");
	}
	
	public static void main(String[] args) 
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				new Driver();
			}
		});
		
	}
}
