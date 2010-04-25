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
 * Move one element atomic from one vector to the other.
 * 
 * @author Martin Schoeberl
 *
 */
public class VecMover<T> implements Runnable {

	public boolean finished;
	private Vector out, in;
	private int cnt = 0;
	
	public VecMover(Vector v1, Vector v2) {
		out = v1;
		in = v2;
	}
	
	public void run() {
		while (cnt<Const.CNT) {
			Native.wrMem(1, Const.MAGIC);	// start transaction
			int nr = out.size();
			if (nr>0) {
				Object o = out.remove(nr-1);
				in.addElement(o);
				++cnt;
			}
			Native.wrMem(0, Const.MAGIC);	// end transaction
		}
		finished = true;
	}
	
}

