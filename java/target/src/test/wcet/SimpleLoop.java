package wcet;

import com.jopdesign.sys.*;

public class SimpleLoop {

	static int ts, te, to;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// measurement + return takes 22+22+21=65 cycles
		// WCET measured: 2312
		// WCET analysed: 2377
		measure();
		System.out.println(te-ts-to);
	}
	
	static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		int val = 123;
		for (int i=0; i<100; ++i) { // @WCA loop=100
			val += i;
		}
		te = Native.rdMem(Const.IO_CNT);		
	}
	
}
