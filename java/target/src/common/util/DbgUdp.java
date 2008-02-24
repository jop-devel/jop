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
