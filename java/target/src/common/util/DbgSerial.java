/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package util;

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
			// busy wait, no sleep for thread tests! dummy WCA value
			while ((Native.rd(Const.IO_STATUS)&Const.MSK_UA_TDRE)==0) ; // @WCA loop=100
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


	/** makes only sense for tmpfered debug output (see DbgUdp) */
	int dbgReadBuffer(int[] udpBuf, int pos) {

		return 0;
	};
}
