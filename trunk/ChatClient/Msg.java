package ChatClient;

public class Msg {

	private String type = "";
	private String usr = "";
	private String msg = "";
	private String widgetMsg = "";
	private int msgid = 0;
	private int x= 0;
	private int y= 0;

	/*  client Msg constructor
	 *	post : Student1 101 String This is a test.
	 *	post : Student1 101 RectangleWidget 10 20 
	 *	post : Student1 101 RectangleWidget 10 20 #0000ff 50 12
	 */
	public Msg(String msg) {
		String[] splitMsg = msg.trim().split(" ", 4);

		this.usr = splitMsg[0];
		this.msgid = Integer.parseInt(splitMsg[1]);
		this.type = splitMsg[2];

		if (splitMsg.length == 3)
			this.msg = "";
		else
			this.msg = splitMsg[3];

		if (type.equals("String") == false) {
			splitMsg = splitMsg[3].split(" ", 3);
			this.x = Integer.parseInt(splitMsg[0]);
			this.y = Integer.parseInt(splitMsg[1]);
			if (splitMsg.length == 3)
				this.widgetMsg = splitMsg[2];
		}
	}

	public String getContext() {
		return String.format("%s posted message '%d' in %s: %s",
				this.usr, this.msgid, this.type, this.msg);
	}

	public int getMsgid() {
		return msgid;
	}

	public String getUsr() {
		return usr;
	}

	public String getMsg() {
		return msg;
	}

	public String getWidgetMsg() {
		return widgetMsg;
	}
	
	public String getType() {
		return type;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
}