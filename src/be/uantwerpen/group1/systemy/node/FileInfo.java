package be.uantwerpen.group1.systemy.node;

/**
 * abstract data type for storing file info
 *
 * @author Robin Janssens
 */

 public class FileInfo {

 	private String name;
 	private boolean local;

 	/**
 	 * Constructor
 	 * 
 	 * @param name : String
 	 * @param local : boolean
 	 */
 	public FileInfo(String name, boolean local){
 		this.name = name;
 		this.local = local;
 	}
 	
 	// -----
 	// GET
 	// -----
 	public String getName()
 	{
 		return name;
 	}

 	public boolean getLocal()
 	{
 		return local;
 	}

 	// -----
 	// SET
 	// -----
 	public void setName(String name)
 	{
 		this.name = name;
 	}

 	public void setLocal(boolean local)
 	{
 		this.local = local;
 	}
 }
