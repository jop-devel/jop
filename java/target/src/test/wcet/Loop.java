package wcet;

import com.jopdesign.sys.*;

public class Loop {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		measure(true, 123);
	}

	public static int measure(boolean b, int val) {
		
		// measured: 2009
		// wcet: 2033
		// difference is 24 cycles:
		//		iload_1		1
		//		ireturn		23
		
		int i, j;
		
//		int ts, te, to;
//		ts = Native.rdMem(Const.IO_CNT);
//		te = Native.rdMem(Const.IO_CNT);
//		to = te-ts;
//		ts = Native.rdMem(Const.IO_CNT);
		
		
		for (i=0; i<10; ++i) {	//@WCA loop=10
			if (b) {
				for (j=0; j<3; ++j) {	//@WCA loop=3
					val *= val;
				}
			} else {
				for (j=0; j<7; ++j) {	//@WCA loop=7
					val += val;
				}
			}
		}
		

//		te = Native.rdMem(Const.IO_CNT);
//		System.out.println(te-ts-to);
		
		return val;
	}
}
