package oebb;

/**
*	Main.java: test main for OEBB.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*	Version:
*	0.15		init Fehler in UDP (=> UDP checksum error) korrigiert
*	0.16		TFTP retransmit on missing ACK in read
*	0.17		Zielmelderaum, 2 stop bit in uart.vhd (JOP)
*
*/

import util.*;
import ejip.*;
import joprt.*;

import com.jopdesign.sys.Native;

public class Main {

	// SW version
	public static final int VER_MAJ = 0;
	public static final int VER_MIN = 17;

	static LinkLayer ipLink;

	static boolean reset;

	public static void main(String[] args) {

		reset = false;

		Timer.init(20000000, 5);	// just for the watch dog or some usleep (where ?) in Amd.java!!!!
		Timer.wd();

		// Dbg.initSer();				// use serial line for debug output
		Dbg.initSerWait();				// use serial line for debug output
		Dbg.wr("RESET ");
		
		Keyboard.init(Native.IO_BG);
		Timer.wd();

		for (int i=0; i<4; ++i) {
			Keyboard.loop();
		}

		//
		//	Change to test modus (without return)
		//
		int val = Keyboard.rd();
		if (val==Keyboard.K1) {
			TestMode.doit();
		}

		// set DTR to 1
		Native.wr(8, Native.IO_BG+1);

		Timer.wd();
		RtThread.sleepMs(1000);
		Timer.wd();

		Flash.init();

Flash.check();

		//
		//	start TCP/IP and all (four) threads
		//
		Net.init();
		//
		//	start device driver threads
		//
		if (val==Keyboard.K2) {
			ipLink = Ppp.init(); 
		} else {
			ipLink = Slip.init((192<<24) + (168<<16) + (1<<8) + 2); 
		}


		//
		//	start GPS thread with priority
		//
		Gps.init(2);

		//
		//	start Communication thread
		//
		// TODO find a schedule whith correct priorities
		// Serial is 10
		// Ppp or Slip is 9
		// Serial2 (GPS) is 8
		// Net is 5
		Comm.init(Flash.getId(), 4, 100000, ipLink);

		//
		//	start Display and Keyboard thread.
		//
		new Display(3, 5000);

		//
		//	start Logic thread.
		//
		new Logic(1, 100000, ipLink);

		RtThread.startMission();

		//
		//	WD thread has lowest priority (as it is a non RT Thread).
		//	We can see if every timing will be met (or a reset happens...)
		//
		forever();
	}

	private static void forever() {

		//
		//	just do the WD blink with lowest priority
		//	=> if the other threads take to long (*3) there will be a reset
		//
/*
ipLink.startConnection(new StringBuffer("ATD*99***1#\r"), 
		new StringBuffer("AT+CGDCONT=1,\"IP\",\"A1.net\"\r"), 
		new StringBuffer("ppp@A1plus.at"), 
		new StringBuffer("ppp"));

ipLink.startConnection(new StringBuffer("ATD*99***1#\r"), 
		new StringBuffer("AT+CGDCONT=1,\"IP\",\"network\"\r"), 
		new StringBuffer("peter"), 
		new StringBuffer("paul"));
*/



		for (;;) {

			if (!reset) {
				Timer.wd();
} else {	// for test without WD

Object o = new Object();
synchronized(o) {
	for (;;);	// wait on WD!
}
			}

			for (int i=0; i<25; ++i) {
				RtThread.sleepMs(20);
			}
		}
	}
}
