package testrt;
import util.*;
import com.jopdesign.sys.*;

public class B {

	private static Object syn;

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		syn = new Object();
		// ack int and schedule timer in 330 ms
		Native.wr(
 			Native.rd(Const.IO_US_CNT) + 330000,
			Const.IO_TIMER);
		// enable int
		Native.wr(1, Const.IO_INT_ENA);

		Thread th = new Thread() {
			public void run() {
				work('a');
			}
		};
		th.start();

		// th.setPriority(Thread.MAX_PRIORITY);

		work('0');
	}

	static void work(int ch) {

		for (;;) {

			synchronized (syn) {
				for (int i=0; i<5; ++i) {
					for (int j=0; j<386000/5; ++j) ;
					Dbg.wr(ch+i);
				}
			}
			for (int i=5; i<10; ++i) {
				for (int j=0; j<386000/5; ++j) ;
				Dbg.wr(ch+i);
			}
		}
	}

}
