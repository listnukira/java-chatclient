package task;

import java.io.Serializable;

public interface Task extends Serializable {
	Object execute();
	void init(String init_str);
}