package testrt;

import util.*;
import com.jopdesign.sys.Native;

/**
*	test thread switch performance.
*	5 threads, loop 200 => 1000 thread switches.
*
*	on ACX board (with serial output):
*	20 MHz, ram_cnt = 3		: 0.6422 s
*	20 MHz, ram_cnt = 2		: 0.6030 s
*	with mem wait instr.	: 0.5185 s
*	without serial out		: 0.4765 s
*
*	on Cyc board:
*	with serial out			: 0.4635 s
*	without serial out		: 0.4233 s
*
*	with enhanced bc load:
*	ACX without serial out	: 0.381 s
*	Cyc without serial out	: 0.328 s
*	yield with int and monitor 
*	Cyc without serial out	: 0.387 s
*/

public class PerfYield5 extends Thread {

	private int c;

	public void run() {

		for (;;) {
			Dbg.wr(c);
			yield();
		}

		// should not reach the end
	}

	PerfYield5(int ch) {
		c = ch;
	}

	private static void loop() {

		for (int i=0; i<200; ++i) {
			Dbg.wr('m');
			yield();
		}
	}

	public static void main(String[] args) {

		Dbg.initSer();

		PerfYield5 t1 = new PerfYield5('a');
		PerfYield5 t2 = new PerfYield5('b');
		PerfYield5 t3 = new PerfYield5('c');
		PerfYield5 t4 = new PerfYield5('d');

		int start, end;

		t1.start();
		t2.start();
		t3.start();
		t4.start();

		start = Native.rd(Native.IO_CNT);

		loop();

		end = Native.rd(Native.IO_CNT);

		end -= start;
		Dbg.wr('\n');
		Dbg.intVal(end);
		Dbg.wr('\n');
		int ms = end / 20000000;
		end %= 20000000;
		int us = end / 2000;
		Dbg.intVal(ms);
		Dbg.intVal(us);

/*
		Dbg.wr('\n');
		Dbg.intVal(Thread.max1);
		Dbg.intVal(Thread.max2);
		Dbg.wr('\n');
		Dbg.intVal(Thread.max1/20);
		Dbg.intVal(Thread.max2/20);
*/
		for (;;) ;
	}
}
