package vm04;
import util.*;
import joprt.*;
import com.jopdesign.sys.Native;

public class Periodic {

	public final static int CNT = 10;
	static int[] result;

	public static void main(String[] args) {

		Dbg.initSerWait();				// use serial line for debug output

		result = new int[CNT];

		RtThread rt = new RtThread(10, 300) {
			public void run() {

				int ts, ts_old;
				int i;

				waitForNextPeriod();
				ts_old = Native.rd(Native.IO_US_CNT);

				for (i=0; i<CNT; ++i) {
					waitForNextPeriod();
					ts = Native.rd(Native.IO_US_CNT);
					result[i] = ts-ts_old;
					ts_old = ts;
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
// Dbg.intVal(123456);
			;			// busy do nothing
		}
	}

}
