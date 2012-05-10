package widgets;

import javax.swing.*;

public abstract class Widget extends JPanel {
	private static final long serialVersionUID = 1L;
	abstract public void parseCommand(String cmd) ;
	abstract public String toCommand() ;
	abstract public void destroy () ; 
}
