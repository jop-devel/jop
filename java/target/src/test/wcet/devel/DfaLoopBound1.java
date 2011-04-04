package wcet.devel;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/** Simple tests for DFA loop bounds */

public class DfaLoopBound1 {
	static volatile int x;
	/* most simple DFA test (7 * 10K) */
	private static void test0() {
		int u = 9;
		for(int i = 0; i+1 < u-1; ++i) {
			compute1();
		}
	}
	/* basic DFA test (5 * 10K) */
	private static void test1() {
		int u,l;
		if((x&1) == 0) l=2;
		else         l=3;
		if(((x>>1) & 1) == 0) u=5;
		else                  u=7;
		for(int i = l; i < u; ++i) {
			compute1();
		}
	}

	/* should have roughly 10K cycles to simplify the evaluation */
	static void compute1()  { 
		for(int j=0;j<1;++j)    // @WCA loop=1
		for(int i= 7;i<200;++i) // @WCA loop=193
			x = (x+1) * i;
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
		test0();
		test1();
	}


}
