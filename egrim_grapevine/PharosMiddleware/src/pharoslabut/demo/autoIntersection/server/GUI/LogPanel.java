package pharoslabut.demo.autoIntersection.server.GUI;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * @author Maykel Hanna
 *
 */
public class LogPanel extends JPanel {
	
	private static final long serialVersionUID = 3827993482624351568L;
	private static JTextArea textArea = new JTextArea(9, 77);
	private JScrollPane scrollPane;

	public LogPanel() {
	    setBackground(Color.BLACK);
	    
	    textArea.setEditable(false);		
	    scrollPane = new JScrollPane(textArea);
	    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    
		add(scrollPane);
	}
	
	public Dimension getPreferredSize() {
	    return new Dimension(IntersectionGUI.frameWidth-100,140);
	}
	
	public static void appendLog(String s) {
		textArea.append(s+"\n");
		textArea.setCaretPosition(textArea.getText().length());
	}
}
