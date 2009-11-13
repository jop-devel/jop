package taskset;

import joprt.RtThread;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class TaskLong extends RtThread {
	private int overhead;
	private int startTask;
	private int endTask;
	private int taskTime;

	TaskLong (int priority, int usPeriod, int msExecTime) {
		super(priority, usPeriod);
		taskTime = msExecTime;
	}

	public void run() {

		for (;;) {
			startTask = Native.rdMem(Const.IO_CNT);
			endTask = Native.rdMem(Const.IO_CNT);
			overhead = endTask - startTask;

			measure();

			System.out.println(endTask - startTask - overhead);
			waitForNextPeriod();
		}
	}

	private void measure() {
		// measured: 3750168
		// wcet: 3750189 (min)
		// difference is 21 cycles:
		//		return		21

		startTask=Native.rdMem(Const.IO_CNT);

		doWork(taskTime);

		endTask=Native.rdMem(Const.IO_CNT);
	}

	private void doWork(int timelimit) {
		int val,ms,i,j;

		for (ms=0;ms<timelimit;ms++) { //@WCA loop=75
			for (i=0;i<999;i++) { //@WCA loop=999
				val = i;
			}
			for (j=0;j<1166;j++) { //@WCA loop=1166
				val = j+1;
			}
		}
	}

}
