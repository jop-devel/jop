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

import util.Timer;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;
/**
 * @author Martin Schoeberl
 *
 */
public class TestExample {

	final static int CNT = 3;
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	static int[] ia = new int[100];
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Runner r[] = new Runner[sys.nrCpu-1];
		for (int i=0; i<sys.nrCpu-1; ++i) {
			r[i] = new Runner();
			Startup.setRunnable(r[i], i);
		}
		
		Runnable me = new Runner();
		// start the other CPUs
		sys.signal = 1;
		
		me.run();

		// wait for other CPUs to finish
		boolean allDone = false;
		while (!allDone) {
			allDone = true;
			for (int i=0; i<sys.nrCpu-1; ++i) {
				allDone &= r[i].finished;
			}			
		}
	}
	
	static class Runner implements Runnable {

		public boolean finished;
		
		private static final int MAGIC = -10000;

		public void run() {
			for (int i=0; i<CNT; ++i) {
				
				Native.wrMem(1, MAGIC);	// start transaction
//				System.out.println(sys.cpuId);
//				System.out.println("hello");
				for (int j=0; j<ia.length; ++j) {
					ia[j] = sys.cpuId+1;
				}
				for (int j=0; j<ia.length; ++j) {
					if (ia[j]!=sys.cpuId+1) {
						synchronized (this) {
							System.out.println("wrong data "+sys.cpuId);							
						}
						break;
					}
				}
				Native.wrMem(0, MAGIC);	// end transaction
				
				// data should be consistent and non zero
				Native.wrMem(1, MAGIC);	// start transaction
				int val = ia[0];
				if (val==0) {
					synchronized (this) {
						System.out.println("data is zero "+sys.cpuId);							
					}					
				}
				for (int j=0; j<ia.length; ++j) {
					if (ia[j]!=val) {
						synchronized (this) {
							System.out.println("data inconsistent "+sys.cpuId);							
						}
						break;
					}
				}				
				Native.wrMem(0, MAGIC);	// end transaction
			}
			finished = true;
		}
		
	}

}
