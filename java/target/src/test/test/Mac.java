package test;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Mac {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		mac(2, 1);
		mac(2, 2);
		mac(3, 3);
		System.out.println("result "+result());
		
		mac(5, 5);
		mac(5, 5);
		System.out.println("result "+result());
	}

	static void mac(int a, int b) {
		
		Native.wrMem((a<<16)+b, Const.IO_MAC);
	}
	
	static int result() {
		
		return Native.rdMem(Const.IO_MAC);
	}
}
