/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package jeopard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class PcUdpControl extends PcControl {

	final static int UDP_PORT = 8888;

	InetAddress address;
	DatagramSocket sendSocket;
	
	public PcUdpControl() {
		try {
			address = InetAddress.getByName("192.168.1.2");
			sendSocket = new DatagramSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	public void send() {
		pack();
		byte buf[] = new byte[data.length*4];
		for (int i=0; i<data.length; ++i) {
			for (int j=0; j<4; ++j) {
				buf[i*4+j] = (byte) (data[i] >>> (8*(3-j)));
			}
		}
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, UDP_PORT);
		try {
			sendSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
