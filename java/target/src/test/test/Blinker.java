package test;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
//
//	Clock.java
//

public class Blinker {

	public static void main( String s[] ) {

		Native.wr(0, Const.IO_WD);		// make WD happy
		Native.wr(1, Const.IO_WD);
		Native.wr(0, Const.IO_WD);
		time();
	}

	static void time() {

		int next;
		int h, m, s, ms;


		h = m = s = ms = 0;
		next = 0;
		s = -1;

		for (;;) {

			++ms;
			if (ms==1000) {
				ms = 0;
				++s;
				if (s==60) {
					s = 0;
					++m;
				}
				if (m==60) {
					m = 0;
					++h;
				}
				if (h==24) h = 0;

				Native.wr(s & 1, Const.IO_WD);
			}

			Native.wr(~s & 1, Const.IO_WD);
			Native.wr(s & 1, Const.IO_WD);

			next = waitForNextInterval(next);
		}
	}

	static int waitForNextInterval(int next) {

		final int INTERVAL = 20000;		// one ms

		if (next==0) {
			next = Native.rd(Const.IO_CNT)+INTERVAL;
		} else {
			next += INTERVAL;
		}

		while (next-Native.rd(Const.IO_CNT) >= 0)
				;

		return next;
	}


}
