
public class A {

	private static final int IO_STATUS = 1;
	private static final int IO_UART = 2;
	private static int[] buf;

	static void main(String[] args) {


		test(0, 0);
		test(0, 1);
		test(1, 0);
		test(3, 2);
		test(-3, 2);
		test(3, -2);
		test(-3, -2);
		test(10000, -3);
		test(10000, -287652987);
		test(123456789, -3);
		test(123456789, 123456789);
		test(-1, -1);
		test(0x7fffffff, 2);
		test(2, 0x7fffffff);

		test(0x80000000, 3);
		test(3, 0x80000000);

		test(0x80000000, 0x80000000);
		test(0x80000000, 0x7fffffff);
		test(0x7fffffff, 0x80000000);
		test(0x7fffffff, 0x7fffffff);

		test(0x80000001, 0x80000001);
		test(0x80000001, 0x7fffffff);
		test(0x7fffffff, 0x80000001);
		test(0x7fffffff, 0x7fffffff);


		for (;;);
	}

	static void test(int a, int b) {

		int i;
		int t, td;
		int t1, t2;

		t = JopSys.rd(JopSys.IO_CNT);
		t = JopSys.rd(JopSys.IO_CNT)-t;
		td = t;

		t = JopSys.rd(JopSys.IO_CNT);
		i = Util.mul(a, b);
		t = JopSys.rd(JopSys.IO_CNT)-t;
		t1 = t-td;
		wr(i);

		t = JopSys.rd(JopSys.IO_CNT);
		i = a*b;
		t = JopSys.rd(JopSys.IO_CNT)-t;
		t2 = t-td;
		wr(i);
		wr(t1);
		wr(t2);
		wrWait('\n');
	}


	static void wr(int i) {

		int cnt;

		if(i==-2147483648) {
			wrWait('-');
			wrWait('2');
			wrWait('1');
			wrWait('4');
			wrWait('7');
			wrWait('4');
			wrWait('8');
			wrWait('3');
			wrWait('6');
			wrWait('4');
			wrWait('8');
			wrWait(' ');
			return;
		}

		if (i<0) {
			wrWait('-');
			i = -i;
		}


		for (cnt=0;;) {
			buf[cnt++] = i%10;
			i /= 10;
			if (i==0) break;
		}
		for (int j=cnt-1; j>=0; --j) {
			wrWait((char) ('0'+buf[j]));
		}
		wrWait(' ');
	}

	/** only for missed in Timer */
	static void wrWait(char c) {

		while ((JopSys.rd(IO_STATUS)&1)==0) ;
		JopSys.wr(c, IO_UART);
	}
}
	
