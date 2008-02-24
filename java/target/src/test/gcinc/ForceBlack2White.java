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

package gcinc;

import joprt.RtThread;

import com.jopdesign.sys.GC;

public class ForceBlack2White {
	
	int nr;
	ForceBlack2White r1, r2;
	

	public ForceBlack2White(int i) {
		nr = i;
	}

	static ForceBlack2White nroot;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		nroot = new ForceBlack2White(1); 
			
		nroot.r1 = new ForceBlack2White(2);
		nroot.r2 = new ForceBlack2White(3);
		nroot.r1.r1 = new ForceBlack2White(4);

		new RtThread(2, 333) {
			public void run() {

				ForceBlack2White r, tmp;
				for (;;) {
					ForceBlack2White abc = new ForceBlack2White(123);
					r = nroot;
					// switch pointer to 4 from 2 to 3
					tmp = r.r1.r1;
					r.r1.r1 = null;
					waitForNextPeriod();
					r.r2.r1 = tmp;
					waitForNextPeriod();
					check();
					waitForNextPeriod();
					// switch pointer to 4 from 3 to2
					tmp = r.r2.r1;
					r.r2.r1 = null;
					waitForNextPeriod();
					r.r1.r1 = tmp;
					waitForNextPeriod();
					check();
					waitForNextPeriod();
//					System.out.print('.');
//					if (abc.nr!=123) {
//						System.out.println("Error in GC (local)");
//						System.exit(1);
//					}
//					System.out.print('.');
					abc = null;
					waitForNextPeriod();
				}
			}
			
			void check() {
				boolean ok = true;
				ForceBlack2White r = nroot;
				if (r.nr!=1) ok = false;
				if (r.r1.nr!=2) ok = false;
				if (r.r2.nr!=3) ok = false;
				if (r.r1.r1!=null && r.r1.r1.nr!=4) ok = false;
				if (r.r2.r1!=null && r.r2.r1.nr!=4) ok = false;
				if (!ok) {
					System.out.println("Error in GC");
					System.exit(1);
				}
			}
		};

		new RtThread(1, 3456) {
			public void run() {
				GC.setConcurrent();
				for (;;) {
					waitForNextPeriod();
//					int time = RtSystem.currentTimeMicro();
					System.out.print("G");
					GC.gc();
//					int now = RtSystem.currentTimeMicro();
//					System.out.println(now-time);
//					time = now;
					
//					waitForNextPeriod();
				}
			}
		};
		
		RtThread.startMission();
	}

}
