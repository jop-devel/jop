// Put this in the make file
// P2=wcet
// P3=StartDSVM
// WCET_METHOD=main
package wcet;

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import util.Dbg;
import wcet.dsvmfp.*;

public class StartDSVM {

	static int ts, te, to;

	public static void main(String[] args) {
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		Dbg.initSer();
		TestSMO.init();
		invoke();
		if (Config.MEASURE) System.out.println(te-ts-to);

		// TestSMO.report();
	}

	static void invoke() {
		measure();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	static void measure() {
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		TestSMO.deployRT();
	}

}
