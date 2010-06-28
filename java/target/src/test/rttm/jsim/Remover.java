/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package rttm.jsim;

import rtlib.SRSWQueue;

import com.jopdesign.sys.Native;

/**
 * @author Martin Schoeberl
 *
 */
public class Remover<T> implements Runnable {

	public boolean finished;
	private SRSWQueue<T> q;
	private int cnt = 0;
	
	public Remover(SRSWQueue<T> queue) {
		q = queue;
	}
	
	public void run() {
		while (cnt<Const.CNT) {
			Native.wrMem(1, Const.MAGIC);	// start transaction
			Object o = q.deq();
			if (o!=null) {
				++cnt;
			}
			Native.wrMem(0, Const.MAGIC);	// end transaction
		}
		finished = true;
	}
}
