package wcet;

import jbe.BenchUdpIp;
import jbe.ejip.Packet;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class StartBenchUdpIp {

	static int ts, te, to;

	static BenchUdpIp bui;

	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// initialization
		bui = new BenchUdpIp();

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
		// bui.loop();
		Packet.getPacket(null, 0, 0);
		te = Native.rdMem(Const.IO_CNT);		
	}
			
}
