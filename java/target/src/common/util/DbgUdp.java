package util;

/**
*	buffered debug output for UDP debug.
*/

public class DbgUdp extends Dbg {

	private static final int BUF_LEN = 1024;
	private static final int BUF_MSK = 0x3ff;

	private int[] txBuf;
	private int rdptTx, wrptTx;

	DbgUdp() {
		txBuf = new int[BUF_LEN];		// should be byte
		rdptTx = wrptTx = 0;
	}

	void dbgWr(int c) {

		synchronized(txBuf) {
			if (((wrptTx+1)&BUF_MSK) == rdptTx) {
				return;									// buffer full => drop value
			}
			txBuf[wrptTx] = c;
			wrptTx = (wrptTx+1)&BUF_MSK;			
		}
	}

	/**
	*	read out buffer and write it in udp packet.
	*/
	int dbgReadBuffer(int[] udpBuf, int pos) {

		int i, j, k;

		synchronized(txBuf) {
			j = 0;
			k = pos;
			for (i=0; rdptTx!=wrptTx; ++i) {
				j <<= 8;
				j += txBuf[rdptTx];
				rdptTx = (rdptTx+1) & BUF_MSK;
				if ((i&3)==3) {
					udpBuf[k] = j;
					++k;
				}
			}
			int cnt = i & 3;
			if (cnt!=0) {
				for (; cnt<4; ++cnt) {
					j <<= 8;
				}
				udpBuf[k] = j;
			}
		}
		return i;
	};
}
