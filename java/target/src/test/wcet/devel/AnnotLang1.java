package wcet.devel;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class AnnotLang1 {
	int x;
	
	/* test constant expressions (24 10K) */
	private void testConstantExpressions() {
		for(int i = 0; i < 8; ++i) // @WCA loop <= 2+2*3
			compute1();
		for(int i = 0; i < 8; ++i) // @WCA loop <= (2+2)*2
			compute1();
		for(int i = 0; i < 8; ++i) // @WCA loop <= (3-1)*5-2
			compute1();
	}

	/* test markers.
	 * without markers: (~512 10K).
	 * with marker outer: (~288 10K).
	 * with marker outer[2]: (120 10K) */
	private void testMarkers() {		
		for(int i = 0; i < 8; ++i) { // @WCA loop <= 8
			for(int j = i; j < 8; ++j) { // @WCA loop <= 4*9 outer
				for(int k = j; j < 8; ++k) { // @WCA loop <= 8*15 outer(2)
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

	/* testing wcet */
	/** Set to false for the WCET analysis, true for measurement */
	final static boolean MEASURE = true;
	static int ts, te, to;
	private static AnnotLang1 test;

	public static void main(String[] args) {
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		test = new AnnotLang1();
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
		test.testConstantExpressions();
		test.testMarkers();
	}

}
