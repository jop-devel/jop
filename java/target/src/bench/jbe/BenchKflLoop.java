package jbe;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

public class BenchKflLoop {
	
	
	public static void main(String[] args) {
		
		BenchKfl bench = new BenchKfl();
		int ts, te, to;
		int i=0;
		
		SysDevice sys = IOFactory.getFactory().getSysDevice();
		
		ts = sys.cntInt;
		te = sys.cntInt;
		to = te-ts;
		ts = sys.cntInt;
		bench.test(1000);
		te = sys.cntInt;
		System.out.println();
		System.out.println(te-ts-to);

		
	}
	

}
