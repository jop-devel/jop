package util;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
*	Buffered serial line. Create a periodic thread
*	for the loop().
*/

public class Serial {


	private static final int BUF_LEN = 128;
	private static final int BUF_MSK = 0x7f;

	private static final int DATA = 1;
	private static final int STATUS = 0;

	// private static final int PERIOD = 3000;

/**
*	I/O address for UART.
*/
	private int io_status;
	private int io_data;
/**
*	Send buffer for serial line.
*/
	private int[] txBuf;
/**
*	Receive buffer for serial line.
*/
	private int[] rxBuf;

	private int rdptTx, wrptTx;
	private int rdptRx, wrptRx;

	private Object monitor;

	private Serial ser;

	public Serial(int addr) {

		io_status = addr + STATUS;
		io_data = addr + DATA;

		txBuf = new int[BUF_LEN];		// should be byte
		rxBuf = new int[BUF_LEN];
		rdptTx = wrptTx = 0;
		rdptRx = wrptRx = 0;
		monitor = new Object();

	}


/**
*	read and write loop.
*/
	public void loop() {

		int i, j;

		synchronized (monitor) {
//
//	read serial data
//
			i = wrptRx;
			j = rdptRx;
			while ((Native.rd(io_status) & Const.MSK_UA_RDRF)!=0 && ((i+1)&BUF_MSK)!=j) {
				rxBuf[i] = Native.rd(io_data);
				i = (i+1)&BUF_MSK;
			}
			wrptRx = i;
//
//	write serial data
//
			i = rdptTx;
			j = wrptTx;
			while ((Native.rd(io_status) & Const.MSK_UA_TDRE)!=0 && i!=j) {
				Native.wr(txBuf[i], io_data);
				i = (i+1) & BUF_MSK;
			}
			rdptTx = i;
		}
	}

	public int rxCnt() {

		int ret;

		synchronized (monitor) {
			ret = (wrptRx-rdptRx) & BUF_MSK;
		}
		return ret;
	}

	public int txFreeCnt() {

		int ret;

		synchronized (monitor) {
			ret = (rdptTx-1-wrptTx) & BUF_MSK;
		}
		return ret;
	}

	public int rd() {

		int i, ch;
		synchronized (monitor) {
			i = rdptRx;
			rdptRx = (i+1) & BUF_MSK;
			ch = rxBuf[i];
		}
		return ch;
	}

	public void wr(int c) {

		synchronized (monitor) {
			int i = wrptTx;
			wrptTx = (i+1) & BUF_MSK;
			txBuf[i] = c;
		}
	}
	
	public void setDTR(boolean arg) {

		Native.wr(arg ? 1 : 0, io_status);
	}
}
