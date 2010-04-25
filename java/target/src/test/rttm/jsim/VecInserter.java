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

import java.util.Vector;

import com.jopdesign.sys.Native;

/**
 * @author Martin Schoeberl
 *
 */
public class VecInserter<T> implements Runnable {

	public boolean finished;
	private Vector vec;
	// recycle the Object -- we would use a pool in a real application
	Object o = new Object();

	public VecInserter(Vector v) {
		vec = v;
	}

	public void run() {
		Object o = new Object();
		for (int i=0; i<Const.CNT; ++i) {
			Native.wrMem(1, Const.MAGIC);	// start transaction
			vec.addElement(o);
			Native.wrMem(0, Const.MAGIC);	// end transaction
			
		}
		finished = true;
	}
}
