//
//	Prime.java
//
//	prime performace test with uart
//

public class Prime {

	public static final int IO_PORT = 0;
	public static final int IO_STATUS = 1;
	public static final int IO_UART = 2;
	public static final int IO_ECP = 3;
	public static final int IO_CNT = 10;
	public static final int IO_MS = 11;

	public static void main( String s[] ) {

		int i, j, k, t1, t2;

		t1 = JopSys.rd(IO_MS);
		t2 = JopSys.rd(IO_MS)-t1;

		t1 = JopSys.rd(IO_MS);
		for (i=3; i<2000; ++i) {
			for (j=2; j<i; ++j) {
				for (k=i; k>0; ) {
					k -= j;
				}
				if (k==0) break;
			}
			k = i;
			if (j==i) {
				for (j=0;i>999;++j) i-= 1000; while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr(j+'0', IO_UART);
				for (j=0;i>99;++j) i-= 100; while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr(j+'0', IO_UART);
				for (j=0;i>9;++j) i-= 10; while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr(j+'0', IO_UART);
				while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr(i+'0', IO_UART);
				while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr(' ', IO_UART);
			}
			i = k;
		}
		t1 = JopSys.rd(IO_MS)-t1-t2;
		i = t1;
		while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr('\r', IO_UART);
		while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr('\n', IO_UART);
		for (j=0;i>9999;++j) i-= 10000; while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr(j+'0', IO_UART);
		for (j=0;i>999;++j) i-= 1000; while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr(j+'0', IO_UART);
		while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr('.', IO_UART);
		for (j=0;i>99;++j) i-= 100; while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr(j+'0', IO_UART);
		for (j=0;i>9;++j) i-= 10; while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr(j+'0', IO_UART);
		while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr(i+'0', IO_UART);

		while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr('\r', IO_UART);
		while ((JopSys.rd(IO_STATUS)&1)==0); JopSys.wr('\n', IO_UART);

		for (;;) ;	// stop program
	}

}
