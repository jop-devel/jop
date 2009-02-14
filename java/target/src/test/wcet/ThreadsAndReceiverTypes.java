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

package wcet;

import joprt.*;
import util.Timer;
/**
 * Example for threads complicating receiver type analyis
 * 
 * @author benedikt
 *
 */
public class ThreadsAndReceiverTypes {
    static class A {
        int foo() { return 1; }
    }
    static class B extends A {
        int foo() { return 2; }        
    }
    static class C extends A {
        int foo() { return 3; }        
    }
    static class D extends A {
        int foo() { return 4; }        
    }

    static A a = new A();
    static B b = new B();
    static C c = new C();
    static D d = new D();
    static A x = new A();
    static int val = 0;
    static void go1() {
        x = a;
        x = b;
    }
    static void go2() {
        x = c;
        x = d;
    }
    static void measure() {
        val = x.foo();
    }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new RtThread(1, 1000) {			
			public void run() {			    
				for (;;) {
					go1();
					waitForNextPeriod();
				}
			}
		};
		new RtThread(2, 1700) {			
			public void run() {			    
				for (;;) {
					go2();
					waitForNextPeriod();
				}
			}
		};
		new RtThread(3, 500) {			
			public void run() {			    
				for (;;) {
					measure();
					waitForNextPeriod();
				}
			}
		};

		RtThread.startMission();
		
		for (;;) {
			Timer.wd();
			RtThread.sleepMs(100);
			System.out.println("Value of foo: "+val);
		}
	}

}
