package testurt;
import util.*;
import joprt.*;
import com.jopdesign.sys.Native;

public class RoundRobin extends Scheduler {

	/**
	*	test threads
	*/
	static class Work extends RtThread {
		int c;
		Work(int ch) {

			super(5, 100000);
			c = ch;
		}

		public void run() {

			for (;;) {
				Dbg.wr(c);
				// busy wait
				int ts = Native.rd(Native.IO_US_CNT) + 3000;
				while (ts-Native.rd(Native.IO_US_CNT)>0)
					;
			}
		}
	}


	//
	//	user scheduler starts here
	//
	public void RoundRobin() {
	}

	public void addThread(RtThread t) {}


	//
	//	called by JVM
	//
	public void schedule() {

		RtThread th = active.next;
		if (th==null) th = RtThread.head;
		dispatch(th, getNow()+10000);
	}


	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		new Work('a');
		new Work('b');
		new Work('c');

		RoundRobin rr = new RoundRobin();

		rr.start();

		// sleep
		for (;;) {
			Dbg.wr('M');
			Timer.wd();
			RtThread.sleepMs(1200);
		}
	}

}
