package pharoslabut;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class HeadingSelectDialog extends Dialog implements ActionListener{
	private JLabel message;
	private JButton northButton;
	private JButton southButton;
	private JButton eastButton;
	private JButton westButton;
	private GUI gui;
	
	public HeadingSelectDialog(Frame parent, String title, GUI g)
	{
		super(parent, title, true);
		gui = g;
		setLayout(new GridLayout(2,1,1,1));
		setSize(300, 200);
		message = new JLabel("Please Select An Initial Heading", SwingConstants.CENTER);
		northButton = new JButton("North");
		southButton = new JButton("South");
		eastButton = new JButton("East");
		westButton = new JButton("West");
		JButton blankButton1 = new JButton("");
		JButton blankButton2 = new JButton("");
		add(message);
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(2, 3, 1, 1));
		buttons.add(blankButton1);
		buttons.add(northButton);
		buttons.add(blankButton2);
		buttons.add(westButton);
		buttons.add(southButton);
		buttons.add(eastButton);
		add(buttons);
		northButton.addActionListener(this);
		southButton.addActionListener(this);
		eastButton.addActionListener(this);
		westButton.addActionListener(this);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		String commandString = ae.getActionCommand();
		System.out.println(commandString);
		if(commandString.equals("North"))
		{
			gui.setInitialHeading(240);
			
			dispose();
		}
		else if(commandString.equals("South"))
		{
			gui.setInitialHeading(60);
			dispose();
		}
		else if(commandString.equals("East"))
		{
			gui.setInitialHeading(150);
			dispose();
		}
		else
		{
			gui.setInitialHeading(-30);
			dispose();
		}
	}

}
