package wcet.devel;
/* Automated Test Procedure:
 * 
 * The compute method takes roughly 10K cycles
 *
 * $test$> make java_app wcet P1=test P2=wcet/devel P3=AnnotLang1 WCET_METHOD=compute1
 * $grep$> wcet: (cost: ^ 9974 $ )
 *
 * All tests take roughly 144 * 10K cycles to execute
 *
 * $test$> make jsim P1=test P2=wcet/devel P3=AnnotLang1
 * $grep$> wcet[AnnotLang1]: ^ 1431236
 *
 * Test constant expressions takes ~ 24 * 10K
 *
 * $test$> make wcet P1=test P2=wcet/devel P3=AnnotLang1 WCET_METHOD=testConstantExpressions
 * $grep$> wcet: (cost: ^ 242904 $ , execution
 *
 * Test markers takes ~ 120 * 10K
 *
 * $test$> make wcet P1=test P2=wcet/devel P3=AnnotLang1 WCET_METHOD=testMarkers
 * $grep$> wcet: (cost: ^ 1214886 $ , execution
 *
 */

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * Testing new annotation language features (1)
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class AnnotLang1 {
	int x;
	
	/* test constant expressions (24 10K) */
	private void testConstantExpressions() {
		for(int i = 0; i < 8; ++i) // @WCA loop <= 2+2*3
			compute1();
		for(int i = 0; i < 8; ++i) // @WCA loop <= (2+2)*2
			compute1();
		for(int i = 0; i < 8; ++i) // @WCA loop <= ((3-1)*10-4)/2
			compute1();
	}

	/* test markers.
	 * without markers: (~512).
	 * with marker outer: (~288).
	 * with marker outer[2]: (~120) */
	private void testMarkers() {		
		for(int i = 0; i < 8; ++i) { // @WCA loop <= 8
			for(int j = i; j < 8; ++j) { // @WCA loop <= 4*9 outer
				for(int k = j; k < 8; ++k) { // @WCA loop <= 8*15 outer(2)
					compute1();
				}				
			}
		}
	}

	/* should have roughly 10K cycles to simplify the evaluation */
	void compute1()  { 
		for(int j=0;j<1;++j)    // @WCA loop=1
		for(int i= 7;i<167;++i) // @WCA loop=160
			x = (x+1) * i;
	}

	static int ts, te, to;
	private static AnnotLang1 test;

	public static void main(String[] args) {
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		test = new AnnotLang1();
		invoke();
		if (Config.MEASURE) {
			int dt = te-ts-to;
			System.out.print("wcet[AnnotLang1]:");
            System.out.println(dt);
        }
	}
	
	static void invoke() {
		measure();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	static void measure() {
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		test.testConstantExpressions();
		test.testMarkers();
	}

}
