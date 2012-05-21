package ChatClient;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.*;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.lang.model.type.PrimitiveType;
import javax.swing.*;

import editor.*;
import widgets.*;

public class WidgetEditPanel {
	private ChatClientGUI gui;
	private JFrame editGUI;
	private JPanel panel;
	private JScrollPane scroll;
	private Object object;
	private int objID;
	ArrayList<PropertyDescriptor>  propertyDescriptor;
	
	public WidgetEditPanel(ChatClientGUI gui, Object o, int objID) {
		this.gui = gui;
		this.objID = objID;
		editGUI = new JFrame("Widget editor panel");
		editGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		panel = new JPanel(new GridLayout(0, 2), true);
		scroll = new JScrollPane(panel);
		scroll.setPreferredSize(new Dimension(450, 500));
		
		this.object = o;
		propertyDescriptor = new ArrayList<PropertyDescriptor>();
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

		/* for each propertydescriptor, analysis it */
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
			String field_name = pd.getName();
			Method get_method = pd.getReadMethod();
			Method set_method = pd.getWriteMethod();
			String value = "";
			
			/* get member value which has both read and write method */
			if (get_method != null && set_method != null) {
				String field_type = pd.getPropertyType().getSimpleName();
				/* if member's type is primitive or string, using textfield */
				if (pd.getPropertyType().isPrimitive() || field_type.equals("String")) {
					try {
						Object o = get_method.invoke(object);
						if (o != null) {
							value = o.toString();
						}
						
						JTextField text = new JTextField(value);
						text.addActionListener(textListener);
						text.addFocusListener(textFocusListener);
						
						text.setName(String.valueOf(propertyDescriptor.size()));
						propertyDescriptor.add(pd);
						panel.add(new JLabel(field_name));
						panel.add(text);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						Class.forName("editor." + field_type + "Editor");
						
						JButton btn = new JButton(field_type);
						btn.addActionListener(btnListener);
						
						btn.setName(String.valueOf(propertyDescriptor.size()));
						propertyDescriptor.add(pd);
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

	FocusAdapter textFocusListener = new FocusAdapter() {
		public void focusLost(FocusEvent f) {
			JTextField text = (JTextField) f.getSource();
			PropertyDescriptor pd = propertyDescriptor.get(Integer.valueOf(text.getName()));
			detectModify(pd, text);
		}
	};
	
	ActionListener textListener = new ActionListener() {
		public void actionPerformed(ActionEvent action) {
			JTextField text = (JTextField) action.getSource();
			PropertyDescriptor pd = propertyDescriptor.get(Integer.valueOf(text.getName()));
			detectModify(pd, text);
		}
	};
	
	ActionListener btnListener = new ActionListener() {
		public void actionPerformed(ActionEvent action) {
			JButton btn = (JButton) action.getSource();
			new editThread(btn).start();
		}
	};
	
	public void detectModify(PropertyDescriptor pd, JTextField text) {
		Method get_method = pd.getReadMethod();
		Method set_method = pd.getWriteMethod();
		String oldValue = "", newValue = text.getText();

		try {
			Object o = get_method.invoke(object);
			if (o != null) {
				oldValue = o.toString();
			}
			/* value has been modified */
			if (oldValue.equals(newValue) == false) {
				o = Convert.convert(newValue, pd.getPropertyType());
				set_method.invoke(object, o);
				
				gui.chatClient.sout.println(String.format("/change %d %s", objID, 
						((Widget) object).toCommand()));
				gui.whiteboard.revalidate();
				gui.whiteboard.repaint();
			}
		} catch (Exception e) {
			text.setText(oldValue);
		}
	}
	
	public class editThread extends java.lang.Thread {
		private JButton btn;
		
		public editThread(JButton btn) {
			this.btn = btn;
		}
		
		public void run() {
			PropertyDescriptor pd = propertyDescriptor.get(Integer.valueOf(btn.getName()));
			String type = pd.getPropertyType().getSimpleName();
			Class<?> c = null;
			Object o = null;
			Object reto = null;
			
			try {
				c = Class.forName("editor." + type + "Editor");
				o = c.newInstance();
				BaseEditor editor = (BaseEditor) o;
				reto = editor.returnValue(pd.getReadMethod().invoke(object));
				pd.getWriteMethod().invoke(object, reto);

				gui.chatClient.sout.println(String.format("/change %d %s", objID, 
						((Widget) object).toCommand()));
				gui.whiteboard.revalidate();
				gui.whiteboard.repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				Method disposeMethod = c.getMethod("dispose");
				disposeMethod.invoke(c);
			} catch (Exception e) {}
		}
	}
}
