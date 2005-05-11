/**
*	Write Rs485 Data with timestamp to serial line.
*/

public class DbgRs485 {

	public static final int IO_PORT = 0;

	private static final int IO_UART = 2;
	private static final int IO_RS485 = 15;
	private static final int IO_STATUS = 1;

	public static void main(String[] args) {

		Timer.init();
		Timer.start();

		int val, ts;

		for (;;) {

			if ((JopSys.rd(IO_STATUS)&32)!=0) {
				val = JopSys.rd(IO_RS485);
				wr(val);							// write value
				ts = JopSys.rd(JopSys.IO_CNT);
				for (int i=0; i<4; ++i) {			// write timestamp
					wr(ts);							// low byte first
					ts >>= 8;
				}

			}
			Timer.wd();
		}
	}

	static void wr(int c) {
		while ((JopSys.rd(IO_STATUS)&1)==0) ;
		JopSys.wr(c, IO_UART);
	}
}
