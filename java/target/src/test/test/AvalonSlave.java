package test;

import com.jopdesign.sys.*;

public class AvalonSlave {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int AVALON_TEST_SLAVE_0_BASE = 0x00800000/4;
		
		System.out.println("Avalon slave test");
		System.out.println("cnt="+Native.rd(AVALON_TEST_SLAVE_0_BASE));
		System.out.println("xyz="+Native.rd(AVALON_TEST_SLAVE_0_BASE+1));
		Native.wr(1234, AVALON_TEST_SLAVE_0_BASE+1);
		System.out.println("cnt="+Native.rd(AVALON_TEST_SLAVE_0_BASE));
		System.out.println("xyz="+Native.rd(AVALON_TEST_SLAVE_0_BASE+1));
	}

}
