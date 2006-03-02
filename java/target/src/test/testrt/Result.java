package testrt;
import joprt.RtThread;
import util.Dbg;

public class Result {

	static int last_per;
	static int max_jitter;

	public static void printPeriod(int ts_old, int ts) {

		/*- not available at the moment
		 *  
		int tim1 = RtThread.ts1-RtThread.ts0;
		int tim2 = RtThread.ts2-RtThread.ts1;
		int tim3 = RtThread.ts3-RtThread.ts2;
		int tim4 = RtThread.ts4-RtThread.ts3;
		int tim5 = ts-RtThread.ts4;
		int tim6 = ts-RtThread.ts0;
		int per = ts-ts_old;
		if (last_per==0) last_per = per;	// first time
		Dbg.intVal(tim1);
		Dbg.intVal(tim2);
		Dbg.intVal(tim3);
		Dbg.intVal(tim4);
		Dbg.intVal(tim5);
		Dbg.intVal(tim6);
		Dbg.intVal(per);
		tim1 = last_per-per;
		last_per = per;
		if (tim1<0) tim1 = -tim1;
		if (tim1>max_jitter) max_jitter = tim1;
		Dbg.intVal(max_jitter);
		
		Dbg.wr('\n');
		*/
	}

}
