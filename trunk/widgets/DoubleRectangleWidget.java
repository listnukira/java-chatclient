package widgets;

import java.awt.Color;
import java.awt.Graphics;

public class DoubleRectangleWidget extends Widget {

	private static final long serialVersionUID = 1L;
	private Integer innerWidth;
	private Integer innerHeight;
	private Integer outerWidth;
	private Integer outerHeight;
	private Color innerBackground;
	private Color outerBackground;
	
	public DoubleRectangleWidget() {
		innerBackground = Color.GREEN;
		outerBackground = Color.RED;
		innerWidth = Integer.valueOf(50);
		innerHeight = Integer.valueOf(40);
		outerWidth = Integer.valueOf(80);
		outerHeight = Integer.valueOf(70);
		setSize(outerWidth, outerHeight);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
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
			innerWidth = Integer.parseInt(tokens[1]);
			innerHeight = Integer.parseInt(tokens[2]);
			outColor = Color.decode(tokens[3]);
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
				innerWidth, innerHeight, getHexColor(outerBackground), outerWidth, outerHeight);
	}
	
	private String getHexColor(Color cColor) {
		return String.format("#%02x%02x%02x", cColor.getRed(), 
				cColor.getGreen(), cColor.getBlue());
	}
}
