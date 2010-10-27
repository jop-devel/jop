/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)

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
package cache;

import cmp.HelloCMP;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

/**
 * Test if cache invalidation is correct.
 * 
 * @author martin
 *
 */
public class TestConcurrent implements Runnable {
	
	static volatile int volStaticInt;
	static volatile long volStaticLong;
	
	volatile int volInt;
	volatile long volLong;
	
	int a, b;
	long lo;
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int i;
		long l;

		System.out.println("Test concurrent access");	
		SysDevice sys = IOFactory.getFactory().getSysDevice();
		if (sys.nrCpu==1) {
			System.out.println("Single core is implicit cache coherent");
			System.exit(0);
		}
		
		TestConcurrent tc = new TestConcurrent();

		// test r/w/r
		i = tc.a;
		i = 123456789;	// force a cache state change
		tc.a = 123;
		i = tc.a;
		if (i!=123) {
			System.out.println("Error in r/w/r on field");
		}
		l = tc.lo;
		i = 123456789;	// force a cache state change
		tc.lo = 456;
		l = tc.lo;
		if (l!=456) {
			System.out.println("Error in r/w/r on long field");
		}

		
		// set runnable 
		Startup.setRunnable(tc, 0);
		// set some default values
		volStaticInt = 123;
		volStaticLong = -1;
		tc.a = 11;
		tc.b = 22;
		tc.volInt = 1;
		tc.volLong = 1L;
		// get it into the cache
		i = tc.volInt;
		// start the other CPU(s)
		sys.signal = 1;

		while (tc.volInt==1) {
			;
		}
		System.out.println("Volatile read ok");
		// get tc.a into the cache
		synchronized (tc) {
			i = tc.a;			
		}
		tc.volInt = 3;
		while (i==11) {
			synchronized(tc) {
				i = tc.a;
			}			
		}
		System.out.println("Test finished ok");
	}


	@Override
	public void run() {
		
		volInt = 2;
		while (volInt==2) {
			;
		}
		synchronized (this) {
			a = 111;
		}
	}

}
