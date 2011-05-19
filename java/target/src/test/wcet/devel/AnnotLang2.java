package wcet.devel;
/* Automated Test Procedure:
 * 
 * The compute method takes roughly 10K cycles
 *
 * $test$> make java_app wcet P1=test P2=wcet/devel P3=AnnotLang2 WCET_METHOD=compute1
 * $grep$> wcet: (cost: ^ 9974 $ )
 *
 * All tests take roughly 24 * 10K cycles to execute
 *
 * $test$> make jsim P1=test P2=wcet/devel P3=AnnotLang2 
 * $grep$> wcet[AnnotLang2]: ^ 238470
 *
 * Test testConstantReference takes ~ 24 * 10K
 *
 * $test$> make wcet P1=test P2=wcet/devel P3=AnnotLang2 WCET_OPTIONS="--use-dfa no"
 * $grep$> wcet: (cost: ^ 242359 $ , execution
 *
 */

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * Testing new annotation language features (2)
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class AnnotLang2 {
	public static final int L1 = 6;
	public static final int L2 = 8;
	public static class Inner {
		public static class Constants {
			public static final int L3 = 10;			
		}
	}
	int x;
	
	/* test constant expressions (24 10K) */
	private void testConstantReference() {
		for(int j=0;j<L1;++j) // @WCA loop = L1
			compute1();
		for(int j=0;j<L2;++j) // @WCA loop = wcet.devel.AnnotLang2.L2
			compute1();
		for(int j=0;j<Inner.Constants.L3;++j) // @W C A loop = wcet.devel.AnnotLang2$Inner.L3
			// @WCA loop = 10
			compute1();
	}


	/* should have roughly 10K cycles to simplify the evaluation */
	void compute1()  { 
		for(int j=0;j<1;++j)    // @WCA loop=1
		for(int i= 7;i<167;++i) // @WCA loop=160
			x = (x+1) * i;
	}

	static int ts, te, to;
	private static AnnotLang2 test;

	public static void main(String[] args) {
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		test = new AnnotLang2();
		invoke();
		if (Config.MEASURE) {
			int dt = te-ts-to;
			System.out.print("wcet[AnnotLang2]:");
            System.out.println(dt);
        }
	}
	
	static void invoke() {
		measure();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	static void measure() {
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		test.testConstantReference();
	}
}
