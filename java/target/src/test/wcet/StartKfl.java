package wcet;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import wcet.kflapp.Mast;

public class StartKfl {

	static int ts, te, to;


	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// initialization
		Mast.main(null);

		int min = 0x7fffffff;
		int max = 0;
		int val = 0;
		for (int i=0; i<100; ++i) { // @WCA loop=100
			measure();
			val = te-ts-to;
			if (val<min) min = val;
			if (val>max) max = val;
		}
		System.out.println(min);
		System.out.println(max);
	}
	static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		Mast.loop();
		te = Native.rdMem(Const.IO_CNT);		
	}
			
}
