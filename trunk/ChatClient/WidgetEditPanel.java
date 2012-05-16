package ChatClient;

import java.awt.*;
import javax.swing.*;

public class WidgetEditPanel {
	private static final long serialVersionUID = 1L;
	private JFrame editGUI;
	
	public WidgetEditPanel(Object o) {
		editGUI = new JFrame("Widget editor panel");
		editGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buildUI();
	}
	
	public void buildUI() {
		editGUI.getContentPane().setLayout(new GridLayout(0, 2));
		
		editGUI.pack();
		editGUI.setVisible(true);
	}

}
