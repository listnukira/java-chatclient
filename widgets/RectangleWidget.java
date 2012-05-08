package widgets;

import java.awt.Color;

public class RectangleWidget extends Widget {

	private static final long serialVersionUID = 1L;
	int wbWidth ;
	int wbHeight ;
	Color wbBackColor ;
	
	public RectangleWidget()
	{
		wbWidth=80;
		wbHeight=60 ;
		wbBackColor = new Color(139,0,255) ;
		setVisible(true) ;
	}
	public void parseCommand(String cmd)
	{
		String[] tokens = cmd.split("( )+", 3) ;
		if ( tokens.length != 3 ) return ;
		Color bkColor = null ;
		int w, h ;
		try {
			bkColor = Color.decode(tokens[0]);
			h = Integer.parseInt(tokens[1]) ;
			w = Integer.parseInt(tokens[2]) ;
		} catch ( Exception e ) {
			return ;
		}
		
		wbBackColor = bkColor ;
		wbHeight = h;
		wbWidth = w ;
		
		setSize(wbWidth,wbHeight) ;
		this.setBackground(wbBackColor) ;
	}
	public String toCommand()
	{
		return String.format("%s %d %d", 
				getHexColor(wbBackColor), wbHeight, wbWidth) ;
	}

	public void destroy()
	{	
	}
	
	public int getwbWidth() {
		return wbWidth;
	}
	public void setwbWidth(int wbWidth) {
		this.wbWidth = wbWidth;
	}
	public int getwbHeight() {
		return wbHeight;
	}
	public void setwbHeight(int wbHeight) {
		this.wbHeight = wbHeight;
	}

	public Color getwbBackColor() {
		return wbBackColor;
	}
	public void setwbBackColor(Color wbBackColor) {
		this.wbBackColor = wbBackColor;
	}
	private String getHexColor(Color cColor)
	{
        return String.format("#%02x%02x%02x", 
        		cColor.getRed(), cColor.getGreen(), cColor.getBlue() ) ;
	}
}
