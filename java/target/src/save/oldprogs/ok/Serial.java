/**
*	Buffered serial line.
*/

public class Serial {

	private static final int IO_STATUS = 1;
	private static final int IO_UART = 2;

	private static int[] constTab;
//
//	buffer for serial out
//
	private static int[] buf;

	static int rdpt, wrpt;

	public static void init() {

		buf = new int[32];
		rdpt = wrpt = 0;

		constTab = new int[6];		// <clinit> is missing!
		constTab[0] = 1000000;
		constTab[1] = 100000;
		constTab[2] = 10000;
		constTab[3] = 1000;
		constTab[4] = 100;
		constTab[5] = 10;
	}

	static void loop() {

		for (int i=0; rdpt!=wrpt && i<4; ++i) {		// max. 4 tries (fill uart buffer)

			if ((JopSys.rd(IO_STATUS)&1) == 0) {
				break;					// tdr not empty
			}

			JopSys.wr(buf[rdpt], IO_UART);
			rdpt = (rdpt+1)&0x1f;
		}
	}

	static void wr(char c) {

		if (((wrpt+1)&0x1f) == rdpt) {
			return;
		}
		buf[wrpt] = c;
		wrpt = (wrpt+1)&0x1f;
	}

	static void wr(int i) {

		if (i<0) {
			wr('-');
			i = -i;
		}

		boolean print = false;
		int j, k;

		for (j=0; j<constTab.length; ++j) {
			int val = constTab[j];
			for (k=0; i>=val; ++k) i-= val;
			if (k!=0 || print) {
				wr((char) (k+'0'));
				print = true;
			}
		}
		wr((char) (i+'0'));
	}

	/** only for missed in Timer */
	static void wrWait(char c) {

		while ((JopSys.rd(IO_STATUS)&1)==0) ;
		JopSys.wr(c, IO_UART);
	}
}
