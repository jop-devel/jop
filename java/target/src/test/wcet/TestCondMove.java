package wcet;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class TestCondMove {

	static int ts, te, to;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Native.condMove() is disabled now
		
//		System.out.println(Native.condMove(1, 2, true));
//		System.out.println(Native.condMove(1, 2, false));
//		
//		ts = Native.rdMem(Const.IO_CNT);
//		te = Native.rdMem(Const.IO_CNT);
//		to = te-ts;
//
//		ts = Native.rdMem(Const.IO_CNT);
//		Native.condMove(1, 2, true);
//		te = Native.rdMem(Const.IO_CNT);
//		System.out.println(te-ts-to);
//
//		ts = Native.rdMem(Const.IO_CNT);
//		Native.condMove(1, 2, false);
//		te = Native.rdMem(Const.IO_CNT);
//		System.out.println(te-ts-to);
	}

}
