
public class Min {

	public static final int IO_PORT = 0;
	public static final int IO_STATUS = 1;
	public static final int IO_UART = 2;
	public static final int IO_CNT = 10;

	public static void main(String s[]) {


		while ((JopSys.rd(IO_STATUS)&1)==0) ;
		JopSys.wr('A', IO_UART);
		while ((JopSys.rd(IO_STATUS)&1)==0) ;
		JopSys.wr('B', IO_UART);
		JopSys.wr(31, IO_PORT);

		int blink = 0;

		for (;;) {
			int next = JopSys.rd(JopSys.IO_CNT);
			next += 6000000;
			while (next-JopSys.rd(JopSys.IO_CNT) >= 0)
				;
			JopSys.wr(blink, IO_PORT);
			if (blink==0) {
				blink = 31;
			} else {
				blink = 0;
			}
		}
	}

}
