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
*	0.18		TFTP: accept a retransmit of last data block on a WRQ
*	0.23		Version for OEBB Demo
*	0.42		Beep bei jedem Anzeige(=Status)wechsel, Eingabe der
*				Streckennummer wenn nicht eindeutig (1km Abstand)
*	0.90		Fahrerlaubnis bleibt bei falscher Richtung,
*				Richtungscheck toleranter.
*	0.91		ES mode - first version
*	0.92		ES mode default
*
*/

import util.*;
import ejip.*;
import joprt.*;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Main {

	// SW version
	public static final int VER_MAJ = 0;
	public static final int VER_MIN = 92;

	// TODO find a schedule whith correct priorities
	// Serial is 10
	// Ppp or Slip is 9
	// Serial2 (GPS) is 8
	// Net is 5
	private static final int LOGIC_PRIO = 1;
	private static final int LOGIC_PERIOD = 100000;
	private static final int GPS_PRIO = 2;
	private static final int GPS_PERIOD = 100000;
	private static final int DISPLAY_PRIO = 3;
	private static final int DISPLAY_PERIOD = 5000;
	private static final int COMM_PRIO = 4;
	private static final int COMM_PERIOD = 100000;
	private static final int GPSSER_PRIO = 8;
	private static final int GPSSER_PERIOD = 12000;
	private static final int NET_PRIO = 5;
	private static final int NET_PERIOD = 10000;
	private static final int IPLINK_PRIO = 9;
	private static final int IPLINK_PERIOD = 10000;
	private static final int IPSER_PRIO = 10;
	private static final int IPSER_PERIOD = 3000;

	static Net net;
	static LinkLayer ipLink;
	static Serial ser, ser2;
	static RtThread pppThre;


	static boolean reset;

	public static void main(String[] args) {

		reset = false;

		Timer.wd();

		// ncts is set to '0' in bgio.vhd, so we can 'wait' with open line
		Dbg.initSerWait();				// use serial line for debug output
		Dbg.wr("RESET ");
		
		Keyboard.init(Const.IO_BG);
		Timer.wd();

		for (int i=0; i<4; ++i) {
			RtThread.sleepMs(5);
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
		Native.wr(8, Const.IO_BG+1);

		Timer.wd();
		RtThread.sleepMs(1000);
		Timer.wd();

		Flash.init();


		Flash.check();

		Status.isMaster = Flash.isMaster();
		
		//
		//	start TCP/IP and all (four) threads
		//
		net = Net.init();
		// remove default TFTP handler
		Udp.removeHandler(BgTftp.PORT);
		// BUT this handler can only handle 64KB sector
		// writes. A new FPGA configuration has to be
		// split to more writes!!!
		Udp.addHandler(BgTftp.PORT, new BgTftp());
		ser = new Serial(Const.IO_UART_BG_MODEM_BASE);

		//
		//	Create serial, PPP/SLIP and TCP/IP threads
		//
		new RtThread(IPSER_PRIO, IPSER_PERIOD) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ser.loop();
				}
			}
		};

		pppThre = new RtThread(IPLINK_PRIO, IPLINK_PERIOD) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ipLink.loop();
				}
			}
		};

		new RtThread(NET_PRIO, NET_PERIOD) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					net.loop();
				}
			}
		};

		if (val==Keyboard.K3) {
			// SLIP for simpler tests
			ipLink = Slip.init(ser,	(192<<24) + (168<<16) + (1<<8) + 2); 
		} else if (val==Keyboard.K2){
			// use second SLIP subnet for 'COs test'
			ipLink = Slip.init(ser, (192<<24) + (168<<16) + (2<<8) + 2); 
		} else {
			ipLink = Ppp.init(ser, pppThre); 
//			System.out.println("SLIP is default!!");
//			ipLink = Slip.init(ser,	(192<<24) + (168<<16) + (1<<8) + 2); 
		}

		//
		//	create GPS serial and GPS thread
		//
//
//		for GPS use: 4800 baud => 2.0833 ms per character
//		send fifo: 4, receive fifo: 8
//			16 ms should be ok, 12 ms for shure
//
		ser2 = new Serial(Const.IO_UART_BG_GPS_BASE);
		new RtThread(GPSSER_PRIO, GPSSER_PERIOD) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ser2.loop();
				}
			}
		};

		Gps.init(GPS_PRIO, GPS_PERIOD, ser2);

		//
		//	create Communication thread
		//
		Comm.init(Flash.getId(), COMM_PRIO, COMM_PERIOD, ipLink);

		//
		//	create Display and Keyboard thread.
		//
		new Display(DISPLAY_PRIO, DISPLAY_PERIOD);

		//
		//	create Logic thread.
		//
		new Logic(LOGIC_PRIO, LOGIC_PERIOD, ipLink);

		//
		//	start all threads
		//
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
				Timer.loop();	// for the second timer
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
