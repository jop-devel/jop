
package tcpip;

/**
*	Main.java: test main.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

import util.*;

import com.jopdesign.sys.Native;

public class Main {

	public static final int IO_INOUT = 0;
	public static final int IO_LED = 4;
/**
*	test main.
*/
	public static void main(String[] args) {

		Timer.init(20000000, 5);	// just for the watch dog or some usleep (where ?)
		Dbg.init();

		//
		//	start TCP/IP and all (four) threads
		//
		Net.init();

		//
		//	WD thread has lowest priority to see if every timing will be met
		//
/* oder doch nicht !
		Thread th = Thread.currentThread();
		th.setPriority(Thread.MIN_PRIORITY);
*/
/* read ID  and status from NAND
*/
Dbg.wr('\n');
// Native.wrMem(0xff, 0x100001);
Native.wrMem(0x90, 0x100001);
Native.wrMem(0x00, 0x100002);
//
//	should read 0x98 and 0x73
//
Dbg.hexVal(Native.rdMem(0x100000));
Dbg.hexVal(Native.rdMem(0x100000));

//
//	read status, should be 0xc0
//
Native.wrMem(0x70, 0x100001);
Dbg.hexVal(Native.rdMem(0x100000));
Dbg.hexVal(Native.rdMem(0x100000));

/* Read ID from Flash
Native.wrMem(0xaa, 0x80555);
Native.wrMem(0x55, 0x802aa);
Native.wrMem(0x90, 0x80555);
Dbg.hexVal(Native.rdMem(0x80000));
Native.wrMem(0xaa, 0x80555);
Native.wrMem(0x55, 0x802aa);
Native.wrMem(0x90, 0x80555);
Dbg.hexVal(Native.rdMem(0x80001));
*/

Dbg.wr('\n');
int ch;
ch = Native.rdMem(0x80000);
Dbg.wr(ch);
ch = Native.rdMem(0x80002);
Dbg.wr(ch);
ch = Native.rdMem(0x80001);
Dbg.wr(ch);
ch = Native.rdMem(0x80000);
Dbg.wr(ch);
ch = Native.rdMem(0x80001);
Dbg.wr(ch);
ch = Native.rdMem(0x80002);
Dbg.wr(ch);

Dbg.wr('\n');

for (int i=0; i<4; ++i) {
ch = Native.rdMem(0x00000+i);
Dbg.intVal(ch);
}
		forever();
	}

	private static void forever() {

		//
		//	just do the WD blink with lowest priority
		//	=> if the other threads take to long (*3) there will be a reset
		//
		for (;;) {
			for (int i=0; i<10; ++i) {
				try {
					Thread.sleep(50);
				} catch (Exception e) {}
				int val = Native.rd(IO_INOUT);
				Native.wr(val, IO_LED);
			}
			Timer.wd();
		}
	}
}
