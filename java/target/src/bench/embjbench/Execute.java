package embjbench;

class Execute {

	static void perform(BenchMark bm) {

		int start, stop, cnt, time, overhead, minus;
		cnt = 1;
		time = 1;
		overhead = 0;
		minus = 0;

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
			start = LowLevel.timeMillis();
			bm.overheadMinus(cnt);
			stop = LowLevel.timeMillis();
		}

		LowLevel.msg("time", time);
		LowLevel.msg("overhead", overhead);
		LowLevel.msg("overhead minus", minus);
		time -= overhead;
		time += minus;

		if (time<25 || cnt<0) {
			LowLevel.msg(bm.getName());
			LowLevel.msg(" no result");
			LowLevel.msg("\n");
			return;
		}

		// result is test() per second
		int result;
		if (cnt>2000000) {		// check for overflow
			result = cnt/time*1000;
		} else {
			result = cnt*1000/time;
		}
		LowLevel.msg(bm.getName(), result);
		LowLevel.msg("\n");
	}
}
