package test;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Mac {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int i;
		mac(2000, 1000);
		for (i=0; i<4; ++i) ; // wait a little bit
		mac(2000, 2000);
		for (i=0; i<4; ++i) ; // wait a little bit
		mac(3000, 3000);
		for (i=0; i<4; ++i) ; // wait a little bit
		System.out.println("result "+Native.rdMem(Const.IO_MAC_A)+
				" "+Native.rdMem(Const.IO_MAC_B));
		
		mac(-5, 5);
		for (i=0; i<4; ++i) ; // wait a little bit
		mac(5, -5);
		for (i=0; i<4; ++i) ; // wait a little bit
		System.out.println("result "+Native.rdMem(Const.IO_MAC_A)+
				" "+Native.rdMem(Const.IO_MAC_B));
	}

	static void mac(int a, int b) {
		
		Native.wrMem(a, Const.IO_MAC_A);
		Native.wrMem(b, Const.IO_MAC_B);
	}
	
}
