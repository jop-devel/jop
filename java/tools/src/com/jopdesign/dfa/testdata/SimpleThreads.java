/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Wolfgang Puffitsch

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

package com.jopdesign.dfa.testdata;

import joprt.RtThread;

public class SimpleThreads {

	static X x;
	static Object obj;
	
	public static void main(String [] args) {
		
		RtThread t1 = new T1(1, 1000);
		RtThread t2 = new T2(2, 1000);

		RtThread.startMission();
	}
}

class X {
	void foo() {}
}
class Y extends X {
	void foo() {
		SimpleThreads.obj = SimpleThreads.x;
	}
}
class Z extends X {}

class T1 extends RtThread {
	
	public T1(int prio, int us) {
		super(prio, us);
	}
	
	public void run() {
		SimpleThreads.x = new Y();
		SimpleThreads.x.foo();
	}
}

class T2 extends RtThread {	

	public T2(int prio, int us) {
		super(prio, us);
	}

	public void run() {
		SimpleThreads.x = new Z();
	}
}

class T3 extends RtThread {	

	public T3(int prio, int us) {
		super(prio, us);
	}

	public void run() {
		SimpleThreads.x = new X();
	}
}	
