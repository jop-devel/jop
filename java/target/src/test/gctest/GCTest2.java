/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen

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

package gctest;

import joprt.RtThread;

import com.jopdesign.sys.GC;

// GcTest2
// The test is supposed to test if GC takes place after several 
// threads has passes ONE reference down the line. The last thread
// nulls the reference and we check that GC detects it.
// Parameters: This test can be changed using the NUMTHREADS constant

public class GCTest2 {
	static int NUMTHREADS;

	static GarbageThread[] garbageThreads;

	public static void main(String s[]) {
		NUMTHREADS = 500; //Takes about 2 min to run.
		System.out.print("NUMTHREADS ");
		System.out.println(NUMTHREADS);
		garbageThreads = new GarbageThread[NUMTHREADS];
		System.out.println("gabagethreads length " + garbageThreads.length);

		for (int i = 0; i < NUMTHREADS; i++) {
			GarbageThread gt = new GarbageThread(i + 10, (i + 1) * 100000, i,
					NUMTHREADS, garbageThreads);
			garbageThreads[i] = gt;
		}
		System.out.println("Threads created");

		int gcBefore = GC.freeMemory();
		Garbage2 garbage = new Garbage2();
		garbage.size = gcBefore - GC.freeMemory();
		System.out.println("here2");
		// Give the garbage object to the first thread

		garbageThreads[0].garbage = garbage;
		System.out.println("here3");
		garbage = null;
		System.out.println("here2");
		RtThread.startMission();
		System.out.println("startMission called");
		for (;;) {
			System.out.println("Sleeping for 1000 ms");
			RtThread.sleepMs(1000);
		}
	}

}

class GarbageThread extends RtThread {
	int id;

	public Garbage2 garbage;

	public GarbageThread[] garbageThreads;

	public int NUMTHREADS;

	public GarbageThread(int prio, int us, int id, int NUMTHREADS,
			GarbageThread[] garbageThreads) {
		super(prio, us);
		this.id = id;
		this.NUMTHREADS = NUMTHREADS;
		this.garbageThreads = garbageThreads;
	}

	public void run() {
		for (;;) {
			// The threads just pass on the reference
			System.out.print("Garbage therad ");
			System.out.println(id);
			if (id < NUMTHREADS - 1) {
				if (garbage != null) { // Pass on the garbage reference if I
										// have it
					garbageThreads[id + 1].garbage = garbage;
					garbage = null;
					System.out.println("garbage passed on");
				}
			}

			// The last thread releases the reference and checks GC
			if (id == NUMTHREADS - 1 && garbage != null) {
				System.out.println("Final stuff");
				int size = garbage.size;
				System.out.print("Size of gabage object:");
				System.out.println(size);
				GC.gc(); // Remove other garbage first to calibrate
				int gcBefore = GC.freeMemory();
				garbage = null; // So much work for so little, but here it is
				GC.gc();
				int gcAfter = GC.freeMemory();
				
				if ((gcAfter-gcBefore) != size) {
					System.out.println("GC did not collect the floating reference");
					System.out.println("gcBefore " + gcBefore);
					System.out.println("gcAfter " + gcAfter);
					System.exit(-1);
				} else {
					System.out.println("Test completed OK");
					System.exit(0);
				}
			}
			waitForNextPeriod();
		}
	}
}

class Garbage2 {
	public int size = -1;

	public Garbage2() {
		System.out.println("The only Garbage object created.");
	}
}
