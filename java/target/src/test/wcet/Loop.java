package wcet;

import com.jopdesign.sys.*;

public class Loop {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		loop(false, 123);
	}

	public static int loop(boolean b, int val) {
		
		int i, j;
//		int ts, te, to;
//		ts = Native.rdMem(Const.IO_CNT);
//		te = Native.rdMem(Const.IO_CNT);
//		to = te-ts;
//		System.out.println(to);
//		ts = Native.rdMem(Const.IO_CNT);
		for (i=0; i<10; ++i) {	//@WCA loop=10
			if (b) {
				for (j=0; j<3; ++j) {	//@WCA loop=3
					val *= val;
				}
//				return val;
			} else {
				for (j=0; j<7; ++j) {	//@WCA loop=7
					val += val;
//					return val;
				}
			}
		}
//		te = Native.rdMem(Const.IO_CNT);
//		System.out.println(te-ts-to);
		return val;
	}
}
