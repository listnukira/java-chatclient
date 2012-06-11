package ChatClient;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.net.*;
import java.awt.Component;
import java.awt.Dimension;
import java.io.*;

import widgets.*;
import rmi.*;
import task.*;

public class ChatClient {
	
	private String host = "localhost";
	private int port = 0;
	
	ChatClientGUI gui = null;
	public static volatile String name;
	Socket socket = null;
	public static BufferedReader sin = null;
	public static PrintWriter sout = null;
	volatile boolean nameIsReady = false;
	volatile boolean nameChecked = false;
	volatile boolean isConnect = false;
	volatile boolean isIdle = false;
	ArrayList<Msg> postLog = new ArrayList<Msg>();
	Map<String, Task> taskPool = new LinkedHashMap<String, Task>();
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
		isIdle = false;
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
			gui.chatArea.append("The server does not exist. Please type different domain and/or port.\n");
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
			/* permission setting, only owner add listener */
			if (name.equals(m.getUsr())) {
				((Widget)object).addMouseListener(gui.widgetListener);
				((Widget)object).addMouseMotionListener(gui.widgetListener);
			}
			/* Add new button */
			if (gui.widgetList.indexOf(m.getType()) == -1) {
				gui.addBtn(m.getType());
			}
		} catch (ClassNotFoundException e) {
			gui.chatArea.append(String.format("Error:<%s> is not found for %s", m.getType(), 
					m.getContext()));
			return;
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

	public void showTaskLog() {
		for (String key: taskPool.keySet()) {
			gui.chatArea.append(String.format("Task ID: %s, Task Type: %s\n", key, 
					taskPool.get(key).getClass().getSimpleName()));
		}
	}
	
	/*	msg ex:  /task t1 Pi 3
	 *			  cmd id type arg
	 */
	public void createTask(String msg) {
		String[] splited = msg.split(" ", 4);
		
		if (taskPool.containsKey(splited[1])) {
			gui.chatArea.append(String.format("Error: tid: %s is used.\n", splited[1]));
			return;
		}
		
		try {
			Class<?> c = Class.forName("task." + splited[2]);
			Task o = (Task) c.newInstance();
			o.init(splited[3]);
			taskPool.put(splited[1], o);
			gui.chatArea.append(String.format("Task ID: %s, Task Type: %s\n", splited[1], splited[2]));
		} catch (ClassNotFoundException e) {
			gui.chatArea.append(String.format("Error: TaskType %s is not found.\n", splited[2]));
			return;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/*	msg ex:  /rexe tid [user]
	 */
	public void rexeTask(String msg) {
		String[] splited = msg.split(" ", 3);
		
		if (splited.length == 1) {
			return;
		}
		
		String tid = splited[1];
		String target = (splited.length == 3) ? splited[2] : null;
		
		Task task = (Task) taskPool.get(tid);
		if (task == null) {
			gui.chatArea.append(String.format("Task ID %s is not exist\n", tid));
		} else {
			new rexeThread(task, target).start();
		}
	}
	
	/*	msg ex:  2  10  20
	 *			id   x   y 
	 */
	public void moveObj(String msg) {
		String[] splited = msg.split(" ", 3);
		int objID = Integer.parseInt(splited[0]);
		int x = Integer.parseInt(splited[1]);
		int y = Integer.parseInt(splited[2]);
		
		/* modify client side log */
		for (Msg m: postLog) {
			if (m.getMsgid() == objID) {
				m.setX(x);
				m.setY(y);
				break;
			}
		}
		
		/* change object location */
		for (Component j : gui.whiteboard.getComponents()) {
			if ( Integer.parseInt(j.getName()) == objID ) {
				j.setLocation(x, y);
				break;
			}
		}
		
		gui.whiteboardResize();
	}
	
	/*	msg ex:  2  aaa ttt ccc
	 *			id   attribute
	 */
	public void changeObj(String msg) {
		String[] splited = msg.split(" ", 2);
		int objID = Integer.parseInt(splited[0]);
		
		/* modify client side log */
		for (Msg m: postLog) {
			if (m.getMsgid() == objID) {
				m.setMsg(String.format("%d %d %s", m.getX(), m.getY(), splited[1]));
				break;
			}
		}
		
		/* change object appearance */
		for (Component j : gui.whiteboard.getComponents()) {
			if ( Integer.parseInt(j.getName()) == objID ) {
				((Widget) j).parseCommand(splited[1]);
				break;
			}
		}
		
		gui.whiteboardResize();
		gui.whiteboard.repaint();
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

				/* when name checked, registry client side rmi */
				Compute computeEngine = (Compute) new ComputeEngine();
				Registry registry = LocateRegistry.getRegistry(1099);
				registry.rebind(name, computeEngine);
				
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
					} else if (splitMsg[0].equals("/change")) {
						changeObj(splitMsg[1]);
					} else {}
					
					gui.chatArea.setCaretPosition(gui.chatArea.getDocument().getLength());
				}

				/* close connection */
				isConnect = false;
				isIdle = true;
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
	
	/* client using another thread run RMI */
	public class rexeThread extends Thread {
		private Task task;
		private String target;
		public rexeThread(Task task, String target) {
			this.task = task;
			this.target = target;
		}
		
		@Override
		public void run() {
			try {			
				Compute remoteCompute = (Compute) Naming.lookup("rmi://localhost:1099/@SERVER");
				String result = remoteCompute.executeTask(task, target).toString();
				gui.chatArea.append(String.format("%s\n", result));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}

