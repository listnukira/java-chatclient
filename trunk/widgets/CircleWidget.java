package widgets;

import java.awt.Color;
import java.awt.Graphics;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CircleWidget extends Widget
{
	private static final long serialVersionUID = 1L;
	private PropertyChangeSupport propertyChangeSupport;
    private Integer wbRadius;
    private Color wbFore;
    private Color wbBack;
    
    public CircleWidget()
    {
        propertyChangeSupport = new PropertyChangeSupport(this);
        wbFore = Color.GREEN ;
        wbBack = Color.BLUE ;
        wbRadius = Integer.valueOf(80);
        setSize(wbRadius.intValue(), wbRadius.intValue());
    }

    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        g.setColor(wbFore);
        setBackground(wbBack);
        g.fillOval(0, 0, wbRadius.intValue() - 1, wbRadius.intValue() - 1);
        setSize(wbRadius.intValue(), wbRadius.intValue());
    }

    public int getWidth()
    {
        return wbRadius.intValue();
    }

    public int getHeight()
    {
        return wbRadius.intValue();
    }

    public Integer getwbRadius()
    {
        return wbRadius;
    }

    public void setwbRadius(Integer wbRadius)
    {
        Integer oldRadius = this.wbRadius;
        this.wbRadius = wbRadius;
        propertyChangeSupport.firePropertyChange("wbRadius", oldRadius, wbRadius);
        repaint();
    }

    public String toCommand()
    {
    	return String.format("%s %s %d", 
    			getHexColor(wbBack), getHexColor(wbFore), wbRadius) ;
    }

    public void parseCommand(String command)
    {
    	String[] tokens = command.split("( )+", 3) ;
		if ( tokens.length != 3 ) return ;
		Color bkColor, frColor = null ;
		int r ;
		try {
			bkColor = Color.decode(tokens[0]);
			frColor = Color.decode(tokens[1]);
			r = Integer.parseInt(tokens[2]) ;
		} catch ( Exception e ) {
			return ;
		}
		
		setwbBack(bkColor);
		setwbFore(frColor);
		setwbRadius(r) ;
    }
    public void destroy()
	{	
	}
    
    public Color getwbFore()
    {
        return wbFore;
    }

    public void setwbFore(Color wbFore)
    {
        Color oldFore = this.wbFore;
        this.wbFore = wbFore;
        propertyChangeSupport.firePropertyChange("wbFore", oldFore, wbFore);
        repaint();
    }

    public Color getwbBack()
    {
        return wbBack;
    }

    public void setwbBack(Color wbBack)
    {
        Color oldBack = this.wbBack;
        this.wbBack = wbBack;
        propertyChangeSupport.firePropertyChange("wbBack", oldBack, wbBack);
        repaint();
    }
    private String getHexColor(Color cColor)
	{                        
        return String.format("#%02x%02x%02x", 
        		cColor.getRed(), cColor.getGreen(), cColor.getBlue() ) ;
	}
}
