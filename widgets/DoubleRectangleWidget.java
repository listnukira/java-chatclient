package widgets;

import java.awt.Color;
import java.awt.Graphics;

public class DoubleRectangleWidget extends Widget {

	private static final long serialVersionUID = 1L;
	private int innerWidth;
	private int innerHeight;
	private int outerWidth;
	private int outerHeight;
	private Color innerBackground;
	private Color outerBackground;
	
	public DoubleRectangleWidget() {
		innerBackground = Color.GREEN;
		outerBackground = Color.RED;
		innerWidth = 50;
		innerHeight = 40;
		outerWidth = 80;
		outerHeight = 70;
		setSize(outerWidth, outerHeight);
	}

	public void paint(Graphics g) {
		super.paint(g);
		setSize(outerWidth, outerHeight);
		setBackground(outerBackground);
		g.setColor(innerBackground);
		g.fillRect((outerWidth - innerWidth) / 2, (outerHeight - innerHeight) / 2, innerWidth, innerHeight);
	}
	
	@Override
	public void destroy() {
	}

	@Override
	public void parseCommand(String cmd) {
		String[] tokens = cmd.split("( )+", 6);
		if ( tokens.length != 6 ) return;
		
		Color inColor, outColor;
		try {
			inColor = Color.decode(tokens[0]);
			outColor = Color.decode(tokens[1]);
			innerWidth = Integer.parseInt(tokens[2]);
			innerHeight = Integer.parseInt(tokens[3]);
			outerWidth = Integer.parseInt(tokens[4]);
			outerHeight = Integer.parseInt(tokens[5]);
		} catch (Exception e) {
			return;
		}
		
		innerBackground = inColor;
		outerBackground = outColor;
		repaint();
	}

	@Override
	public String toCommand() {
		return String.format("%s %s %s %s %s %s", getHexColor(innerBackground), 
				getHexColor(outerBackground), innerWidth, innerHeight, outerWidth, outerHeight);
	}
	
	private String getHexColor(Color cColor) {
		return String.format("#%02x%02x%02x", cColor.getRed(), 
				cColor.getGreen(), cColor.getBlue());
	}
	
	public int getInnerWidth() {
		return innerWidth;
	}

	public void setInnerWidth(int innerWidth) {
		this.innerWidth = innerWidth;
	}

	public int getInnerHeight() {
		return innerHeight;
	}

	public void setInnerHeight(int innerHeight) {
		this.innerHeight = innerHeight;
	}

	public int getOuterWidth() {
		return outerWidth;
	}

	public void setOuterWidth(int outerWidth) {
		this.outerWidth = outerWidth;
	}

	public int getOuterHeight() {
		return outerHeight;
	}

	public void setOuterHeight(int outerHeight) {
		this.outerHeight = outerHeight;
	}

	public Color getInnerBackground() {
		return innerBackground;
	}

	public void setInnerBackground(Color innerBackground) {
		this.innerBackground = innerBackground;
	}

	public Color getOuterBackground() {
		return outerBackground;
	}

	public void setOuterBackground(Color outerBackground) {
		this.outerBackground = outerBackground;
	}
}
