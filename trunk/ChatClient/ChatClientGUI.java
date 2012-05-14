package ChatClient;

import java.io.*;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import widgets.*;

public class ChatClientGUI implements ActionListener {
	
	private JFrame frame;
	private int shift_x;
	private int shift_y;
	
	/* GUI */
	ChatClient chatClient;
	JPanel whiteboard;
	JScrollPane boardScroll;
	JPanel btnPanel;
	JScrollPane btnScroll;
	JTextArea chatArea;
	JScrollPane chatScroll;
	JLabel clabel;
	JTextField cTextField;
	JButton b1;
	JButton b2;
	JButton b3;
	
	String type = "";
	int moveObjId = -1;
	
	ArrayList<String> widgetList = new ArrayList<String>();
	
	/* Constructor */
	public ChatClientGUI() {
		frame = new JFrame("ChatClient");
		widgetList.add("Rectangle");
		widgetList.add("Juggler");
		widgetList.add("Circle");
		buidUI();
		cTextField.addActionListener(this);
		whiteboard.addMouseListener(whiteboardListener);
	}

	/* Receive input cmd */
	public void actionPerformed(ActionEvent action) {
		String input = cTextField.getText().trim();
		cTextField.setText("");
		cTextField.requestFocusInWindow();

		if (input.startsWith("/connect")) {
			chatClient.logout.println(input);
			if (chatClient.isConnect == true) {
				chatClient.sout.println("/leave");
			}

			String[] splited = input.split(" ", 3);
			chatClient.reset(splited[1], Integer.parseInt(splited[2]));
			return;
		}

		/* first input name, not connect */
		if (chatClient.nameIsReady == false && chatClient.isConnect == false) {
			chatClient.name = input;
			chatClient.nameIsReady = true;
			chatClient.connectToServer();
		}
		
		/* is connecting and name not checked */
		if (chatClient.nameChecked == false) {
				chatClient.sout.println(input);
				chatClient.name = input;
				chatArea.append(input);
				chatArea.append("\n");
		} else if (chatClient.isConnect == true) {
			chatClient.logout.println(input);
			if (input.equals("/showPost")) {
				chatClient.showPostLog();
			} else {
				chatClient.sout.println(input);
			}
		} else {
		
		}
	}

	public void buidUI() {
		//frame.setSize(800, 600);
		frame.setResizable(false);
		frame.getContentPane().setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
		GridBagConstraints c = new GridBagConstraints();

		/* Whiteboard */
		whiteboard = new JPanel(null, true);
		whiteboard.setPreferredSize(new Dimension(500, 300));
		whiteboard.setAutoscrolls(true);
		whiteboard.setBackground(Color.WHITE);
		boardScroll = new JScrollPane(whiteboard);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 5;
		c.gridheight = 4;
		c.fill = GridBagConstraints.BOTH;
		frame.getContentPane().add(boardScroll, c);
		// End whiteboard
		
		/* Widget button panel */
		btnPanel = new JPanel(new GridLayout(0, 1), true);
		btnPanel.setPreferredSize(new Dimension(100, 150));
		btnScroll = new JScrollPane(btnPanel);
		
		for (String s : widgetList) {
			JButton btn = new JButton(s);
			btnPanel.add(btn);
			btn.addActionListener(btnListener);
		}

		c.gridx = 5;
		c.gridwidth = 1;
		c.gridheight = 4;
		frame.getContentPane().add(btnScroll, c);
		// End four button

		/* Chat region */
		chatArea = new JTextArea("Username: ", 10, 15);
		chatArea.setEditable(false);
		chatArea.setAutoscrolls(true);
		chatArea.setBackground(Color.LIGHT_GRAY);
		chatScroll = new JScrollPane(chatArea);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 6;
		c.gridheight = 2;
		frame.getContentPane().add(chatScroll, c);
		// End chat region

		/* Input label */
		clabel = new JLabel("Input Username: ");
		c.gridy = 6;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.insets = new Insets(3, 3, 3, 3);
		frame.getContentPane().add(clabel, c);
		// End input label
		
		/* Command region */
		cTextField = new JTextField(15);
		c.gridx = 2;
		c.gridwidth = 4;
		c.insets = new Insets(0, 0, 0, 0);
		frame.getContentPane().add(cTextField, c);
		// End command region
		
		frame.pack();
		frame.setVisible(true);
		cTextField.requestFocusInWindow();
	}

