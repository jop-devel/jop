package util;

/**
*	serial output for debug on uart 1.
*/
import com.jopdesign.sys.Native;

public class DbgSerial extends Dbg {

	boolean waitHs;

	DbgSerial() {
		waitHs = false;
	}
	DbgSerial(boolean w) {
		waitHs = w;
	}

	void dbgWr(int c) {

		if (waitHs) {
			// busy wait, no sleep for thread tests!
			while ((Native.rd(Native.IO_STATUS)&Native.MSK_UA_TDRE)==0) ;
		}
/*
		// changed for OEBB
		if ((Native.rd(Native.IO_STATUS)&Native.MSK_UA_TDRE)==0) {
			Thread.yield();
			// try { Thread.sleep(10); } catch (Exception e) {}			// wait one character
		}
*/
/*
		if ((Native.rd(Native.IO_STATUS)&Native.MSK_UA_TDRE)==0) {
			return;
		}
*/
		Native.wr(c, Native.IO_UART);
	}


	/** makes only sense for tmpfered debug output (see DbgUdp) */
	int dbgReadBuffer(int[] udpBuf, int pos) {

		return 0;
	};
}
