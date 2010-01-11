package taskset;

import joprt.RtThread;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.GC;

public class TaskSet {

	public static TaskShort taskShort;
	public static TaskMedium taskMedium;
	public static TaskLong taskLong;
	public static int startTime;

	public static void main(String[] args) {
		int i;
		taskShort = new TaskShort(3, 100000, 25); //25
		taskMedium = new TaskMedium(2, 200000, 100); //100
		taskLong = new TaskLong(1, 400000, 75); //75

		//Countdown to delay start of "interesting" execution by 10 seconds
		for(i=10;i>0;i--){
			System.out.print("Tminus");
			System.out.println(i);
			doWork(1000);
		}
		System.out.println();
		startTime = Native.rdMem(Const.IO_CNT);
		RtThread.startMission();
	}

	private static void doWork(int timelimit) {
		int val,ms,i,j;

		for (ms=0;ms<timelimit;ms++) { //@WCA loop=1000
			for (i=0;i<999;i++) { //@WCA loop=999
				val = i;
			}
			for (j=0;j<1166;j++) { //@WCA loop=1166
				val = j+1;
			}
		}

	}
}