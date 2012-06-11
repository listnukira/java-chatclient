package task;

public class DisplayResultForTA {

	public static String getUsername() {
		return ChatClient.ChatClient.name;
	}
	
	public static void displayUsingWidget(String widget, int x, int y, String args) {
		String post = String.format("/post %s %d %d %s", widget, x, y, args);
		ChatClient.ChatClient.sout.println(post);
	}
}
