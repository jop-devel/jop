package wcet;

import lego.LineFollower;

import com.jopdesign.sys.*;

public class StartLineFollower {

	static int ts, te, to;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		LineFollower.init();
		// measurement + return takes 22+22+21=65 cycles
		// WCET measured: x
		// WCET analysed: y
		measure();
		System.out.println(te-ts-to);
	}
	
	static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		LineFollower.loop();
		te = Native.rdMem(Const.IO_CNT);		
	}
	
}
