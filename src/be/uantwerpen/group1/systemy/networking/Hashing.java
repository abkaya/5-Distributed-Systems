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
		if (!input.isEmpty()) {
			/*The hash function below collides on addresses certain in our local network (e.g. 192.168.1.107 produces the same hash as 192.168.1.108)
			 * 
			 * The Math.abs function is not desirable, due to a possibility(1 in 4 billion) of returning a negative value.
			 * So we're going to force the sign bit to 0 instead, using a mask. (Source: http://algs4.cs.princeton.edu/34hash/)
			 * So instead ofa applying the modulo operator on a signed 32 bit integer, we'll be applying it on an unsigned 31 bit integer.
			 * 
			 * Since we're using a hashmap, we need tablength-1 for the modulo operation. Making that value 32767 for a 16 bit table
			 */
			//return (Math.abs((input.hashCode())) % 32768);
			return (input.hashCode() & 0x7fffffff) % 32768;
		} else {
			return 0;
		}
	}
	
}
