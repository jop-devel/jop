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
 * @author Martin Schoeberl
 * 
 */
public class ReadWriteQueueInserter extends QueueThread {

	public volatile boolean finished;
	private WaitFreeReadWriteQueue q;
	private int cnt = 0;

	public ReadWriteQueueInserter(WaitFreeReadWriteQueue queue) {
		super();
		q = queue;
	}

	public void run() {
//		System.out.println("Inserter: run();");
		Object o = new Object(); // reusable object
//		System.out.println("IOC");
		while (cnt < Const.CNT) {
//			System.out.println("Inserter: calling write .....");
			++cnt;
			while (!q.write(o, this)) {
//				System.out.println("Inserter: calling write2 .....");
			}
			System.out.println("i: " + cnt);
		}
		finished = true;
	}
}
