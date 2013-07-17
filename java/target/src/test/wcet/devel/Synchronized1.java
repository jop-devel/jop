/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt.huber@gmail.com)

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

package wcet.devel;
/*
  make jsim P1=test P2=wcet/devel P3=Synchronized1
  
*/
import joprt.*;
import util.Timer;

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Synchronized1 {

	static class Mon {
		public static final int LO =   0;
		public static final int HI = 100;
		private int value;
		public Mon(int initial)    { value = initial; }
		public int getValue()      { return value;    }
		public synchronized boolean increment() { value++; return (value < HI); }
		public synchronized boolean decrement() { value--; return (value > LO); }
                public synchronized boolean syncrement(Mon other) {
                    if(getValue() < HI && other.increment()) { return increment(); }
                    else                                     { return false;       }
                }
		public synchronized boolean check() {
			int sum = 0;
			for(int i = 0; i < value; i++) { //@WCA loop <= 100
				sum += value;
			}
			return(sum==0 || sum==5050);
		}
	}
	private static Mon monitor1 = new Mon(50);
	private static Mon monitor2 = new Mon(50);
        private static int shared1 = 1, shared2 = 1;
        private static int unshared1 = 1, unshared2 = 1;


	static int ts, te, to;


	/**
	 * @param args
	 */
	public static void main(String[] args) {

                // tests
                ts = Native.rdMem(Const.IO_CNT);
                te = Native.rdMem(Const.IO_CNT);
                to = te-ts;
                run1();
                run2();
                run3();
                run4();
                run5();
		// initialization

		new RtThread(1, 3000) {
			
			public void run() {
				
				for (;;) {
					run4();
					if (!waitForNextPeriod()) {
						break;
					}
				}
			}
		};


		new RtThread(2, 500) {
			

			public void run() {

				for (;;) {
					run5();
					if (!waitForNextPeriod()) {
						break;
					}
				}
			}
		};

		System.out.println("Start Mission");
		RtThread.startMission();
		
		for (;;) {
			Timer.wd();
			System.out.println("monitor1 (unsync): " + monitor1.getValue());
			System.out.println("monitor2 (unsync): " + monitor2.getValue());
			RtThread.sleepMs(1000);
		}
	}
        /* For run1(), we expect one blocking time, namely Mon#check */
        public static void run1() {
            monitor1.check();
            monitor2.check();
        }

        /* For run2(), we expect three blocking times, 2+3 should be the same
           and larger than 1 */
        public static void run2() {

            ts = Native.rdMem(Const.IO_CNT);
            synchronized(monitor1) {
                shared1 = shared1 * shared2;
            }
            te = Native.rdMem(Const.IO_CNT);
            System.out.print("[run2/1] ");
            System.out.println(te-ts-to);

            ts = Native.rdMem(Const.IO_CNT);
            synchronized(monitor2) {
                shared1 = shared1 * shared1 + shared2 * shared2;
            }
            te = Native.rdMem(Const.IO_CNT);
            System.out.print("[run2/2] ");
            System.out.println(te-ts-to);

            for(int i = 0; i < 100; i++) { //@WCA loop=100
                unshared1 = unshared1 * 3 + 1;
            }

            ts = Native.rdMem(Const.IO_CNT);
            synchronized(monitor2) {
                shared1 = shared1 * shared1 + shared2 * shared2;
            }
            te = Native.rdMem(Const.IO_CNT);
            System.out.print("[run2/3] ");
            System.out.println(te-ts-to);
        }
        /* run3(): triple nested synchronized */ 
        public static void run3() {
            int x = 0;
            synchronized(monitor1) {
                x += shared1;
                synchronized(monitor2) {
                    if(monitor1.syncrement(monitor2)) {
                        x+=1;
                    }
                }
                x += shared1;
                shared1 = 2 * x;
            }
        }

        /* For run4(), we expect 5 blocking times:
           Mon#check, the first synchronized block (nested: Mon#decrement),
           the second synchronized block (nested: Mon#increment).
           The nesting in this test accesses the same lock twice */
	public static void run4() {
		monitor1.check();
		synchronized(monitor1) {
			while(monitor1.decrement()) { // @WCA loop<=100
			}
		}
		for(int i = 0; i < 32000; i++) {  //@WCA loop = 32000
			unshared1 = unshared1 * 3 + 1;
		}
		monitor2.check();
		synchronized(monitor2) {
			while(monitor2.increment()) { //@WCA loop<=100
			}
		}
	}
        /* run5() has true nested locks, and is otherwise similar to run3() */
	public static void run5() {
		monitor2.check();
		synchronized(monitor1) {
			while(monitor2.decrement()) { // @WCA loop<=100
			}
		}
		for(int i = 0; i < 32000; i++) {  //@WCA loop = 32000
			unshared1 = unshared1 * 3 + 1;
		}
		monitor1.check();
		synchronized(monitor2) {
			while(monitor1.increment()) { //@WCA loop<=100
			}
		}
	}
}
