package vm04;
import joprt.RtThread;
import joprt.SwEvent;
import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Event {

	public final static int CNT = 500;
	static int[] result;
	static SwEvent sev;

	static int f_tim;

	public static void main(String[] args) {

		Dbg.initSerWait();				// use serial line for debug output
		result = new int[CNT];


		sev = new SwEvent(11, 10000) {

			public void handle() {
				f_tim = Native.rd(Const.IO_CNT);
			}
		};

		RtThread rt = new RtThread(10, 10000) {
			public void run() {

				int i, ts;

				for (i=0; i<CNT; ++i) {
					waitForNextPeriod();
					ts = Native.rd(Const.IO_CNT);
					sev.fire();
					result[i] = f_tim-ts;
				}
			
				result();
			}

			void result() {

				int max = 0;
				int min = 999999999;
				int i;

				for (i=0; i<CNT; ++i) {
					int diff = result[i];
					if (diff<min) min = diff;
					if (diff>max) max = diff;
					Dbg.intVal(diff);
					Dbg.wr('\n');
				}
				Dbg.intVal(min);
				Dbg.intVal(max);
				Dbg.wr('\n');

				for (;;) waitForNextPeriod();
			}
		};


		RtThread.startMission();

		for (;;) {
			;			// busy do nothing
		}
	}

}
