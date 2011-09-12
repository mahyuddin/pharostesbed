package pharoslabut.demo.autoIntersection;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class IntersectionDetectorDisplay extends JLabel {
	public static final long DISPLAY_DURATION = 2000;
	
	private static final long serialVersionUID = -2931497139130826529L;

	private int tracking = 0;

	private int left_x = 1, left_y = 1, right_x = 1, right_y = 1;

	private Color left_color = Color.WHITE, right_color = Color.WHITE;

	Timer timer;
	
	public IntersectionDetectorDisplay() {
		super("No Intersection");
		setForeground(Color.black);
		setFont(getFont().deriveFont(140f));

		JFrame frame = new JFrame("Intersection Detector");
		frame.getContentPane().add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null); // center frame
		frame.setVisible(true);
	}
	
	public void clearText() {
		setText("            ");
	}
	
	public void updateText(String msg) {
//		if (timer != null) {
//			timer.cancel();
//			timer = null;
//		}
		setText(msg);
//		timer = new Timer();
//		timer.schedule(new TimerTask() {
//			public void run() {
//				clearText();
//				timer = null;
//			}
//		}, DISPLAY_DURATION);
	}

	public Dimension getPreferredSize() {
		String text = getText();
		FontMetrics fm = this.getFontMetrics(getFont());

		int w = fm.stringWidth(text);
		w += (text.length() - 1) * tracking;
		w += left_x + right_x;

		int h = fm.getHeight();
		h += left_y + right_y;

		return new Dimension(w, h);
	}

	public void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		char[] chars = getText().toCharArray();

		FontMetrics fm = this.getFontMetrics(getFont());
		int h = fm.getAscent();
		LineMetrics lm = fm.getLineMetrics(getText(), g);
		g.setFont(getFont());

		int x = 0;

		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			int w = fm.charWidth(ch) + tracking;

			g.setColor(left_color);
			g.drawString("" + chars[i], x - left_x, h - left_y);

			g.setColor(right_color);
			g.drawString("" + chars[i], x + right_x, h + right_y);

			g.setColor(getForeground());
			g.drawString("" + chars[i], x, h);

			x += w;
		}

	}
}
