package ChatClient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import task.*;

public interface Compute extends Remote {
	Object executeTask(Task t, String target) throws RemoteException;
}