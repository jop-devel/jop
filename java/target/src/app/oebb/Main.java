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
*	0.93		Verlassensmeldung von Status.direction abhaengig, Fehler bei
*				Ziel im 6-er behoben.
*				Fehlermeldung bei fehlenden ES Eintraegen in der Streckendatei
*				im ES mode.
*				Haltepunktauswahl: Naechster mögliche Haltepunkt von der
*				aktuellen Position aus (in Richtung 'rechts').
*	0.94		ES mode logbook, Print log auf Serviceschnittstelle bei Start
*				mit Taste 3 gedrueckt (SLIP). 
*
*	0.97-0.99	Interne Testversionen fuer Download mit Javaprog. > 64KB
*
*	0.95		Reset Fehler bei mehreren Strecken im ES-mode behoben.
*				Diese Version ist nach 0.97, aber noch mit JOP pre2005
*				version.
*
	
	0.97 Java Program ist noch <64KB, aber neuer JOP, Java Program braucht
      aber schon den neuen JOP zum Laufen. Unterscheidung der JOP versionen
      beim Start auf der Serviceschnittstelle:
        'JOP start V pre2005' - alte Version
        'JOP start V 20050728' - neue Version

	0.98		Verwende Text 1 und 2, wenn 3 und 4 nicht gesetzt sind.
	
	0.98c		Automatische ES/ZLB Erkennung
	
	0.99		Diverse gröbere Änderungen laut ÖBB Bestellung
	
	1.00		Änderungen laut ÖBB Bestellung, Version für Wieselburger
				Testbetrieb

*/

import util.*;
import ejip.*;
import joprt.*;

import com.jopdesign.sys.Const;
//import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

public class Main {

	// SW version
	public static final int VER_MAJ = 2;
	public static final int VER_MIN = 36;

	private static final int LOG_PRIO = 1;
	private static final int LOG_PERIOD = 1000000;
	private static final int STRECKE_PRIO = 1;
	private static final int STRECKE_PERIOD = 100000;
	private static final int LOGIC_PRIO = 2;
	private static final int LOGIC_PERIOD = 100000;
	private static final int GPS_PRIO = 3;
	private static final int GPS_PERIOD = 100000;
	private static final int DISPLAY_PRIO = 4;
	private static final int DISPLAY_PERIOD = 5000;
	private static final int WD_PRIO = 5;
	private static final int WD_PERIOD = 25000;
	private static final int STATE_PRIO = 6;
	private static final int STATE_PERIOD = 100000;
	private static final int NET_PRIO = 7;
	private static final int NET_PERIOD = 10000;
	private static final int GPSSER_PRIO = 8;
	private static final int GPSSER_PERIOD = 12000;
	private static final int IPLINK_PRIO = 9;
	private static final int IPLINK_PERIOD = 10000;
	private static final int IPSER_PRIO = 10;
	private static final int IPSER_PERIOD = 3000;

	static Net net;
	static LinkLayer ipLink;
	static BgTftp tftpHandler;
	static Serial ser, ser2;
	static RtThread pppThre;
	
	static State state;
	static Logic logic;
	static Logging logger;
	static SingleFileFS fs;


	static boolean reset;

