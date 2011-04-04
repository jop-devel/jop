package wcet.devel;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/** Simple tests for DFA loop bounds */

public class DfaLoopBound1 {
	static volatile int x;
	private static void test1() {
		int u,l;
		/* basic DFA test */
		if((x&1) == 0) l=2;
		else         l=3;
		if(((x>>1) & 1) == 0) u=5;
		else                  u=7;
		for(int i = l; i < u; ++i) {
			x = x * 31;
		}
	}

	/* testing wcet */
	/** Set to false for the WCET analysis, true for measurement */
	final static boolean MEASURE = true;
	static int ts, te, to;

	public static void main(String[] args) {
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		x=3;
		invoke();
		if (MEASURE) {
			int dt = te-ts-to;
			System.out.print("wcet[test1]:");
            System.out.println(dt);
        }
	}
	
	static void invoke() {
		measure();
		if (MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	static void measure() {
		if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
		test1();
	}


}
