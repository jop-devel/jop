package oebb;

/**
*	Test program for new message format.
*
*	Author: Martin Schoeberl (martin@jopdesign.com)
*
*/

import util.*;
import ejip.*;
import joprt.*;

import com.jopdesign.sys.Const;
//import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

public class TestMain {

	// SW version
	public static final int VER_MAJ = 2;
	public static final int VER_MIN = 0;

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
	private static final int COMM_PRIO = 6;
	private static final int COMM_PERIOD = 100000;
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
	static Serial ser, ser2;
	static RtThread pppThre;


	static boolean reset;

	public static void main(String[] args) {

		reset = false;

		Timer.wd();

		// ncts is set to '0' in bgio.vhd, so we can 'wait' with open line
		Dbg.initSerWait();				// use serial line for debug output
		Timer.wd();


		// set DTR to 1
		Native.wr(8, Const.IO_BG+1);

		Timer.wd();
		RtThread.sleepMs(1000);
		Timer.wd();

		// we need the BgTftp befor Flash.init()!
//		BgTftp tftpHandler = new BgTftp();
/* comment Flash for JopSim debug
*/
//		Flash.init();


		Timer.wd();

		
		//
		//	start TCP/IP and all (four) threads
		//
		net = Net.init();
		// remove default TFTP handler
		Udp.removeHandler(BgTftp.PORT);
		// BUT this handler can only handle 64KB sector
		// writes. A new FPGA configuration has to be
		// split to more writes!!!
//		Udp.addHandler(BgTftp.PORT, tftpHandler);
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

		// SLIP for simpler tests
		ipLink = Slip.init(ser,	(192<<24) + (168<<16) + (1<<8) + 2); 

		new RtThread(1, 100000) {
			public void run() {
				State msg = new State(ipLink);
				msg.destIp = (192 << 24) + (168 << 16) + (0 << 8) + 5;
				msg.bgid = 0x1234;
				msg.strnr = 155;
				msg.zugnr = 4711;
				msg.setPos(43);
				msg.type = State.TYPE_ZUG;

				Udp.addHandler(State.ZLB_RCV_PORT, msg);
				for (;;) {
					msg.run();
					waitForNextPeriod();
				}
			}
		};
		
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
							for (;;);	// wait on WD!
						}
					}
				}
			}
		};

//System.out.println("startMission");
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
//				System.out.print(t2);
//				System.out.print(idle);
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
