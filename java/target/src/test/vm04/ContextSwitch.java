package vm04;
import util.*;
import joprt.*;
import com.jopdesign.sys.*;

public class ContextSwitch {

	public final static int CNT = 500;

	static int[] result;
	static int ts;
	static int t_diff;

	public static void main(String[] args) {

		Dbg.initSerWait();				// use serial line for debug output
		result = new int[CNT];

		// low priority thread
		RtThread lprt = new RtThread(5, 100000) {

			public void run() {

				for (;;) {
					ts = Native.rd(Const.IO_CNT);
				}
			}
		};


		RtThread rt = new RtThread(10, 5000) {
			public void run() {

				int i;

				// give lprt a chance to start
				waitForNextPeriod();

				for (i=0; i<CNT; ++i) {
					waitForNextPeriod();
					result[i] = Native.rd(Const.IO_CNT)-ts;
				}
			
				result();
			}

			void result() {

				int max = 0;
				int min = 999999999;
				int i;

				for (i=0; i<CNT; ++i) {
					int val = result[i]-t_diff;
					if (val<min) min = val;
					if (val>max) max = val;
					Dbg.intVal(val);
					Dbg.wr('\n');
				}
				Dbg.intVal(min);
				Dbg.intVal(max);
				Dbg.wr('\n');

				for (;;) waitForNextPeriod();
			}
		};

		// measure time for measurement
		ts = Native.rd(Const.IO_CNT);
		ts = Native.rd(Const.IO_CNT)-ts;
		t_diff = ts;

		RtThread.startMission();

		for (;;) {
			;			// busy do nothing
		}
	}

}
