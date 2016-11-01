package be.uantwerpen.group1.systemy.networking;

import static org.apache.commons.lang3.SystemUtils.IS_OS_UNIX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;

/**
 * RMI class which can be used bind objects from different types in the
 * registry, using Generics, or simply request registries on remote or local
 * machines. usage: RMI<Type> rmi = new RMI<Type>(...);
 * 
 * @author Abdil Kaya
 *
 */
public class RMI<T>
{
	/** Remote method invocation registry */
	private Registry registry = null;

	/** 
	 * Constructor w/o parameters typically used by nodes which don't need to start
	 * an rmi registry or bind objects to it
	 */
	public RMI()
	{
		setPermissions();
	}

	/**
	 * Constructor for RMI servers Starts the rmi registry on this machine on a given port.
	 * 
	 * @param hostName : The IP address of the name server
	 * @param name : name to bind the remote reference to in the registry
	 * @param obj : Object of which to create a stub
	 * @param port : (optional) The port to bind the registry on. Default at 1099 if not provided.
	 */
	public RMI(String hostName, String name, T obj, int port)
	{
		setPermissions();
		System.setProperty("java.rmi.server.hostname", hostName);
		startRegistry(port);
		bindObject(name, obj);
	}

	/**
	 * Constructor for RMI servers Starts the rmi registry on this machine on port 1099.
	 * 
	 * @param hostName : The IP address of the name server
	 * @param name : name to bind the remote reference to in the registry
	 * @param obj : Object of which to create a stub
	 */
	public RMI(String hostName, String name, T obj)
	{
		this(hostName, name, obj, 1099);
	}

	/**
	 * Starts the registry on the local machine.
	 * @param port : the port to start the rmi registry on
	 */
	private void startRegistry(int port)
	{
		try
		{
			registry = LocateRegistry.createRegistry(port);
		} catch (RemoteException e)
		{
			System.err.println("The rmi registry might already be running or port " + port + " is in use.");
		}
	}

	/**
	 * Returns the rmi registry on host with `hostName`, or null if the registry
	 * doesn't exist This method will typically be used by the client nodes.
	 * 
	 * @param hostName : name of the server on which the registry is running
	 * @return registry
	 */
	public Registry getRegistry(String hostName, int port)
	{
		Registry registry = null;
		try
		{
			registry = LocateRegistry.getRegistry(hostName, port);
		} catch (RemoteException e)
		{
			return null;
		}
		return registry;
	}

	/**
	 * Method to bind a remote object in the rmi registry of the local server
	 * (may or may not be the local port). 
	 * 
	 * @param name : name to bind the remote object to in the registry
	 * @param obj  : skeleton of the remote object to bind in the registry
	 * @return boolean result to check whether or not calling this method failed
	 */
	@SuppressWarnings("unchecked")
	public boolean bindObject(String name, T obj)
	{
		try
		{
			T stub = (T) UnicastRemoteObject.exportObject((Remote) obj, 0);
			registry.rebind(name, (Remote) stub);
			System.out.println(name + " bound");
			return true;
		} catch (RemoteException e)
		{
			return false;
		}
	}

	/**
	 * Returns the stub of the remote object requested. Requires the name it was bound to in the registry.
	 * This method is to be used by nodes utilising
	 *  
	 * @param obj : an object of the interface to which the stub will be returned
	 * @param name : name of the remote object as it was bound in the registry
	 * @param hostName : hostName running  the rmi registry
	 * @param port : port on which the rmi registry is running
	 * @return the stub or null if it fails
	 */
	@SuppressWarnings("unchecked")
	public T getStub(T obj, String name, String hostName, int port)
	{
		Registry registry = getRegistry(hostName, port);
		try
		{
			obj = (T) registry.lookup(name);
		} catch (RemoteException | NotBoundException e)
		{
			return null;
		}
		return obj;
	}

	/**
	 * Sets required user java policies in their home folder. By default, the
	 * security manager searches for java.policy in a user's home directory We
	 * will set the required policy and create the security manager afterwards.
	 * 
	 */
	private void setPermissions()
	{
		PrintWriter out = null;
		/*
		 * We'd like to set the paths properly w.r.t. to how the OS handles paths/directories note: windows does not get detected as unix.
		 */
		try
		{
			if (IS_OS_UNIX)
			{
				out = new PrintWriter(System.getProperty("user.home") + "/.java.policy");
				System.out.println("Detected OS: UNIX");
			} else if (IS_OS_WINDOWS)
			{
				System.out.println("Detected OS: Windows");
				out = new PrintWriter(System.getProperty("user.home") + "\\.java.policy");
			}
			/*
			 * In order to use the security manager, we'll have to give executables the right permissions. 
			 * Not using the security manager gives errors and setting these up for each client separately is tedious work.
			 */
			if (out != null)
			{
				out.println("grant {");
				out.println(" permission java.security.AllPermission;");
				out.println("};");
				out.close();
			}
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		if (System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}

	}

}
