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
		/*    String driverName = "com.sun.comm.Win32Driver";
		 try {
		 CommDriver commdriver = (CommDriver) Class.forName(driverName)
		 .newInstance();
		 commdriver.initialize();
		 } catch (Exception e2) {
		 e2.printStackTrace();
		 }
		 */
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
			outputStream = serialPort.getOutputStream();
			iStream = serialPort.getInputStream();
		} catch (IOException e) {
			sysoutStream.println(e);
		}
		try {
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			sysoutStream.println(e);
		}

		downLoad(fname);

		if (echo) {
			echo();
		}

		serialPort.close();

	}

	static public boolean downLoad(String fname) {
		sysoutStream.println("TEST");
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
			byte adword[] = new byte[4];
			int rplyCnt = 0;
			int cnt = 0;
			for (; in.nextToken() != StreamTokenizer.TT_EOF; ++cnt) {
				// in.nval contains the next 32 bit word to be sent
				int l = (int) in.nval;

				// Java code length at index 1 position in .jop
				if (cnt == 1) {
					sysoutStream.println(l + " words of Java bytecode ("
							+ (l / 256) + " KB)");
				}
				for (int i = 0; i < 4; i++) {
					byte b = (byte) (l >> ((3 - i) * 8));
					++rplyCnt;
					outputStream.write(b);

					if (!usb) {
						if (cnt == i) {
							// TODO check reply
							iStream.read();
							--rplyCnt;
						} else if (iStream.available() != 0) {
							iStream.read();
							--rplyCnt;
						}
					}
				}

				if ((cnt & 0x3f) == 0) {
					sysoutStream.print(prog_char[(cnt >> 6) & 0x07] + "\r");
				}

			}
			while (rplyCnt > 0) {
				iStream.read();
				--rplyCnt;
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

	public static void setOutputStream(OutputStream outputStream) {
		JavaDown.outputStream = outputStream;
	}

	public static void setIStream(InputStream stream) {
		JavaDown.iStream = stream;
	}

	public static void setSysoutStream(PrintStream sysoutStream) {
		JavaDown.sysoutStream = sysoutStream;
	}

	public static void setSysinStream(InputStream sysinStream) {
		JavaDown.sysinStream = sysinStream;
	}
}
