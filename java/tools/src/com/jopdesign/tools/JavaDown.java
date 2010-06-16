/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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

//Copyright: www.jopdesign.com
package com.jopdesign.tools;

import java.io.*;
import java.util.*;
import gnu.io.*;

//import javax.comm.*;

public class JavaDown {

	static boolean echo = false;

	static boolean usb = false;

	static Enumeration portList;

	static CommPortIdentifier portId;

	static SerialPort serialPort;

	static OutputStream outputStream;

	static InputStream iStream;

	static PrintStream sysoutStream;

	static InputStream sysinStream;

	final static String exitString = "JVM exit!";

	final static char prog_char[] = { '|', '/', '-', '\\', '|', '/', '-', '\\' };

	public static void main(String[] args) {
		sysoutStream = System.out;
		sysinStream = System.in;

		if (args.length < 2) {
			sysoutStream.println("usage: java JavaDown [-e] [-usb] file port");
			System.exit(-1);
		}

		if (args[0].equals("-e") || args[1].equals("-e")) {
			echo = true;
		}
		if (args[0].equals("-usb") || args[1].equals("-usb")) {
			usb = true;
		}

		String fname = args[args.length - 2];
		String portName = args[args.length - 1];

		try {
			portId = CommPortIdentifier.getPortIdentifier(portName);
		} catch (NoSuchPortException e2) {
			sysoutStream.println("Can not open port " + portName);
		}
		try {
			serialPort = (SerialPort) portId.open("JavaDown", 2000);
		} catch (PortInUseException e) {
			sysoutStream.println(e);
		}
		try {
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		} catch (UnsupportedCommOperationException e) {
			sysoutStream.println(e);
		}
 		try {
			outputStream = serialPort.getOutputStream();
			iStream = serialPort.getInputStream();
		} catch (IOException e) {
			sysoutStream.println(e);
		}

		downLoad(fname);

		if (echo) {
			echo();
		}

		serialPort.close();

	}

	static final int CNTMOD = 4;

	static public boolean downLoad(String fname) {
		FileReader fileIn = null;
		try {
			fileIn = new FileReader(fname);
		} catch (FileNotFoundException e1) {
			sysoutStream.println("Error opening " + fname);
			System.exit(-1);
		}

		// read .jop file word for word and write bytes to JOP
		try {
			StreamTokenizer in = new StreamTokenizer(fileIn);
			in.slashSlashComments(true);
			in.whitespaceChars(',', ',');
			int cnt = 0;
			int lbuf [] = new int[CNTMOD];
			for (; in.nextToken() != StreamTokenizer.TT_EOF; ++cnt) {
				// in.nval contains the next 32 bit word to be sent
				int l = (int) in.nval;
				lbuf[cnt % CNTMOD] = l;

				// Java code length at index 1 position in .jop
				if (cnt == 1) {
					sysoutStream.println(l + " words of Java bytecode ("
							+ (l / 256) + " KB)");
				}
				for (int i = 0; i < 4; i++) {
					byte b = (byte) (l >> ((3 - i) * 8));
					outputStream.write(b);
				}

				if (!usb) {
					if (cnt % CNTMOD == CNTMOD-1) {
						for (int k = 0; k < CNTMOD; k++) {
							int r = 0;
							for (int i = 0; i < 4; i++) {
								r = (r << 8) | (iStream.read() & 0xff);
							}
							if (r != lbuf[k]) {
								sysoutStream.println("received word differs from sent word");
							}
						}
					}
				}

				if ((cnt & 0x3f) == 0) {
					sysoutStream.print(prog_char[(cnt >> 6) & 0x07] + "\r");
				}
			}

			if (!usb) {
				for (int k = 0; k < cnt % CNTMOD; k++) {
					int r = 0;
					for (int i = 0; i < 4; i++) {
						r = (r << 8) | (iStream.read() & 0xff);
					}
					if (r != lbuf[k]) {
						sysoutStream.println("received word differs from sent word");
					}
				}
			}

			sysoutStream.println(cnt + " words external RAM (" + (cnt / 256)
					+ " KB)");
			sysoutStream.println("download complete");
			sysoutStream.println("");
			sysoutStream.println("");

		} catch (IOException e) {
			sysoutStream.println(e);
		}
		return true;
	}

	static public void echo() {

		// start monitoring System.in in seperate thread
		new Thread() {
			public void run() {
				try {
					int rd = 0;
					while ((rd = System.in.read()) != -1) {
						outputStream.write(rd);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}.start();

		// same length as exitString as we will delete[] and append the char
		StringBuffer eb = new StringBuffer("123456789");

		while (true) {
			try {
				if (iStream.available() != 0) {
					char ch = (char) iStream.read();
					sysoutStream.print(ch);
					sysoutStream.flush();
					eb.append(ch);
					eb.deleteCharAt(0);
				}
				// test if the JOP JVM has exited
				if (eb.toString().equals(exitString)) {
					// sysoutStream.println(" JavaDown Exiting");
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// who closes the serial port now?
		// serialPort.close();
	}
}
