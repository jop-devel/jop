package kfl;

/**
*	Rs232 - Rs485 router as stans alone program.
*/

public class EchoRs485 {

	public static final int IO_PORT = 0;

	private static final int IO_UART = 2;
	private static final int IO_RS485 = 15;
	private static final int IO_STATUS = 1;

	public static void main(String[] args) {

		Display.init();
		init();
		Timer.init();
		Timer.start();

		for (;;) {

			if ((JopSys.rd(IO_STATUS)&2)!=0) {
				JopSys.wr(JopSys.rd(IO_UART), IO_RS485);
				while ((JopSys.rd(IO_STATUS)&32)==0)
					;									// wait on RS485 Echo
				JopSys.rd(IO_RS485);					// consume Echo
			}
			if ((JopSys.rd(IO_STATUS)&32)!=0) JopSys.wr(JopSys.rd(IO_RS485), IO_UART);

			Timer.wd();

		}
	}

	public static void init() {

		int[] str =  {'K', 'F', 'L', ' ', 'P', 'C', '-', 'R', 'S', '4', '8', '5'};

		for (int i=0; i<str.length; ++i) {
			Display.data(str[i]);
		}
	}
}
