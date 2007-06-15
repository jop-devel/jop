package jbe;

public class Execute {

	public static int perform(BenchMark bm) {

		int start, stop, cnt, time, overhead, minus;
		cnt = 512;		// run the benchmark loop 1024 times minimum
		time = 1;
		overhead = 0;
		minus = 0;

		//LowLevel.msg(bm.getName());
		while (time<1000) {
			cnt <<= 1;
			if (cnt < 0) {
				break;
			}
			start = LowLevel.timeMillis();
			bm.test(cnt);
			stop = LowLevel.timeMillis();
			time = stop-start;
			start = LowLevel.timeMillis();
			bm.overhead(cnt);
			stop = LowLevel.timeMillis();
			overhead = stop-start;
/*
			start = LowLevel.timeMillis();
			bm.overheadMinus(cnt);
			stop = LowLevel.timeMillis();
			minus = stop-start;
*/
		}

//		LowLevel.msg("time", time);
//		LowLevel.msg("ohd", overhead);
//		LowLevel.msg("ohdm", minus);
//		LowLevel.msg("cnt", cnt);
		time -= overhead;
		time += minus;

		if (time<25 || cnt<0) {
			LowLevel.msg(bm.getName());
			LowLevel.msg(" no result");
			LowLevel.lf();
			//return ;
			return 0;
		}

		// result is test() per second
		int result;
		if (cnt>2000000) {		// check for overflow on cnt*1000
			result = cnt/time;
			if (result>2000000) {
				LowLevel.msg(bm.getName());
				LowLevel.msg(" no result");
				LowLevel.lf();
				//return ;
				return 0;
			}
			result *= 1000;
		} else {
			result = cnt*1000/time;
		}
		//+++++++++++++LowLevel.msg(result);
		//+++++++++++++LowLevel.msg("1/s");
		
			return result;
				
		/*if (LowLevel.FREQ!=0) {
			int clocks = (LowLevel.FREQ*2000000/result+1)/2;
			if (LowLevel.FREQ>1000) {
				result /= 10;
				clocks = (LowLevel.FREQ*200000/result+1)/2;
			}
			LowLevel.msg(clocks);
			LowLevel.msg("clocks");
		}
		LowLevel.lf();*/
	}
}
