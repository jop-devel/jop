import java.io.*;
import java.net.*;
import java.util.*;

/**
*	send UPD requests to jop and receive UPD packets for debug output.
*/

public class UDPDbg {

	public static void main(String[] args) throws IOException {


		// get a datagram socket
		DatagramSocket socket = new DatagramSocket();

		byte[] sndBuf = new byte[256];
		byte[] rcvBuf = new byte[256];

		InetAddress address;
		if (args.length!=0) {
			address = InetAddress.getByName(args[0]);
		} else {
			address = InetAddress.getByName("192.168.0.4");
		}
		DatagramPacket send = new DatagramPacket(sndBuf, 0, address, 1625);

		socket.setSoTimeout(1000);

		// get response
		for (;;) {

			// this is neccessary! I don't know why I have to construct a new packet for every reveive.
			DatagramPacket rcv = new DatagramPacket(rcvBuf, rcvBuf.length);

			try {
				Thread.sleep(200);
				socket.send(send);
				socket.receive(rcv);

				// display response
				byte[] resp = rcv.getData();
				FileWriter log = new FileWriter("log.txt", true);
				for (int i=0; i<rcv.getLength(); ++i) {
					System.out.print((char) resp[i]);
					log.write((char) resp[i]);
				}
				log.close();
			} catch (Exception e) {
				System.out.println(e);
			}

		}
	
		// socket.close();
	}

	/** This causes UPD packets on socket 137!!! */
	public static void dbg(DatagramPacket p) {

		System.out.println("Address: "+p.getAddress());
		System.out.println("Length: "+p.getLength());
		System.out.println("Port: "+p.getPort());
	}
}
