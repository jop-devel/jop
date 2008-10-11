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

package examples.scopes;

import javax.realtime.LTMemory;
import javax.realtime.LTPhysicalMemory;
import javax.realtime.PhysicalMemoryManager;
import javax.realtime.ScopedMemory;
import javax.realtime.ScratchpadScope;

import com.jopdesign.io.IOFactory;
import com.jopdesign.sys.JVMHelp;

import joprt.RtThread;

public class LocalScope {
	
	static class X {
		int v;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		
		int ia[] = IOFactory.getFactory().getScratchpadMemory();
		for (int i=0; i<256; ++i) {
			ia[i] = i+1;
		}
		for (int i=0; i<256; ++i) {
			if (ia[i] != i+1) {
				System.out.println("shit at "+i);
			}
		}
		
		// This was my version with automatic sizing
		// final ScopedMemory scope = new ScratchpadScope();
		// a RTSJ like version
		// final ScopedMemory scope = new LTPhysicalMemory(PhysicalMemoryManager.ON_CHIP_PRIVATE, 1000);
		// Andy's version
		// ThreadLocalScope scope = new ThreadLocalScope(1000);
		final Runnable run = new Runnable() {
			public void run() {
				// just generate garbage
				for (int i=0; i<3; ++i) {
					// this line generates a LOT of garbage!
					// loop count of 4 does NOT fit into our 1 KB SPM
					System.out.println("i="+i);
				}
			}			
		};

		RtThread rtt1 = new RtThread(10, 10000) {
			public void run() {
				PrivateScope scope = new PrivateScope(1000);
				System.out.print("Size of the scratchpad RAM is ");
				System.out.println(scope.size());
				for (int i=0; i<3; ++i) {
					System.out.println("enter A");
					scope.enter(run);
					waitForNextPeriod();
				}				
			}
		};
		
		RtThread rtt2 = new RtThread(10, 10000) {
			public void run() {
				PrivateScope scope = new PrivateScope(1000);
				System.out.print("Size of the scratchpad RAM is ");
				System.out.println(scope.size());
				for (int i=0; i<3; ++i) {
					System.out.println("enter B");
					scope.enter(run);
					waitForNextPeriod();
				}				
			}
		};
		
		rtt1.setProcessor(0);
		// second thread on second CPU, but we will not see the
		// output
		rtt2.setProcessor(1);

		RtThread.startMission();
	}

}
