package util;

import com.jopdesign.sys.Native;
import joprt.*;

/**
*	Buffered serial line in it's own thread with maximum priority.
*/

public class Serial extends RtThread {


	private static final int BUF_LEN = 128;
	private static final int BUF_MSK = 0x7f;

	private static final int PERIOD = 3000;

/**
*	Send buffer for serial line.
*/
	private static int[] txBuf;
/**
*	Receive buffer for serial line.
*/
	private static int[] rxBuf;

	private static int rdptTx, wrptTx;
	private static int rdptRx, wrptRx;

	private static Object monitor;

	private static Serial ser;

	Serial(int priority, int us) {
		super(priority, us);
	}

	public static void init() {

		if (ser!=null) return;

		txBuf = new int[BUF_LEN];		// should be byte
		rxBuf = new int[BUF_LEN];
		rdptTx = wrptTx = 0;
		rdptRx = wrptRx = 0;
		monitor = new Object();

		//
		// minimum 1 ms, but performance degrades below 2/3 ms
		//
		ser = new Serial(10, PERIOD);
	}

	public void run() {

		for (;;) {
			waitForNextPeriod();
			loop();
		}
	}

/**
*	read and write loop.
*	call it! (polling)
*/
	// public static void loop() {
	private static void loop() {

		int i, j;

		synchronized (monitor) {
//
//	read serial data
//
			i = wrptRx;
			j = rdptRx;
			while ((Native.rd(Native.IO_STATUS2) & Native.MSK_UA_RDRF)!=0 && ((i+1)&BUF_MSK)!=j) {
				rxBuf[i] = Native.rd(Native.IO_UART2);
				i = (i+1)&BUF_MSK;
			}
			wrptRx = i;
//
//	write serial data
//
			i = rdptTx;
			j = wrptTx;
			while ((Native.rd(Native.IO_STATUS2) & Native.MSK_UA_TDRE)!=0 && i!=j) {
				Native.wr(txBuf[i], Native.IO_UART2);
				i = (i+1) & BUF_MSK;
			}
			rdptTx = i;
		}
	}

	public static int rxCnt() {

		int ret;

		synchronized (monitor) {
			ret = (wrptRx-rdptRx) & BUF_MSK;
		}
		return ret;
	}

	public static int txFreeCnt() {

		int ret;

		synchronized (monitor) {
			ret = (rdptTx-1-wrptTx) & BUF_MSK;
		}
		return ret;
	}

	public static int rd() {

		int i, ch;
		synchronized (monitor) {
			i = rdptRx;
			rdptRx = (i+1) & BUF_MSK;
			ch = rxBuf[i];
		}
		return ch;
	}

	public static void wr(int c) {

		synchronized (monitor) {
			int i = wrptTx;
			wrptTx = (i+1) & BUF_MSK;
			txBuf[i] = c;
		}
	}
}
