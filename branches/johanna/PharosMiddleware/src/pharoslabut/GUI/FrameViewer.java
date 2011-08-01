package pharoslabut.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import  java.awt.event.ActionEvent;
import  java.awt.event.ActionListener;
import javax.swing.JButton;
import  javax.swing.JFrame;
import javax.swing.JScrollPane;
import  javax.swing.Timer;
import javax.swing.border.LineBorder;

public class FrameViewer
{
	public static void main(String[] args)
	{
		final JFrame frame = new JFrame();
                final int FRAME_WIDTH = 800;
                final int FRAME_HEIGHT = 600;
                final JButton button = new JButton("Draw");
                final JScrollPane jScrollPane = new JScrollPane();
              

                frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                jScrollPane.setViewportBorder(new LineBorder(Color.BLACK));
                
               
               


		class TimerListener implements ActionListener
		{
            @Override
			public void actionPerformed(ActionEvent event)
			{		
                                Mapping component2 = new Mapping();
				
                                jScrollPane.getViewport().add(component2, null);
                                frame.add(jScrollPane, BorderLayout.CENTER);
				frame.setVisible(true);
                               
                                

				
			}
		}

		ActionListener listener = new TimerListener();
		final int DELAY = 600; // Tiempo en milisegundos
		Timer t = new Timer(DELAY, listener);
		t.start();
	}
}
