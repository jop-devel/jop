package udp;
import java.io.*;
import java.net.*;

/**
*	send UPD requests to jop and receive UPD packets for debug output.
*	Who is the server in this application?
*/

public class TU extends Thread {


	static DatagramSocket socket;
	static DatagramSocket rcv_socket;
	/**
	*	kind of 'ping' the 'client' to send a new packet.
	*/
	public void run() {


		try {

			// send request
			byte[] buf = new byte[256];
			InetAddress address = InetAddress.getByName("192.168.0.123");
			DatagramPacket packet = new DatagramPacket(buf, 1, address, 1234);
			for (;;) {
				socket.send(packet);
				System.out.print('*');
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}


	/**
	*	main is the 'server'.
	*/
	public static void main(String[] args) {


		try {
			// get a datagram socket
			socket = new DatagramSocket();
			rcv_socket = new DatagramSocket(1234);
		} catch (Exception e) {
			System.out.println(e);
		}

		TU t = new TU();
		t.start();
	
		byte[] buf = new byte[256];

		// get response
		for (;;) {

			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				rcv_socket.receive(packet);
			} catch (Exception e) {
				System.out.println(e);
			}

			// display response
			byte[] resp = packet.getData();
			for (int i=0; i<packet.getLength(); ++i) {
				System.out.print((char) resp[i]);
			}
		}
	
		// socket.close();
	}
}
