package pharoslabut.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
//import java.awt.font.LineMetrics;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * A simple GUI that display text.
 * 
 * @author Chien-Liang Fok
 */
public class SimpleTextGUI extends JLabel {

	private static final long serialVersionUID = -9059770975010136887L;

	private int tracking = 0;

	private int leftX = 1, leftY = 1, rightX = 1, rightY = 1;
	
	private Color leftColor = Color.WHITE, rightColor = Color.WHITE;
	
	public SimpleTextGUI() {
		this("          ");
	}
	
	public SimpleTextGUI(String frameName) {
		super(frameName);
		setForeground(Color.black);
		setFont(getFont().deriveFont(140f));

		JFrame frame = new JFrame(frameName);
		frame.getContentPane().add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null); // center frame
		frame.setVisible(true);
	}
	
	public Dimension getPreferredSize() {
		String text = getText();
		FontMetrics fm = this.getFontMetrics(getFont());

		int w = fm.stringWidth(text);
		w += (text.length() - 1) * tracking;
		w += leftX + rightX;

		int h = fm.getHeight();
		h += leftY + rightY;

		return new Dimension(w, h);
	}
	
	public void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		char[] chars = getText().toCharArray();

		FontMetrics fm = this.getFontMetrics(getFont());
		int h = fm.getAscent();
//		LineMetrics lm = fm.getLineMetrics(getText(), g);
		g.setFont(getFont());

		int x = 0;

		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			int w = fm.charWidth(ch) + tracking;

			g.setColor(leftColor);
			g.drawString("" + chars[i], x - leftX, h - leftY);

			g.setColor(rightColor);
			g.drawString("" + chars[i], x + rightX, h + rightY);

			g.setColor(getForeground());
			g.drawString("" + chars[i], x, h);

			x += w;
		}

	}
}
