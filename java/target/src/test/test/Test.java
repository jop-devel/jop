package test;

import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
*	Test.java ... the name implys it
*/

public class Test {

	public static void main(String[] args) {

		int i, j, k;

		Dbg.initSer();

		int t1 = Native.rd(Const.IO_CNT);
		int t2 = Native.rd(Const.IO_CNT);
		int diff = t2-t1;

		Dbg.intVal(diff);

		i = 3; j = 7;
		t1 = Native.rd(Const.IO_CNT);
		k = i << j;
		t2 = Native.rd(Const.IO_CNT);
		t2 = t2-t1-diff;
		Dbg.intVal(t2);

		Dbg.wr('\n');

		shf(1, 0);
		shf(1, 1);
		shf(1, 16);
		shf(1, 31);
		shf(1, 32);
		shf(1, 33);
		Dbg.wr('\n');

		shf(17, 0);
		shf(17, 1);
		shf(17, 16);
		shf(17, 31);
		shf(17, 32);
		shf(17, 33);
		Dbg.wr('\n');

		shf(-1, 0);
		shf(-1, 1);
		shf(-1, 16);
		shf(-1, 31);
		shf(-1, 32);
		shf(-1, 33);
		Dbg.wr('\n');

		shf(0x80000000, 0);
		shf(0x80000000, 1);
		shf(0x80000000, 16);
		shf(0x80000000, 31);
		shf(0x80000000, 32);
		shf(0x80000000, 33);
		Dbg.wr('\n');

		testmul();

		for (;;) ;
	}

	public static void testmul() {

		mul(1, 2);
		mul(-3, 2);
		mul(3, -2);
		mul(-3, -2);
		mul(0xffffffff, 0xffffffff);
		mul(3, 0xffffffff);
		mul(0xffffffff, 47);
		mul(0x80000000, 3);
		mul(3, 0x80000000);
		mul(0x80000001, 3);
		mul(3, 0x80000001);
		mul(1, 0);
		mul(0, 2);
		mul(111, 1234);
		mul(-10000, 1234);
	}

	public static void shf(int i, int j) {

		Dbg.intVal(i);
		Dbg.intVal(j);
		Dbg.intVal(i<<j);
		Dbg.intVal(i>>j);
		Dbg.intVal(i>>>j);
		Dbg.wr('\n');
	}

	public static void mul(int i, int j) {

		Dbg.intVal(i*j);
		Dbg.intVal(Util.mul(i, j));
		Dbg.wr('\n');
	}

}
