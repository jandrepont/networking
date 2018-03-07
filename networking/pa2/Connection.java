package pa2;
import java.net.InetAddress;
import java.net.InterfaceAddress; 
import java.net.NetworkInterface; 
import java.net.SocketException; 
import java.util.Collections; 
import java.util.List; 

/**
 * Manages locating the correct IP for localhost. Composite from various 
 * internet sources. 
 * @version 1 December 2017 
 */ 
public class Connection {
	
	/**
	 * Queries the system for a list of local IPs that can be used and finds 
	 * the first available that is not the loopback IP and returns it as a 
	 * <code>String</code> 
	 * @return a public IP for this machine 
	 */ 
	public static String getLocalNotLoopbackIP() {
		try {
			List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces()); 
			for (NetworkInterface ni : nis) {
			  if (!ni.isLoopback() && ni.isUp() && ni.getHardwareAddress() != null) {
				for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
				  if (ia.getBroadcast() != null) { 
					return ia.getAddress().getHostAddress();
				  }
				}
			  }
			}
		} catch (SocketException e) {
			e.printStackTrace(); 
		} 
		
		return null; 
	} 
}
