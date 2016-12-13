package be.uantwerpen.group1.systemy.networking;

/**
 * Class to calculate hashes
 * 
 */
public class Hashing {

	/**
	 * function to calculate hash from String
	 * return's '0' if String doesn't exist
	 * 
	 * @param input: String to hash
	 * @return int: hashed value of input
	 */
	public static int hash(String input) {
		if (input != null) {
			return (Math.abs((input.hashCode())) % 32768);
		} else {
			return 0;
		}
	}
	
}
