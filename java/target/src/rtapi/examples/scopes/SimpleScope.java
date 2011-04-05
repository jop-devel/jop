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
import javax.realtime.ScopedMemory;

import joprt.RtThread;

public class SimpleScope {

	static class Abc {
		Object ref;
	}
	static class MyRunner implements Runnable {
		
		ScopedMemory outer;
		
		public void run() {
			System.out.println(abcref);
			for (int i=0; i<10; ++i) {
				String s = "i="+i;
			}
			// this should throw an exception
			// sa.ref = abcref;
			// Besides being recursive it is not
			// allowed to reenter a scope again
			// outer.enter(this);
		}
		Abc abcref;
		void setAbc(Abc abc) {
			abcref = abc;
		}
		void setOuter(ScopedMemory sc) {
			outer = sc;
		}
	}
	
	static Abc sa = new Abc();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final ScopedMemory scope = new LTMemory(20000L);
		final ScopedMemory inner = new LTMemory(10000L);
		final Runnable run = new Runnable() {
			public void run() {
				MyRunner r = new MyRunner();
				for (int i=0; i<10; ++i) {
					Abc abc = new Abc();
					r.setAbc(abc);
					r.setOuter(scope);
					for (int j=0; j<10; ++j) {
						inner.enter(r);
					}
				}
			}			
		};

		System.out.println("some new");
		new RtThread(10, 500000) {
			public void run() {
				for (;;) {
					for (int i=0; i<20; ++i) {
						System.out.print("*");
						scope.enter(run);
						// this is a dangling reference
						// sa.ref.toString();
					}				
					waitForNextPeriod();
				}
			}
		};
		
		System.out.println("start mission");
		RtThread.startMission();
		System.out.println("mission started");
	}

}
