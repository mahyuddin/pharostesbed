package pharoslabut.cps;

import java.util.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Provides a text area with line numbers and break points.
 * 
 * @author Chien-Liang Fok
 * @see http://www.velocityreviews.com/forums/t131969-display-line-number-in-jtextpane.html
 */
public class ProgramTextArea extends JPanel {
	
	private static final long serialVersionUID = 767948773563227247L;
	
	private JTextPane textPane;
	private JScrollPane scrollPane;
	
	private JPanel lineNoPanel, breakpointPanel;
	
	/**
	 * Contains the lines that have breakpoints.
	 */
	private Vector<Integer> breakpoints = new Vector<Integer>();
	
	/**
	 * Keeps track of which line we are currently executing.
	 */
	private int executionLine = -1;
	
	/**
	 * The constructor.
	 */
	public ProgramTextArea() {
		super();
		
		lineNoPanel = new LineNoPanel();
		breakpointPanel = new BreakpointPanel();
		
		// we need to override paint so that the line numbers stay in sync
		textPane = new JTextPane() {
			
			private static final long serialVersionUID = 1381991578711455835L;

			public void paint(Graphics g) {
				super.paint(g);
				lineNoPanel.repaint();
				breakpointPanel.repaint();
			}
		};
		scrollPane = new JScrollPane(textPane);
		
		// Create a panel for holding the breakpoint and execution panels
//		JPanel sidePanel = new JPanel();
//		sidePanel.setLayout(new GridLayout(1 /*row*/, 2 /*columns*/));
//		sidePanel.add(executionPanel);
//		sidePanel.add();
		
		
		// Create a gutter panel to keep the line number and breakpoint panels
		JPanel gutterPanel = new JPanel();
		gutterPanel.setLayout(new BorderLayout());
		gutterPanel.add(breakpointPanel, BorderLayout.WEST);
		gutterPanel.add(lineNoPanel, BorderLayout.CENTER);
		
		// Add the gutter panel and scroll pane to this panel
		setLayout(new BorderLayout());
		add(gutterPanel, BorderLayout.WEST);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Returns the text typed into the text pane.  This is the program that the user entered.
	 * 
	 * @return The text in the text pane.
	 */
	public String getText() {
		return textPane.getText();
	}
	
	/**
	 * Sets the execution line of the program.
	 * 
	 * @param line The line that is currently being executed.
	 */
	public void setExecutionLine(int line) {
		executionLine = line;
		lineNoPanel.repaint();
	}
	
	/**
	 * Clear all break points.
	 */
	public void clearBreakpoints() {
		breakpoints.clear();
		breakpointPanel.repaint();
	}
	
	/**
	 * Returns the list of breakpoints that have been set.
	 * 
	 * @return The breakpoints that have been set.
	 */
	public Vector<Integer> getBreakpoints() {
		return breakpoints;
	}
	
	/**
	 * Determines whether a breakpoint exists on a specific line.
	 * 
	 * @param line
	 * @return
	 */
	public boolean containsBreakpoint(int line) {
		return breakpoints.contains(line);
	}
	
	/**
	 * Toggles the presence of a breakpoint.
	 * 
	 * @param line The line on which the breakpoint should be set.
	 */
	public void toggleBreakpoint(int line) {
		if (breakpoints.contains(line))
			breakpoints.remove(new Integer(line));
		else
			breakpoints.add(line);
		breakpointPanel.repaint();
	}
	
	/**
	 * A panel that allows users to enable and disable breakpoints.
	 */
	private class BreakpointPanel extends JPanel {
		
		private static final long serialVersionUID = 2002032854576020494L;

		
		public BreakpointPanel() {
			super();
			
			// The following controls the width of the breakpoint column
			setPreferredSize(new Dimension(15, 30)); 
			setMinimumSize(new Dimension(15, 30));
			
			// Change the background color so this panel can be distinguished from
			// the line number panel.
			setBackground(Color.LIGHT_GRAY);
			
			// Add a mouse listener for toggling break points.
			addMouseListener(new BreakpointMouseListener());
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			ViewSpecs viewSpecs = new ViewSpecs(g);

			for (int line = viewSpecs.startline, y = viewSpecs.startingY; line <= viewSpecs.endline; 
				y += viewSpecs.fontHeight, line++) 
			{
				if (containsBreakpoint(line))
					g.drawString("*", 0, y);
			}

		}
	}
	
	/**
	 * Listens for mouse click events on the breakpoint panel.
	 * Toggles the existence of breakpoints based on the location
	 * of the mouse click.
	 *
	 */
	private class BreakpointMouseListener extends MouseAdapter {
		
		public BreakpointMouseListener() {
			super();
		}
		
		public void mouseClicked(MouseEvent e) {
			Point p = e.getPoint();
			
			// This is the absolute coordinate within the text area that matches the
			// location that was clicked in the BreakpointPanel.
			// We can correlate the locations within the BreakpointPanel and the scrollPane
			// because the BreakpointPanel is narrower than the scrollPane but has the same
			// height and is located next to it.
			int absoluteCoord = textPane.viewToModel(p);

			// Translate view coordinates into lines that are viewable
			Document doc = textPane.getDocument();
			int line = doc.getDefaultRootElement().getElementIndex(absoluteCoord) + 1;
			
			//log("Document end offset: " + doc.getDefaultRootElement().getEndOffset());
			//log("User clicked on " + p + ", which maps to line " + line);
			
			toggleBreakpoint(line);
			
		}
	}
	
	/**
	 * This is a panel that contains the line numbers.  It is positioned
	 * next to the scroll area.
	 */
	private class LineNoPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8186569779644293280L;

		public LineNoPanel() {
			super();
			
			// The following controls the width of the line number column
			setPreferredSize(new Dimension(30, 30)); 
			setMinimumSize(new Dimension(30, 30));
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			ViewSpecs viewSpecs = new ViewSpecs(g);

			for (int line = viewSpecs.startline, y = viewSpecs.startingY; line <= viewSpecs.endline; 
				y += viewSpecs.fontHeight, line++) 
			{
				
				if (line == executionLine) {	
					Font origFont = g.getFont();
					g.setFont(origFont.deriveFont(Font.BOLD));
					g.drawString(Integer.toString(line), 0, y);
					g.setFont(origFont);
				} else {
					g.drawString(Integer.toString(line), 0, y);
				}
			}

		}
	}
	
	private class ViewSpecs {
		int start, end;
		int startline, endline;
		int fontHeight, fontDesc;
		int startingY = -1;
		
		public ViewSpecs(Graphics g) {
			// Get the start and end absolute coordinates of the current program's view
			start =
				textPane.viewToModel(scrollPane.getViewport().getViewPosition());
			end =
				textPane.viewToModel(
						new Point(
								scrollPane.getViewport().getViewPosition().x + textPane.getWidth(),
								scrollPane.getViewport().getViewPosition().y + textPane.getHeight()));

			// Translate view coordinates into lines that are viewable
			Document doc = textPane.getDocument();
			startline = doc.getDefaultRootElement().getElementIndex(start) + 1;
			endline = doc.getDefaultRootElement().getElementIndex(end) + 1;

			// Get the line height specification
			fontHeight = g.getFontMetrics(textPane.getFont()).getHeight();
			fontDesc = g.getFontMetrics(textPane.getFont()).getDescent();
			
			try {
				startingY = textPane.modelToView(start).y - scrollPane.getViewport().getViewPosition().y 
					+ fontHeight - fontDesc;
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}
	}
	
//	private void log(String msg) {
//		System.out.println("ProgramTextArea: " + msg);
//	}
}