	public static void main(String[] args) {

		reset = false;

		Timer.wd();

		// ncts is set to '0' in bgio.vhd, so we can 'wait' with open line
		Dbg.initSerWait();				// use serial line for debug output
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

		//
		//	start TCP/IP
		//
		Ejip ejip = new Ejip();
		net = new Net(ejip);

		// we need the BgTftp befor Flash.init()!
		fs = new SingleFileFS();
		if (fs.isAvailable()) {
			tftpHandler = new NandTftp(ejip, fs);
		} else {
			System.out.println("No NAND Flash");
			tftpHandler = new BgTftp(ejip);			
		}
/* comment Flash for JopSim debug
*/
		Flash.init();


		Flash.check(val==Keyboard.K3);
		Timer.wd();

		Status.isMaster = Flash.isMaster();
		

		
		// remove default TFTP handler
		net.getUdp().removeHandler(BgTftp.PORT);
		// Add special BG TFTP handler
		net.getUdp().addHandler(BgTftp.PORT, tftpHandler);
		ser = new Serial(Const.IO_UART_BG_MODEM_BASE);
		
		// Handler for DGPS data
		net.getUdp().addHandler(DgpsHandler.PORT, new DgpsHandler(ejip));

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
					ipLink.run();
				}
			}
		};

		new RtThread(NET_PRIO, NET_PERIOD) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					net.run();
				}
			}
		};

		// create logging thread
		logger = new Logging(ejip, net);
		new RtThread(LOG_PRIO, LOG_PERIOD) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					logger.run();
				}
			}
		};
		

		if (val==Keyboard.K3) {
			// SLIP for simpler tests
			ipLink = new Slip(ejip, ser, Ejip.makeIp(192, 168, 1, 2));
		} else if (val==Keyboard.K2){
			// use second SLIP subnet for 'COs test'
			ipLink = new Slip(ejip, ser, Ejip.makeIp(192, 168, 2, 2));
		} else {
			ipLink = new Ppp(ejip, ser, pppThre);
//			System.out.println("SLIP is default!!");
//			ipLink = new Slip(ejip, ser, Ejip.makeIp(192, 168, 1, 2));

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
		// The SW handle fired by the GPS thread
		// to find a Strecke
		new Strecke(STRECKE_PRIO, STRECKE_PERIOD);

		//
		//	Crate state object and the periodic thread.
		state = new State(ejip, net, ipLink);
		state.bgid = Flash.getId();
		state.versionStrecke = Flash.getVer();
		net.getUdp().addHandler(State.ZLB_RCV_PORT, state);
		
		new RtThread(STATE_PRIO, STATE_PERIOD) {
			public void run() {
				for (;;) {
					state.run();
					waitForNextPeriod();
				}
			}
		};

		//
		//	create Display and Keyboard thread.
		//
		new Display(DISPLAY_PRIO, DISPLAY_PERIOD);

		//
		//	create Logic thread.
		//
		logic = new Logic(LOGIC_PRIO, LOGIC_PERIOD, ipLink);
		
		//
		//	watch dog in it's own thread
		new RtThread(WD_PRIO, WD_PERIOD) {
			public void run() {
				
				for (;;) {
					for (int i=0; i<20; ++i) {
						waitForNextPeriod();
					}
					if (!reset) {
						Timer.wd();
						Timer.loop();	// for the second timer
					} else {
						// for test without WD disable ints
						Object o = new Object();
						synchronized(o) {
							for (;;) {
								;	// wait on WD!
							}
						}
					}
				}
			}
		};

		//
		//	start all threads
		//

		RtThread.startMission();

		//
		//	utilization thread has lowest priority (as it is a non RT Thread).
		//	We can see if every timing will be met
		//
		forever();
	}

	final static int MIN_US = 10;

	private static void forever() {

		//
		//	Nothing to do in main thread.
		//	We could measure CPU utilization here.
		//

		int t1, t2, t3;
		int idle, timeout;
		
		idle = 0;	
		t1 = Native.rd(Const.IO_US_CNT);
		timeout = t1;

		for (;;) {
			t2 = Native.rd(Const.IO_US_CNT);
			t3 = t2-t1;
			t1 = t2;
			if (t3<MIN_US) {
				idle += t3;
			}
			if (t2-timeout>1000000) {
				t2 -= timeout;
				idle *= 100;
				idle /= t2;
				idle = 100-idle;
//				System.out.print("CPU utilization [%]: ");
//				System.out.print(idle);
//				System.out.println();
//				System.out.print("free memory: ");
//				System.out.println(com.jopdesign.sys.GC.freeMemory());
				idle = 0;	
				t1 = Native.rd(Const.IO_US_CNT);
				timeout = t1;
			}
		}
	}
}
