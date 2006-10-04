package udp;
import java.io.*;
import java.net.*;

/**
*	send UPD requests to jop and receive UPD packets for debug output.
*/

public class UdpPCClient {


	static DatagramSocket socket;
	static DatagramSocket rcv_socket;


	/**
	*	main is the 'server'.
	*/
	public static void main(String[] args) {


		try {
			// get a datagram socket
			socket = new DatagramSocket();
			rcv_socket = new DatagramSocket(1234);
		} catch (IOException e) {
			System.out.println(e);
		}

		new Thread() {
			/**
			*	kind of 'ping' the 'client' to send a new packet.
			*/
			public void run() {

				int cnt = 1;
				try {
					InetAddress address = InetAddress.getByName("192.168.0.123");
					for (;;) {
						// send request
						byte[] buf = ("Hello "+cnt).getBytes();
						++cnt;
						DatagramPacket packet = 
							new DatagramPacket(buf, buf.length, address, 1234);
						socket.send(packet);
						System.out.print('*');
						Thread.sleep(1000);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}.start();
	
		byte[] buf = new byte[256];

		// get response
		for (;;) {

			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				rcv_socket.receive(packet);
			} catch (IOException e) {
				System.out.println(e);
			}

			// display response
			byte[] resp = packet.getData();
			for (int i=0; i<packet.getLength(); ++i) {
				System.out.print((char) resp[i]);
			}
			System.out.println();
		}
	
		// socket.close();
	}
}
