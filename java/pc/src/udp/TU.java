package udp;
import java.io.*;
import java.net.*;

/**
*	send UPD requests to jop and receive UPD packets for debug output.
*	Who is the server in this application?
*/

public class TU extends Thread {


	static DatagramSocket socket;

	/**
	*	kind of 'ping' the 'client' to send a new packet.
	*/
	public void run() {


		try {

			// send request
			byte[] buf = new byte[256];
			InetAddress address = InetAddress.getByName("192.168.0.4");
			DatagramPacket packet = new DatagramPacket(buf, 0, address, 1625);
			for (;;) {
				socket.send(packet);
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}


	/**
	*	main is the 'server'.
	*/
	public static void main(String[] args) throws IOException {


		// get a datagram socket
		socket = new DatagramSocket();

		TU t = new TU();
		t.start();
	
		byte[] buf = new byte[256];

		// get response
		for (;;) {

			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			// display response
			byte[] resp = packet.getData();
			for (int i=0; i<packet.getLength(); ++i) {
				System.out.print((char) resp[i]);
			}
		}
	
		// socket.close();
	}
}
