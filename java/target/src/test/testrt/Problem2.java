package testrt;
import util.*;
import com.jopdesign.sys.*;

public class Problem2 {

	private static Object syn;

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		syn = new Object();

		// test with pending int befor start
		// for sync in Thread code!
		Native.wr(Native.rd(Const.IO_US_CNT)+100, Const.IO_TIMER);
		for (int j=0; j<386000/5; ++j) ;
		Native.wr(1, Const.IO_INT_ENA);

		Thread th = new Thread() {
			public void run() {
				work('a');
			}
		};

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
