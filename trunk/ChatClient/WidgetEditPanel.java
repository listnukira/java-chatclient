package ChatClient;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.lang.reflect.Method;

import javax.swing.*;

import editor.*;
import widgets.*;

public class WidgetEditPanel {
	private JFrame editGUI;
	private JPanel panel;
	private JScrollPane scroll;
	private Object object;
	
	public WidgetEditPanel(Object o) {
		editGUI = new JFrame("Widget editor panel");
		editGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		editGUI.setPreferredSize(new Dimension(450, 500));
		
		panel = new JPanel(new GridLayout(0, 2), true);
		scroll = new JScrollPane(panel);
		
		this.object = o;
		buildUI();
	}
	
	public void buildUI() {
		panel.add(new JLabel("Property"));
		panel.add(new JLabel("Input"));
		
		BeanInfo info = null;
		try {
			info = Introspector.getBeanInfo(object.getClass());
		} catch (java.beans.IntrospectionException e) {
			e.printStackTrace();
		}
		
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
			String field_name = pd.getName();
			Method get_method = pd.getReadMethod();
			Method set_method = pd.getWriteMethod();
			
			if (get_method != null && set_method != null) {
				String type = pd.getPropertyType().getSimpleName();
				
				if (pd.getPropertyType().isPrimitive() || type.equals("String")) {
					panel.add(new JLabel(field_name));
					try {
						Object value = get_method.invoke(object);
						if (value != null) {
							panel.add(new JTextField(value.toString()));
						} else {
							panel.add(new JTextField(""));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						Class<?> c = Class.forName("editor." + type + "Editor");
						JButton btn = new JButton(type);
						btn.setName("editor." + type + "Editor");
						btn.addActionListener(btnListener);
						panel.add(new JLabel(field_name));
						panel.add(btn);
					} catch (ClassNotFoundException e) {
					}
				}
			}
		}
		
		editGUI.getContentPane().add(scroll);
		editGUI.pack();
		editGUI.setVisible(true);
	}

	ActionListener btnListener = new ActionListener () {
		public void actionPerformed(ActionEvent action) {
			JButton btn = (JButton) action.getSource();
			//System.out.println(btn.get);

			new editThread(btn).start();
			/*
			Class<?> c = null;
			Object o = null;
			try {
				c = Class.forName(btn.getName());
				o = c.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			BaseEditor editor = (BaseEditor) o;
			editor.returnValue(Color.BLACK);
			
			try {
				Method disposeMethod = c.getMethod("dispose");
				disposeMethod.invoke(c);
			} catch (Exception e) {}
			*/
		}
	};
	
	public class editThread extends java.lang.Thread {
		private JButton btn;
		
		public editThread(JButton btn) {
			this.btn = btn;
		}
		
		public void run() {
			Class<?> c = null;
			Object o = null;
			try {
				c = Class.forName(btn.getName());
				o = c.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			BaseEditor editor = (BaseEditor) o;
			editor.returnValue(Color.BLACK);
			
			try {
				Method disposeMethod = c.getMethod("dispose");
				disposeMethod.invoke(c);
			} catch (Exception e) {}
		}
	}
}
