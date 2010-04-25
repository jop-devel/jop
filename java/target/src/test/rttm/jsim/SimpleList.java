/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;
/**
 * @author Martin Schoeberl
 *
 */
public class SimpleList {

	static final int MAGIC = -10000;

	static final int CNT = 1000;
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	static Vector vecA = new Vector();
	static Vector vecB = new Vector();
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (sys.nrCpu<3) {
			System.out.println("Not enogh CPUs for this example");
			System.exit(-1);
		}
		Inserter ins = new Inserter();
		Startup.setRunnable(ins, 0);
		Remover rem = new Remover();
		Startup.setRunnable(rem, 1);
		
		// start the other CPUs
		sys.signal = 1;
		
		// move one element atomic from one vector to the other
		for (int i=0; i<CNT; ) {
			boolean found = false;
			Native.wrMem(1, MAGIC);	// start transaction
			int nr = vecA.size();
			if (nr>0) {
				Object o = vecA.remove(nr-1);
				vecB.addElement(o);
				found = true;
			}
			Native.wrMem(0, MAGIC);	// end transaction
			if (found) ++i;
		}


		// wait for other CPUs to finish
		while (!(ins.finished && rem.finished)) {
			;
		}
	}
	
	static class Inserter implements Runnable {

		public boolean finished;
		
		public void run() {
			for (int i=0; i<CNT; ++i) {
				Object o = new Object();
				Native.wrMem(1, MAGIC);	// start transaction
				vecA.addElement(o);
				Native.wrMem(0, MAGIC);	// end transaction
				
			}
			finished = true;
		}
	}

	static class Remover implements Runnable {

		public boolean finished;
		
		public void run() {
			for (int i=0; i<CNT; ) {
				boolean found = false;
				Native.wrMem(1, MAGIC);	// start transaction
				int nr = vecB.size();
				if (nr>0) {
					Object o = vecB.remove(nr-1);
					found = true;
				}
				Native.wrMem(0, MAGIC);	// end transaction
				if (found) ++i;
			}
			finished = true;
		}
	}

}
