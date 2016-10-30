/**
 * RMI class which can be used bind objects from different types in the registry, using Generics, or simply
 * request registries on remote or local machines.
 */
package be.uantwerpen.group1.systemy.networking;

import static org.apache.commons.lang3.SystemUtils.IS_OS_UNIX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Abdil Kaya
 *
 */
public class RMI<T>
{
	/**
	 * T must be an interface which extends the Remote interface
	 */
	private T obj;

	/**
	 * This name will depend on the type of the class and used to associate with
	 * the remote reference
	 */
	private String name;

	/**
	 * Constructor w/o parameters typically used by nodes which don't need to
	 * use
	 */
	public RMI()
	{
		setPermissions();
	}

	/**
	 * Constructor for RMI servers Starts the rmi registry on this machine on
	 * the default port (1099).
	 * 
	 * @param hostName:
	 *            The IP address of the name server
	 * @param name:
	 *            name to bind the remote reference to in the registry
	 * @param obj:
	 *            Object of which to create a stub
	 */
	public RMI(String hostName, String name, T obj)
	{
		this.obj = obj;
		this.name = name;

		setPermissions();

		System.setProperty("java.rmi.server.hostname", hostName);

		/*
		 * Bind the name to a remote reference (the stub) in the registry
		 */
		try
		{
			@SuppressWarnings("unchecked")
			T stub = (T) UnicastRemoteObject.exportObject((Remote) this.obj, 0);
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.rebind(this.name, (Remote) stub);
			System.out.println(this.name + " bound");

		} catch (RemoteException e)
		{
			System.err.println(this.name + " exception:");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Sets required user java policies in their home folder.
	 * By default, the security manager searches for  java.policy in a user's home directory
	 * We will set the required policy and create the security manager afterwards.
	 * 
	 */
	public void setPermissions()
	{
		PrintWriter out = null;
		/*
		 * We'd like to set the paths properly w.r.t. to how the OS handles
		 * paths/directories note: windows does not get detected as unix.
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
			 * In order to use the security manager, we'll have to give
			 * executables the right permissions. Not using the security manager
			 * gives errors and setting these up for each client separately is
			 * tedious work.
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

	/**
	 * Returns the rmi registry on host with 'hostName', or null if the registry doesn't exist
	 * 
	 * @param hostName
	 * @return registry
	 */
	public Registry getRegistry(String hostName)
	{
		Registry registry = null;
		try
		{
			registry = LocateRegistry.getRegistry(hostName, 1099);
		} catch (RemoteException e)
		{
			return null;
			//e.printStackTrace();
		}
		return registry;
	}

}
