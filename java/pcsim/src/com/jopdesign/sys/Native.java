
package com.jopdesign.sys;

/**
*	functions in JOP JVM simulated on PC!
*/

import java.io.*;
import javax.comm.*;

public class Native {
//
// Reihenfolge so lassen!!!
// ergibt static funktionsnummern:
//		1 rd
//		2 wr
//		...
//
	public static final int IO_CNT = 0;
	public static final int IO_INT_ENA = 0;
	public static final int IO_US_CNT = 1;
	public static final int IO_TIMER = 1;
	public static final int IO_SWINT = 2;
	public static final int IO_WD = 3;

	public static final int IO_STATUS = 4;
	public static final int IO_UART = 5;

	public static final int MSK_UA_TDRE = 1;
	public static final int MSK_UA_RDRF = 2;

// BG263
	public static final int IO_STATUS2 = 6;
	public static final int IO_UART2 = 7;
	public static final int IO_STATUS3 = 8;
	public static final int IO_UART3 = 9;

	public static final int IO_DISP = 10;
	public static final int IO_BG = 12;

// TAL
	public static final int IO_IN = 10;
	public static final int IO_LED = 10;
	public static final int IO_OUT = 11;

	public static final int IO_ADC1 = 12;
	public static final int IO_ADC2 = 13;

	public static final int IO_CTRL = 14;
	public static final int IO_DATA = 15;

/**
*	JOP clock ticks per ms
*/
	private static int TICKS = 20000;		// 20 MHz
	private static long startTime;

	public static int rd(int addr) {

		int i;

		try{

			switch (addr) {
				case IO_STATUS:
				case IO_STATUS2:
					i = 0;
					if (is.available()!=0) i |= MSK_UA_RDRF;	// rdrf
					i |= MSK_UA_TDRE;							// tdre is alwais true on OutputStream
					return i;
				case IO_UART:
// System.out.println("Native: read() '"+(char) i+"'");
					break;
				case IO_UART2:
					i =  is.read();
					return i;
				case IO_CNT:
					return (int) ((System.currentTimeMillis()-startTime)*TICKS);
				case IO_US_CNT:
					return (int) ((System.currentTimeMillis()-startTime)*1000);
				default:
//					System.out.println("Native: address "+addr+" not implemented");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return 0x80;	// INIT_DONE for CS8900
	}

	public static void wr(int val, int addr) {

		try{

			switch (addr) {
				case IO_UART:
					System.out.print((char) val);				// debug serial
// System.out.println("Native: write() "+(char) val);
					break;
				case IO_UART2:
					os.write(val);
					break;
				case IO_WD:
					// ignore
System.out.print(val==0 ? "o" : "*");
					break;
				default:
//					System.out.println("Native: address "+addr+" not implemented");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static final int HTML_START = 0x80000;	// start at first address (should be changed!)

	private static String html = "<html><head>" +
								"<meta http-equiv=\"Content-Type\" content=\"text/html\">" + 
								"<title>test</title>" +
								"</head>" +
								"<body>" +
								"<h2>Test Page</h2>" +
								"<p><sub>!ht</sub>" +
								"</body>" +
								"</html>";
	/**
	*	Simulation of JOP memory.
	*	return HTML normal stored in Flash.
	*/
	public static int rdMem(int addr) {

		int i = addr-HTML_START;

		if (i<0 || i>=html.length()) {
			return 0;
		} else {
			return html.charAt(i);
		}
	}

	public static void wrMem(int val, int addr) { return; }
	public static int rdIntMem(int addr) { return 1; }
	public static void wrIntMem(int val, int addr) { return; }
	public static int getSP() { return 1; }
	public static void setSP(int val) { return; }
	public static int getVP() { return 1; }
	public static void setVP(int val) { return; }

	private static CommPortIdentifier portId;
	private static InputStream is;
	private static OutputStream os;
	private static SerialPort serialPort;

/**
*	open serial port.
*/
	static {
		startTime = System.currentTimeMillis();

		try {
			portId = CommPortIdentifier.getPortIdentifier("COM2");
			serialPort = (SerialPort) portId.open("Test", 2000);
			is = serialPort.getInputStream();

	   		// serialPort.addEventListener(this);
			// serialPort.notifyOnDataAvailable(true);

			os = serialPort.getOutputStream();
			serialPort.setSerialPortParams(115200,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT
										| SerialPort.FLOWCONTROL_RTSCTS_IN);

		} catch (Exception e) {
System.out.println("problem with serial port");
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}
}
