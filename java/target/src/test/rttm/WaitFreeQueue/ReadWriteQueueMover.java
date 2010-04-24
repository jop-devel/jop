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
package rttm.WaitFreeQueue;

/**
 * Move one element atomic from one vector to the other.
 * 
 * @author Martin Schoeberl
 * 
 */
public class ReadWriteQueueMover extends QueueThread {

	public boolean finished;
	private WaitFreeReadWriteQueue out, in;
	private int cnt = 0;

	public ReadWriteQueueMover(WaitFreeReadWriteQueue q1,
			WaitFreeReadWriteQueue q2) {
		super();
		out = q1;
		in = q2;
	}

	public void run() {
		
		Object o = null;
		while (cnt < Const.CNT) {
			o = null;
			do {
				o = out.read(this);
			} while (o != null);
			while (!in.write(o, this))
				;
			++cnt;
			System.out.println("m: " + cnt);

		}
		finished = true;
	}
}
