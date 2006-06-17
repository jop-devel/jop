package wcet;

import joprt.RtThread;
import wcet.lift.LiftControl;
import wcet.lift.TalIo;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;


public class StartLift {

	private static LiftControl ctrl;
	private static TalIo io;


	static int ts, te, to;


	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// to avoid stack overflow in Control()
		new RtThread(10,10);
		// initialization
		ctrl = new LiftControl();
		io = new TalIo();

		int min = 0x7fffffff;
		int max = 0;
		int val = 0;
		for (int i=0; i<100; ++i) { // @WCA loop=100
			measure();
			val = te-ts-to;
			if (val<min) min = val;
			if (val>max) max = val;
		}
//		System.out.println(min);
//		System.out.println(max);
	}
	static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		loop();
		te = Native.rdMem(Const.IO_CNT);		
	}
	
	static void loop() {
		ctrl.setVals();
		ctrl.getVals();
		ctrl.loop(io);
	}

}
