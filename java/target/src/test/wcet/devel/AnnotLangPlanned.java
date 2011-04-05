package wcet.devel;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/** Planned features of the annotation language
 * 
 * @author the guys hacking on jop
 *
 */
public class AnnotLangPlanned {
	volatile int x;
	int z = 3;

	/* references to DFA results from arguments */
	void testArgReference(int a, int b) {
		for(int j=a;j<b;++j) // @WCA loop = $arg1 - $arg0
			compute1();
		for(int j=a;j<z;j+=2) // @WCA loop = ($this.z - $arg0) / 2
			compute1();		
	}

	/* references to system parameters provided by the analyzer*/
	void testSysReference() {
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te - ts;
		for(int j=0;j<to;++j) // @WCA loop <= @timer.overhead
			compute1();
	}

	/* absolute loop bound in cycles or real time*/
	void testAbsBound() {
		ts = Native.rdMem(Const.IO_CNT);		
		long START_DELAY = 50000;
		te = ts;
		while((te - ts) < START_DELAY) { // @WCA loop cycles <= @lego.startDelay + @timer.overhead
			te = Native.rdMem(Const.IO_CNT);
		}
		while(! isUART_Ready()) ; // @WCA loop time <= @uart.max_delay ms
	}
	
	static boolean isUART_Ready() {
		return true;
	}

	/* should have roughly 10K cycles to simplify the evaluation */
	void compute1()  { 
		for(int j=0;j<1;++j)    // @WCA loop=1
		for(int i= 7;i<167;++i) // @WCA loop=160
			x = (x+1) * i;
	}
	static int ts,te,to; /* measurement overhead */
}
