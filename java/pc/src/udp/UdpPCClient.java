/*
  This file is part of JOP, the Java Optimized Processor (http://www.jopdesign.com/)

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
