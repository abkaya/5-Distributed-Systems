package be.uantwerpen.group1.systemy.networking;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Class to get 
 * 
 * @author Robin
 */
public class Interface {
	
	public static String getIP() throws SocketException {
		
		String IP = null;
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		while(en.hasMoreElements()){
			NetworkInterface ni =(NetworkInterface) en.nextElement();
			Enumeration<InetAddress> ee = ni.getInetAddresses();
			while(ee.hasMoreElements()) {
				InetAddress ia = (InetAddress) ee.nextElement();
				if(!ia.isLoopbackAddress()) {
					IP = ia.getHostAddress();
					System.out.println("Detected address: " + ia.getHostAddress());
				}
			}
		}
		return IP;
		
	}
		
}
