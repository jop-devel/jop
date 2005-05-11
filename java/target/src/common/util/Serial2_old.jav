package util;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import joprt.*;

/**
*	Buffered serial line in it's own thread with maximum priority.
*	Just a copy of Serial for second port (GPS on OEBB project).
*/

public class Serial2_old extends RtThread {

	private static final int BUF_LEN = 128;
	private static final int BUF_MSK = 0x7f;

//
//	for GPS use: 4800 baud => 2.0833 ms per character
//	send fifo: 4, receive fifo: 8
//		16 ms should be ok, 12 ms for shure
//
	private static final int PERIOD = 12000;

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

	private static Serial2 ser;

	Serial2(int priority, int us) {
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
		ser = new Serial2(8, PERIOD);
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
			while ((Native.rd(Const.IO_STATUS3) & Const.MSK_UA_RDRF)!=0 && ((i+1)&BUF_MSK)!=j) {
				rxBuf[i] = Native.rd(Const.IO_UART3);
				i = (i+1)&BUF_MSK;
			}
			wrptRx = i;
//
//	write serial data
//
			i = rdptTx;
			j = wrptTx;
			while ((Native.rd(Const.IO_STATUS3) & Const.MSK_UA_TDRE)!=0 && i!=j) {
				Native.wr(txBuf[i], Const.IO_UART3);
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

	public static void setDTR(boolean arg) {
		
		Native.wr(arg ? 1 : 0, Const.IO_STATUS3);
	}

}
