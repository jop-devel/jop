package ejip123.util;

/**
*	serial output for debug on uart 1.
*/
import com.jopdesign.sys.Const;
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
			while ((Native.rd(Const.IO_STATUS)&Const.MSK_UA_TDRE)==0) ;
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
		Native.wr(c, Const.IO_UART);
		Native.wr(c, Const.IO_USB_DATA);
	}
}