	static void usage() {
		System.err.println("Usage: java ChatClient [host] [port]");
		System.exit(1);
	}
	
	public static void main(String args[]) {
		/* Parse argument */
		if (args.length != 2) {
			usage();
		}
		
		/* Start program */
		try {
			ChatClientGUI gui = new ChatClientGUI();
			gui.chatClient = new ChatClient(args[0], Integer.parseInt(args[1]), gui);
		} catch (IllegalArgumentException e) {
			usage();
		}
	}

	ActionListener btnListener = new ActionListener () {
		public void actionPerformed(ActionEvent e) {
			if (chatClient.isConnect == false)
				return;
			
			JButton b = (JButton) e.getSource();
			type = b.getText() + "Widget";
		}
	};
	
	MouseAdapter whiteboardListener = new MouseAdapter () {
		public void mouseClicked(MouseEvent e) {
			if (chatClient.isConnect == false || type.isEmpty())
				return;

			try {
				Class<?> c = Class.forName("widgets." + type);
				Object object = c.newInstance();
				chatClient.sout.println(String.format("/post %s %d %d %s", type, 
						e.getX(), e.getY(), ((Widget)object).toCommand()));
			} catch (ClassNotFoundException ee) {
				ee.printStackTrace();
			} catch (InstantiationException ee) {
				ee.printStackTrace();
			} catch (IllegalAccessException ee) {
				ee.printStackTrace();
			}

			type = "";
		}
	};
	
	
	MouseAdapter widgetListener = new MouseAdapter () {
		public void mousePressed(MouseEvent e) {
			for (Msg m : chatClient.postLog) {
				if (m.getMsgid() == Integer.parseInt(e.getComponent().getName()) && 
					chatClient.name.equals(m.getUsr())) {
					shift_x = e.getX();
					shift_y = e.getY();
					moveObjId = m.getMsgid();
				}
			}
		}
		
		public void mouseDragged(MouseEvent e) {
			if (moveObjId == -1)
				return;
			
			int x = e.getComponent().getX() + e.getX() - shift_x;
			int y = e.getComponent().getY() + e.getY() - shift_y;
			if (x < 0 ) x = 0;
			if (y < 0 ) y = 0;
			e.getComponent().setLocation(x, y);
			whiteboardResize();
		}
		
		public void mouseReleased(MouseEvent e) {
			if (moveObjId == -1)
				return;
			
			int x = e.getComponent().getX() + e.getX() - shift_x;
			int y = e.getComponent().getY() + e.getY() - shift_y;
			if (x < 0 ) x = 0;
			if (y < 0 ) y = 0;
			
			chatClient.sout.println(String.format("/move %d %d %d", moveObjId, x, y));
			whiteboard.scrollRectToVisible(e.getComponent().getBounds());
			moveObjId = -1;
		}
	};
	
	public void whiteboardResize() {
		int max_x = 1, max_y = 1;
		int x, y;
		
		for (Component j: whiteboard.getComponents()) {
			if (j.getBounds().getMaxX() > max_x)
				max_x = (int) j.getBounds().getMaxX();
			if (j.getBounds().getMaxY() > max_y)
				max_y = (int) j.getBounds().getMaxY();
			
		}
		
		x = (max_x > 500)?max_x:500;
		y = (max_y > 300)?max_y:300;

		whiteboard.setPreferredSize(new Dimension(x, y));
		whiteboard.revalidate();
	}
	
	public void addBtn(String s) {
		JButton btn = new JButton(s);
		widgetList.add(s);
		btnPanel.add(btn);
		btnPanel.setPreferredSize(new Dimension(100, btnPanel.getPreferredSize().height + 50));
		btn.addActionListener(btnListener);
		btnPanel.revalidate();
	}
}

