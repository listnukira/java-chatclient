package task;

import java.util.Vector;

public class GridifySleep implements Task {
	private int ms;
	
	public GridifySleep() {}
	
	public GridifySleep(String init_str) {
		ms = Integer.parseInt(init_str);
	}
	
	@Gridify(mapper="primeMapper", reducer="primeReducer")
	public Object execute() {
		try {
			System.out.println("Sleeping...");
			Thread.sleep(ms);
			System.out.println("Working...");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "wake up";
	}

	@Override
	public void init(String init_str) {
		ms = Integer.parseInt(init_str);
	}
	
	public Vector<GridifySleep> primeMapper(int num) {
		Vector<GridifySleep> tasks = new Vector<GridifySleep>(num);
		long period = ms / num;
		long remider = ms % num;
		
		for (int i = 0; i < num; ++i) {
			if (i + 1 == num) {
				tasks.add(new GridifySleep(String.format("%d", remider)));
			} else {
				tasks.add(new GridifySleep(String.format("%d", period * (i + 1))));
			}
		}
		return tasks;
	}
	
	public String primeReducer(Vector<Long> mapped_results) {
		return "wake up";
	}
}