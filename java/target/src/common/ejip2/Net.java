package ejip2;


import ejip2.jtcpip.JtcpipException;
import ejip2.jtcpip.StackParameters;
import ejip2.jtcpip.util.Debug;

/**
 * Used to set up our network stuff.
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 905 $ $Date: 2007/01/24 18:38:03 $
 */
public class Net {
	/**
	 * Holds a reference to the actual LinkLayer to abstract the source of the
	 * IP address
	 * 
	 * TODO: should not be that global! We can have more link layers.
	 */
	public static LinkLayer linkLayer;

	public static int[] ethX; // own ethernet address

	public static int ipX; // own ip address

	
	/**
	 * The one and only reference to this object.
	 * 
	 * for compatibility reasons
	 */
	private static Net single;
	
	
	
	
	
	
	/**
	 * Allocate buffer and create thread.
	 * 
	 * for compatibilty reasons.
	 * 
	 */
	//TODO: this is dangerous!
	
	public static Net init_not_used() {

		if (single != null)
			return single; // allready called init()

//		eth = new int[6];
//		eth[0] = 0x00;
//		eth[1] = 0xe0;
//		eth[2] = 0x98;
//		eth[3] = 0x33;
//		eth[4] = 0xb0;
//		eth[5] = 0xf7; // this is eth card for chello
//		eth[5] = 0xf8;
//		ip = (192 << 24) + (168 << 16) + (0 << 8) + 123;
		// ip = (192<<24) + (168<<16) + (0<<8) + 4;

		Udp.init();
		Packet.init();
		TcpIp.init();

		//
		// start my own thread
		//
		single = new Net();

		return single;
	}
	
	
	
	/**
	 * Does the required stuff to setup the network.
	 * 
	 * @throws JtcpipException
	 * 
	 * @throws JtcpipException
	 */
	public static void initNet_not_used() {
//		init(StackParameters.INIT_IP_ADDR, StackParameters.INIT_MAC);
	}
	
	
	
	

	/**
	 * Does the required stuff to setup the network.
	 * 
	 * TODO: use IP address and macAddr, or remove parameter!
	 * TODO: Net should not handle the LinkLayer, we can have
	 * more interfaces!
	 * 
	 * TODO: add all other stuff for initialization
	 * 
	 * @param myIpAddr
	 *            IP address of the stack
	 * @param macAddr
	 *            MAC address of the stack
	 * @throws JtcpipException
	 * @throws JtcpipException
	 */
//	public static LinkLayer init_not_used(String myIpAddr, String macAddr) {

//		eth = new int[6];
//		eth[0] = 0x00;
//		eth[1] = 0x05;
//		eth[2] = 0x02;
//		eth[3] = 0x03;
//		eth[4] = 0x04;
//		eth[5] = 0x07;
//
//		ip = (192 << 24) + (168 << 16) + (0 << 8) + 123;
//		// ip = ipStringToInt(myIpAddr);
//		// StringFunctions.macStrToByteArr(macAddr, mac);
//
//		linkLayer = CS8900.init(eth, ip);
		// NwLoopThread.createInstance(linkLayer);
//		return linkLayer;

		//		
		// while (linkLayer.ip == 0)
		// {
		// System.out.println("DOING DHCP!!!");
		// if (DHCPClient.setNetParams())
		// {
		// System.out.println();
		// System.out.println("Network initialized:");
		// System.out.println("====================");
		// System.out.println("IP addr: " + IP.ipIntToString(linkLayer.ip));
		// System.out.println("Subnet: " + IP.ipIntToString(linkLayer.netmask));
		// System.out.println("Gateway: " +
		// IP.ipIntToString(linkLayer.gateway));
		// System.out.println();
		// System.out.println("DHCP Server: " +
		// IP.ipIntToString(DHCPClient.getServerIP()));
		// System.out.println("Lease time: " + DHCPClient.getLeaseTime() + "h");
		// System.out.println("Renewal time: " + DHCPClient.getRenewalTime() +
		// "h");
		// System.out.println("-----------------------------------------------------------");
		// System.out.println();
		// break;
		// }
		// else
		// {
		// System.out.println();
		// System.out.println("Failed to setup the network - retry in "
		// + DHCPClient.retryTimeout + " sec.");
		// System.out.println("-----------------------------------------------------------");
		// System.out.println();
		//
		// // TODO: wait
		// //Thread.sleep(DHCPClient.retryTimeout * 1000);
		// System.out.println("Retry DHCP request.");
		// }
		// }
//	}

//	public static int ipStringToInt_not_used(String ipAddr) {
//		byte dots = 0;
//		short ipOctet = 0;
//		int ipInt = 0;
//		for (int i = 0; i <= ipAddr.length(); i++) {
//			if (i == ipAddr.length() || ipAddr.charAt(i) == '.') {
//				if (i < ipAddr.length() && ++dots == 4) {
//					if (Debug.enabled)
//						Debug.println("Too many dots in  ipAddr", Debug.DBG_IP);
//
//				}
//
//				if (ipOctet < 0 || ipOctet > 255) {
//					if (Debug.enabled)
//						Debug
//								.println("Wrong IP values in ipAddr",
//										Debug.DBG_IP);
//
//				}
//
//				ipInt = (ipInt << 8) | (ipOctet & 0xFF);
//				ipOctet = 0;
//			} else if (ipAddr.charAt(i) >= '0' && ipAddr.charAt(i) <= '9')
//				ipOctet = (short) (ipOctet * 10 + (ipAddr.charAt(i) - '0'));
//			else {
//				if (Debug.enabled)
//					Debug.println("Wrong char in IP address ipAddr",
//							Debug.DBG_IP);
//
//			}
//		}
//
//		if (dots != 3) {
//			if (Debug.enabled)
//				Debug.println("IP address too short ipAddr", Debug.DBG_IP);
//
//		}
//		return ipInt;
//	}
}
