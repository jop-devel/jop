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

package ejip123.util;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import joprt.RtThread;

/** Buffered serial line. Uses a periodic {@link RtThread} for the loop(). */
public class Serial{

private static final int BUF_LEN = 128;
private static final int BUF_MSK = 0x7f;

private static final int DATA = 1;
private static final int STATUS = 0;

// private static final int PERIOD = 3000;

/** I/O address for UART. */
private int io_status;
private int io_data;
/** Send buffer for serial line. */
private int[] txBuf;
/** Receive buffer for serial line. */
private int[] rxBuf;

private int rdptTx, wrptTx;
private int rdptRx, wrptRx;

private final Object monitor;

//private Serial ser;

public Serial(int prio, int us, int addr){

	io_status = addr + STATUS;
	io_data = addr + DATA;

	txBuf = new int[BUF_LEN];		// should be byte
	rxBuf = new int[BUF_LEN];
	rdptTx = wrptTx = 0;
	rdptRx = wrptRx = 0;
	monitor = new Object();

	new RtThread(prio, us){
		public void run(){
			for(; ;){
				waitForNextPeriod();
				loop();
			}
		}
	};

}

/** read and write loop. */
private void loop(){

	synchronized(monitor){
//	read serial data
		int i = wrptRx;
		int j = rdptRx;
		while((Native.rd(io_status)&Const.MSK_UA_RDRF) != 0 && ((i + 1)&BUF_MSK) != j){
			rxBuf[i] = Native.rd(io_data);
			i = (i + 1)&BUF_MSK;
		}
		wrptRx = i;

//	write serial data
		i = rdptTx;
		j = wrptTx;
		while((Native.rd(io_status)&Const.MSK_UA_TDRE) != 0 && i != j){
			// FIXME deadlock in simulator under windows if there is no slip connection established
			Native.wr(txBuf[i], io_data);
			i = (i + 1)&BUF_MSK;
		}
		rdptTx = i;
	}
}

public int rxCnt(){

	int ret;

	synchronized(monitor){
		ret = (wrptRx - rdptRx)&BUF_MSK;
	}
	return ret;
}

public int txFreeCnt(){

	int ret;

	synchronized(monitor){
		ret = (rdptTx - 1 - wrptTx)&BUF_MSK;
	}
	return ret;
}

public int rd(){

	int ch;
	synchronized(monitor){
		int i = rdptRx;
		rdptRx = (i + 1)&BUF_MSK;
		ch = rxBuf[i];
	}
	return ch;
}

public void wr(int c){

	synchronized(monitor){
		int i = wrptTx;
		wrptTx = (i + 1)&BUF_MSK;
		txBuf[i] = c;
	}
}

public void setDTR(boolean arg){
	synchronized(monitor){
		Native.wr(arg ? 1 : 0, io_status);
	}
}
}
