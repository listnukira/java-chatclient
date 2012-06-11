package ChatClient;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
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
	private JButton okBtn;
	
	private Object object;
	private boolean isOKClicked;
	private int objID;
	private int x;
	private int y;
	ArrayList<PropertyDescriptor>  propertyDescriptor = new ArrayList<PropertyDescriptor>();
	ArrayList<Object> oldValue = new ArrayList<Object>();
	
	/* constructor for exist widget */
	public WidgetEditPanel(ChatClientGUI gui, Object o, int objID) {
		this.gui = gui;
		this.objID = objID;
		this.object = o;
		this.x = -1;
		this.y = -1;
		buildUI();
	}
	
	/* constructor for non-exist widget */
	public WidgetEditPanel(ChatClientGUI gui, Object o, int x, int y) {
		this.gui = gui;
		this.objID = -1;
		this.object = o;
		this.x = x;
		this.y = y;
		buildUI();
	}
	
	public void buildUI() {
		editGUI = new JFrame("Widget editor panel");
		editGUI.addWindowListener(windowListener); // if close, reset oldvalue
		editGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		panel = new JPanel(new GridLayout(0, 2), true);
		scroll = new JScrollPane(panel);
		scroll.setPreferredSize(new Dimension(450, 500));
		
		panel.add(new JLabel("Property"));
		panel.add(new JLabel("Input"));
		
		/* get bean info */
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
				try {
					Object o = get_method.invoke(object);
					
					/* if member's type is primitive or string, using textfield */
					if (pd.getPropertyType().isPrimitive() || field_type.equals("String")) {
						if (o != null) {
							value = o.toString();
						}
						
						JTextField text = new JTextField(value);
						text.addActionListener(textListener);
						text.addFocusListener(textFocusListener);
						text.setName(String.valueOf(propertyDescriptor.size()));
						
						panel.add(new JLabel(field_name));
						panel.add(text);
					} else {
						Class.forName("editor." + field_type + "Editor");
						
						JButton btn = new JButton(field_type);
						btn.addActionListener(btnListener);
						btn.setName(String.valueOf(propertyDescriptor.size()));

						panel.add(new JLabel(field_name));
						panel.add(btn);
					}
					propertyDescriptor.add(pd);
					oldValue.add(o);
				} catch (ClassNotFoundException e) {
					// do nothing
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		okBtn = new JButton("OK");
		okBtn.addActionListener(okbtnListener);
		
		editGUI.getContentPane().add(BorderLayout.SOUTH, okBtn);
		editGUI.getContentPane().add(BorderLayout.CENTER, scroll);
		editGUI.pack();
		editGUI.setVisible(true);
	}

	FocusAdapter textFocusListener = new FocusAdapter() {
		public void focusLost(FocusEvent f) {
			JTextField text = (JTextField) f.getSource();
			detectModify(Integer.valueOf(text.getName()), text);
		}
	};
	
	ActionListener textListener = new ActionListener() {
		public void actionPerformed(ActionEvent action) {
			JTextField text = (JTextField) action.getSource();
			detectModify(Integer.valueOf(text.getName()), text);
		}
	};
	
	ActionListener btnListener = new ActionListener() {
		public void actionPerformed(ActionEvent action) {
			JButton btn = (JButton) action.getSource();
			new editThread(btn).start();
		}
	};
	
	ActionListener okbtnListener = new ActionListener() {
		public void actionPerformed(ActionEvent action) {
			/* objID = -1 when create widget */
			if (objID == -1) {
				String widgeType = object.getClass().getSimpleName();
				gui.chatClient.sout.println(String.format("/post %s %d %d %s", widgeType, 
						x, y, ((Widget) object).toCommand()));
			} else {
				gui.chatClient.sout.println(String.format("/change %d %s", objID, 
				((Widget) object).toCommand()));
			}
			
			isOKClicked = true;
			editGUI.setVisible(false);
			editGUI.dispose();
		}
	};
	
	/* if close window, reset original value */
	WindowAdapter windowListener = new WindowAdapter() {
		public void windowClosed(WindowEvent e) {
			if (objID == -1 || isOKClicked) return;
			
			try {
				for (int i = 0; i < oldValue.size(); ++i) {
					Method set_method = propertyDescriptor.get(i).getWriteMethod();
					set_method.invoke(object, oldValue.get(i));
				}
			} catch (Exception exception) {
				exception.getStackTrace();
			}

			gui.whiteboard.revalidate();
			gui.whiteboard.repaint();
		}
	};
	
	public void detectModify(int idx, JTextField text) {
		PropertyDescriptor pd = propertyDescriptor.get(idx);
		Method set_method = pd.getWriteMethod();
		String textValue = text.getText();

		try {
			Object o = Convert.convert(textValue, pd.getPropertyType());
			if (o.equals(oldValue.get(idx)) == false) {
				set_method.invoke(object, o);

				gui.whiteboard.revalidate();
				gui.whiteboard.repaint();
			}
		} catch (Exception e) {
			text.setText(textValue);
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
