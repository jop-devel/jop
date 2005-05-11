package cache;

import jbe.BenchKfl;
import jbe.BenchUdpIp;
import jbe.kfl.Mast;

public class CallKfl {

	public static void main(String[] args) {

		BenchKfl bk = new BenchKfl();
		BenchUdpIp bu = new BenchUdpIp();

		for (;;) {
			bk.test(4);
			bu.test(4);
			com.jopdesign.sys.Native.rd(1234);	// trigger cache statistics in simulation

		}
/*
		Mast.main(null);

		forever();
*/
	}

	static void forever() {

		for (;;) {
			Mast.loop();
			com.jopdesign.sys.Native.rd(1234);	// trigger cache statistics in simulation
		}
	}

	static void dummy() {

		for (int i=0; i<100000; ++i) Mast.loop();
//		jbe.DoAll.main(null);
	}
			
}
