package wcet.devel;
/* Automated Test Procedure:
 * 
 * The compute method takes roughly 10K cycles
 *
 * $test$> make java_app wcet P1=test P2=wcet/devel P3=DfaLoopBound1 WCET_METHOD=compute1
 * $grep$> wcet: (cost: ^ 9897 $ )
 *
 * All tests take at most 14 * 10K cycles to execute (with y = 3)
 *
 * $test$> make jsim P1=test P2=wcet/devel P3=DfaLoopBound1
 * $grep$> wcet[DfaLoopBound1]: ^ 140095
 *
 * Test 0 takes 9 * 10K
 *
 * $test$> make wcet P1=test P2=wcet/devel P3=DfaLoopBound1 WCET_METHOD=test0 WCET_DFA=yes
 * $grep$> wcet: (cost: ^ 90054 $ , execution
 *
 * Test 1 takes 5 * 10K
 *
 * $test$> make wcet P1=test P2=wcet/devel P3=DfaLoopBound1 WCET_METHOD=test1 WCET_DFA=yes
 * $grep$> wcet: (cost: ^ 50117 $ , execution
 *
 */

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/** Simple tests for DFA loop bounds */

public class DfaLoopBound1 {
	static volatile int x,y;
	/* most simple DFA test (9 * 10K) */
	private static void test0() {
		int u = 11;
		for(int i = 0; i+1 < u-1; ++i) {
			compute1();
		}
	}
	/* basic DFA test (5 * 10K) */
	private static void test1() {
		int u,l;
		if((y&1) == 0) l=2;
		else         l=3;
		if(((y>>1) & 1) == 0) u=5;
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
	
	static int ts, te, to;

	public static void main(String[] args) {
		if (Config.MEASURE) {
			ts = Native.rdMem(Const.IO_CNT);
			te = Native.rdMem(Const.IO_CNT);
			to = te-ts;
		}
		y=2;
		x=1;
		invoke();
		if (Config.MEASURE) {
			int dt = te-ts-to;
			System.out.print("wcet[DfaLoopBound1]:");
            System.out.println(dt);
        }
	}
	
	static void invoke() {
		measure();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	static void measure() {
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		test0();
		test1();
	}


}
