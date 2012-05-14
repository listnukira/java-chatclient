package ChatClient;

import java.util.*;
import java.net.*;
import java.awt.Component;
import java.awt.Dimension;
import java.io.*;

import javax.swing.JButton;

import widgets.*;

public class ChatClient {
	
	private String host = "localhost";
	private int port = 0;
	
	ChatClientGUI gui = null;
	volatile String name;
	Socket socket = null;
	BufferedReader sin = null;
	PrintWriter sout = null;
	volatile boolean nameIsReady = false;
	volatile boolean nameChecked = false;
	volatile boolean isConnect = false;
	ArrayList<Msg> postLog = new ArrayList<Msg>();
	PrintWriter login = null;
	PrintWriter logout = null;

	public ChatClient(String host, int port, ChatClientGUI gui) {
		this.gui = gui;
		this.host = host;
		this.port = port;
	}
	
	public void reset(String host, int port) {
		this.host = host;
		this.port = port;
		nameIsReady = false;
		nameChecked = false;
		isConnect = false;
		postLog.clear();

		gui.clabel.setText("Input Username: ");
		gui.chatArea.setText("Username : ");
	}
	
	public int connectToServer() {
		InetSocketAddress iaddr = new InetSocketAddress(host, port);
		socket = new Socket();
		try {
			socket.connect(iaddr, 1500);
			sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sout = new PrintWriter(socket.getOutputStream(), true);	
		
			if (socket != null && sout != null && sin != null) {
				Thread t = new Thread(new ReceiveMsg());
				t.start();
				isConnect = true;
			}
			return 0;
		} catch (IOException e) {
			System.err.println("The server does not exist. Please type different domain and/or port.");
			System.err.println(e);
			return -1;
		}
	}

	/* remove post msg from postLog, ex: Student1 1 */
	public void rmPost(String msg) {
		String[] splited = msg.split(" ", 2);
		String rmUsr = splited[0];
		int rmMsgid = Integer.parseInt(splited[1]);

		for (Msg m: postLog) {
			if (m.getMsgid() == rmMsgid) {
				gui.chatArea.append(String.format("%s remove message '%d': %s\n", rmUsr, rmMsgid, m.getMsg()));
				if (m.getType().equals("String") == false) {
					for (Component j : gui.whiteboard.getComponents()) {
						if (j.getName().equals(String.valueOf(m.getMsgid()))) {
							((Widget) j).destroy();
							gui.whiteboard.remove(j);
							break;
						}
					}
					gui.whiteboard.repaint();
					gui.whiteboardResize();
				}
				postLog.remove(m);
				break;
			}
		}
	}

	public void savePost(String postStr) {
		Msg m = new Msg(postStr);
		postLog.add(m);
		gui.chatArea.append(m.getContext());
		gui.chatArea.append("\n");
		
		if (m.getType().equals("String"))
			return;

		Object object = null;
		try {
			Class<?> c = Class.forName("widgets." + m.getType());
			object = c.newInstance();
			((Widget)object).parseCommand(m.getWidgetMsg());
			((Widget)object).setLocation(m.getX(), m.getY());
			((Widget)object).setName(String.valueOf(m.getMsgid()));
			((Widget)object).addMouseListener(gui.widgetListener);
			((Widget)object).addMouseMotionListener(gui.widgetListener);
			
			/* Add new button */
			String s = m.getType().substring(0, m.getType().length() - 6);
			if (gui.widgetList.indexOf(s) == -1) {
				gui.addBtn(s);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		gui.whiteboard.add(((Widget)object));
		gui.whiteboardResize();
		gui.whiteboard.repaint();
	}
	
	public void showPostLog() {
		for (Msg m: postLog) {
			gui.chatArea.append(m.getContext() + "\n");
		}
	}

	/*	msg ex:  2  10  20
	 *			id   x   y 
	 */
	public void moveObj(String msg) {
		String[] splited = msg.split(" ", 3);
		
		for (Msg m: postLog) {
			if (m.getMsgid() == Integer.parseInt(splited[0])) {
				m.setX(Integer.parseInt(splited[1]));
				m.setY(Integer.parseInt(splited[2]));
				break;
			}
		}
		
		for (Component j : gui.whiteboard.getComponents()) {
			if (j.getName().equals(splited[0])) {
				j.setLocation(Integer.parseInt(splited[1]), Integer.parseInt(splited[2]));
				break;
			}
		}
		
		gui.whiteboardResize();
	}
	
	public class ReceiveMsg implements Runnable {
		public void run() {
			String response;

			/* keep on reading socket's response */
			try {
				/* checked name */
				while (nameChecked == false && (response = sin.readLine()) != null) {
					String[] splitMsg = response.split(" ", 2);
					if (response.startsWith("/msg Error:")) {
						gui.chatArea.append(splitMsg[1] + "\n");
						response = sin.readLine();
						splitMsg = response.split(" ", 2);
						gui.chatArea.append(splitMsg[1]);
					} else {
						gui.chatArea.append(splitMsg[1] + "\n");
						gui.clabel.setText("Input command: ");
						login = new PrintWriter( (new FileWriter("input_" + name + ".txt", true)), true);
						logout = new PrintWriter( (new FileWriter("output_" + name + ".txt", true)), true);
						login.println(splitMsg[1]);
						nameChecked = true;
					}
					gui.chatArea.setCaretPosition(gui.chatArea.getDocument().getLength());
				}

				/* read server's response */
				while ((response = sin.readLine()) != null) {
					login.println(response);
					String[] splitMsg = response.trim().split(" ", 2);

					if (splitMsg[0].equals("/msg")) {
						gui.chatArea.append(splitMsg[1]);
						gui.chatArea.append("\n");
					} else if (splitMsg[0].equals("/post")) {
						savePost(splitMsg[1]);
					} else if (splitMsg[0].equals("/kick")) {
						if (splitMsg[1].equals(name))
							sout.println("/leave");
					} else if (splitMsg[0].equals("/remove")) {
						rmPost(splitMsg[1]);
					} else if (splitMsg[0].equals("/move")) {
						moveObj(splitMsg[1]);
					} else {}
					
					gui.chatArea.setCaretPosition(gui.chatArea.getDocument().getLength());
				}

				/* close connection */
				isConnect = false;
				sin.close();
				sout.close();
				socket.close();
				
				/* reset whiteboard and chatArea */
				gui.whiteboard.removeAll();
				gui.boardScroll.repaint();
				gui.whiteboard.setPreferredSize(new Dimension(500, 300));
				
				gui.boardScroll.revalidate();
				gui.chatArea.setText("Idle...");

			} catch (IOException e) {
				System.err.println("IOException in run(): " + e);
			}
		}
		
	}
}