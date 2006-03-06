package testurt;
import jopurt.*;
import util.Dbg;
import util.Timer;

public class RoundRobin extends Scheduler {

	/**
	*	test threads
	*/
	static class Work extends RtUserThread {
		int c;
		Work(int ch) {

			super(5, 100000);
			c = ch;
		}

		public void run() {

			for (;;) {
				Dbg.wr(c);
				// busy wait to simulate
				// 3 ms workload in Work.
				int ts = Scheduler.getNow();
				ts += 3000;
				while (ts-Scheduler.getNow()>0)
					;
			}
		}
	}


	//
	//	user scheduler starts here
	//
	public void RoundRobin() {
	}

	public void addTask(Task t) {
		// we do not allow tasks
		// to be added after start()
	}


	//
	//	called by JVM
	//
	public void schedule() {

		RtUserThread th = active.next;
		if (th==null) th = RtUserThread.head;
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
			RtUserThread.sleepMs(1200);
		}
	}

}
