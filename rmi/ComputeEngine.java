package rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import task.*;

public class ComputeEngine extends UnicastRemoteObject implements Compute {
	
	public ComputeEngine() throws RemoteException {
		super();
	}
	
	public Object executeTask(Task task, String target) {
		System.out.println(target);
		return task.execute();
	}
	
}
