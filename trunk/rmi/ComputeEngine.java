package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import task.*;

public class ComputeEngine extends UnicastRemoteObject implements Compute {
	
	private static final long serialVersionUID = 1L;

	public ComputeEngine() throws RemoteException {
		super();
	}
	
	public Object executeTask(Task task, String target) {
		return task.execute();
	}
}
