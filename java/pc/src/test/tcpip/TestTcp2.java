/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Nelson Langkamp

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

package test.tcpip;

import java.net.*;
import java.io.*;

public class TestTcp2 {

	static Socket socket;

	public static void main(String[] args) {
		try {
			socket = new Socket("192.168.0.123", 44);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			Thread write = new Thread() {
				PrintStream os = new PrintStream(socket.getOutputStream());

				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));

				public void run() {
					try {
						while (true) {
							String line = br.readLine();
							if (line.equals("quit"))
								break;
							os.println(line);
							os.flush();
						}
						os.close();
						socket.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			write.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			Thread read = new Thread() {
				DataInputStream is = new DataInputStream(socket
						.getInputStream());

				public void run() {
					try {
						int fromServer = 0;
						while (true) {
							if ((fromServer = is.read()) != -1)
								System.out.println("Server: "
										+ (char) fromServer);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			read.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
