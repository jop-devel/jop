/*
	Test yield() 'performance' = threads switch time

	Results on Cyclone board:

		code length in words:	Cyclone		Acex
		run	loop
14.8.2003
		1	5	with serial out	: 431 us	473 us
		1	5	without output	: 394 us	433 us
		1	127	without output	: 506 us	546 us
		127	127	without output	: 597 us	636 us

	with enh bc load:
		1	5	without output	: 298 us	338 us
	with yield from int and monitor
		1	5	without output	: 367 us	xxx us
*/

package testrt;

import util.*;
import com.jopdesign.sys.*;


public class Yield {

	static final int MAX = 500;

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		new Thread() {
			public void run() {
/* for 127 words code
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
*/
				for (;;) {
					Dbg.wr('.');
					Thread.yield();
				}
			}
		}.start();

		int t = Native.rd(Native.IO_CNT);
		loop();
		t = Native.rd(Native.IO_CNT)-t;

		Dbg.intVal(t/20/(2*MAX));
		Dbg.wr('u');
		Dbg.wr('s');
		Dbg.wr('\n');

/*
		Dbg.intVal(Thread.max1);
		Dbg.intVal(Thread.max2);
		Dbg.wr('\n');
		Dbg.intVal(Thread.max1/20);
		Dbg.intVal(Thread.max2/20);
		Dbg.wr('\n');
*/

		// stop
		for (;;);
	}

	static void loop() {
/* for 127 words code
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
*/
		for (int i=0;i<MAX;++i) {
			Dbg.wr('*');
			Thread.yield();
		}
	}
}
