package oebb;

/**
*	TestMode.java: test modus for OEBB.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

import util.*;
import joprt.*;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class TestMode {

/**
*	test main.
*/

	public static void doit() {

		//
		//	start Display and Keyboard thread.
		//
		new Display(1, 5000);

		RtThread.startMission();

		Display.clear();

		flushSerial();
		serVal = 0;
		outVal = 0;

		Display.write(0, "BG263 Pruefmodus");
		Display.write(20, "Software V ");
		Display.intVal(31, Main.VER_MAJ);
		Display.write(32, ".");
		Display.intVal(33, Main.VER_MIN);
		Display.write(40, "ID: ");
		Display.intVal(44, Flash.getId());

		for (int i=0; i<20; ++i) {
			Timer.wd();
			RtThread.sleepMs(100);
		}
		Display.clear();

		forever();
	}

	// value for relais, dtr
	private static int outVal;

	private static void setOutputs() {

		if (outVal==0) {
			outVal = 2;		// relais a
		} else if (outVal==2) {
			outVal = 4;		// relais b
		} else if (outVal==4) {
			outVal = 8;		// modem dtr
		} else {
			outVal = 0;		// all off
		}
	}

	// test keyboard
	private static void testKeyboard() {


		int val;

		val = Keyboard.rd();
		if (val != -1) {
			if (val==Keyboard.UP) {
				Display.write(40, 'A');
				Display.write(41, 'U');
				Display.write(42, 'F');
			} else if (val==Keyboard.DOWN) {
				Display.write(40, 'A');
				Display.write(41, 'B');
				Display.write(42, ' ');
			} else if (val==Keyboard.E) {
				Display.write(40, 'E');
				Display.write(41, ' ');
				Display.write(42, ' ');
			} else if (val==Keyboard.B) {
				Display.write(40, 'B');
				Display.write(41, ' ');
				Display.write(42, ' ');
			} else if (val==Keyboard.C) {
				Display.write(40, 'C');
				Display.write(41, ' ');
				Display.write(42, ' ');
			} else if (val==Keyboard.BLACK) {
				Display.write(40, 'S');
				Display.write(41, ' ');
				Display.write(42, ' ');
			} else {
				val = Keyboard.num(val);
				if (val!=-1) {
					Display.write(40, ' ');
					Display.write(41, ' ');
					Display.write(42, ' ');
					Display.intVal(40, val);
				}
			}
		}
	}

	// read all characters from serial buffer
	private static void flushSerial() {

		int i;
		for (i=0; i<10; ++i) {
			RtThread.sleepMs(100);
			Timer.wd();
			while ((Native.rd(Const.IO_STATUS) & Const.MSK_UA_RDRF)!=0) {
				Native.rd(Const.IO_UART);
			}
			while ((Native.rd(Const.IO_STATUS2) & Const.MSK_UA_RDRF)!=0) {
				Native.rd(Const.IO_UART2);
			}
			while ((Native.rd(Const.IO_STATUS3) & Const.MSK_UA_RDRF)!=0) {
				Native.rd(Const.IO_UART3);
			}
		}
	}

	private static int serVal;

	private static void serialSend() {

		++serVal;
		if (serVal==10) serVal = 0;

		if ((Native.rd(Const.IO_STATUS) & Const.MSK_UA_TDRE)!=0) {
			Native.wr('0'+serVal, Const.IO_UART);
		}
		if ((Native.rd(Const.IO_STATUS2) & Const.MSK_UA_TDRE)!=0) {
			Native.wr('a'+serVal, Const.IO_UART2);
		}
		if ((Native.rd(Const.IO_STATUS3) & Const.MSK_UA_TDRE)!=0) {
			Native.wr('A'+serVal, Const.IO_UART3);
		}
	}

	private static void serialRcv() {

		int val;

		if ((Native.rd(Const.IO_STATUS) & Const.MSK_UA_RDRF)!=0) {
			val = Native.rd(Const.IO_UART);
			if (val != '0'+serVal) {
				Display.write(0, "Serv.: Falsches Z.");
				while ((Native.rd(Const.IO_STATUS) & Const.MSK_UA_RDRF)!=0) {
					Native.rd(Const.IO_UART);
				}
			} else {
				Display.write(0, "Ser.: OK          ");
			}
		} else {
			Display.write(0, "Serv.: Kein Zeich.");
		}

		if ((Native.rd(Const.IO_STATUS2) & Const.MSK_UA_RDRF)!=0) {
			val = Native.rd(Const.IO_UART2);
			if (val != 'a'+serVal) {
				Display.write(20, "Modem: Falsches Z.");
				while ((Native.rd(Const.IO_STATUS2) & Const.MSK_UA_RDRF)!=0) {
					Native.rd(Const.IO_UART2);
				}
			} else {
				Display.write(20, "Modem: OK         ");
			}
		} else {
			Display.write(20, "Modem: Kein Zeich.");
		}

		if ((Native.rd(Const.IO_STATUS3) & Const.MSK_UA_RDRF)!=0) {
			val = Native.rd(Const.IO_UART3);
			if (val != 'A'+serVal) {
				Display.write(43, "GPS: Falsches Z.");
				while ((Native.rd(Const.IO_STATUS3) & Const.MSK_UA_RDRF)!=0) {
					Native.rd(Const.IO_UART3);
				}
			} else {
				Display.write(43, "GPS: OK         ");
			}
		} else {
			Display.write(43, "GPS: Kein Zeich.");
		}
	}

	private static void forever() {

		int val;

		//
		//	just do the WD blink with lowest priority
		//	=> if the other threads take to long (*3) there will be a reset
		//
		for (;;) {
			Timer.wd();
			setOutputs();

			// 1s loop
			for (int i=0; i<50; ++i) {

				serialSend();

				RtThread.sleepMs(20);

				serialRcv();

				testKeyboard();

				val = Native.rd(Const.IO_BG+1);	// check input pin
				Native.wr(val | (outVal ^ 8), Const.IO_BG+1);	// and set led with value, invert DTR
			}
		}
	}
}
