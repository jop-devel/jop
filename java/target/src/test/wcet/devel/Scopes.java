/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Memory;
/**
 * Purpose: Analyze {@link Memory#enter(Runnable)}
 * Requires that in {@link com.jopdesign.sys.GC#USE_SCOPES} is set to {@code true}.
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class Scopes {
	   /* Debugging signals to manipulate the cache */
    final static int CACHE_FLUSH = -51;
    final static int CACHE_DUMP = -53;

    final static boolean MEASURE_CACHE = false;

	static class Empty implements Runnable {
		@Override
		public void run() {
		}
	}
	static class Alloc implements Runnable {
		int x[];
		@Override
		public void run() {
			x = new int[1023];
		}
	}
	static class BigMethod implements Runnable {
		@Override
		public void run() {
			int val = 23;
		    val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  
		    val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  
		    val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  
		    val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;
		    val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  val += 123;  
		}
	}

	static int ts, te, to, tos;
	Memory scope;
	
	Empty empty;
	Alloc alloc;
	BigMethod bigMethod;

	public Scopes() {
		// scope overhead
		scope = new Memory(5000);
		empty = new Empty();
		bigMethod = new BigMethod();
		alloc = new Alloc();
	}
	

	public void measure() {
		
		scopeEmpty();
		if(Config.MEASURE) {
			System.out.print("Cost for executing empty scope (excluding to): ");
			System.out.println(te-ts-to);
		}
		scopeAlloc();
		if(Config.MEASURE) {
			System.out.print("Cost for executing scope allocating 1024 words (excluding to): ");
			System.out.println(te-ts-to);
		}
		scopeFull();
		if(Config.MEASURE) {
			System.out.print("Cost for executing scope with one large method (excluding to): ");
			System.out.println(te-ts-to);
		}
	}
	public static void main(String[] args) {

		if(Config.MEASURE) {
			ts = Native.rdMem(Const.IO_CNT);
			te = Native.rdMem(Const.IO_CNT);
			to = te-ts;
		}
		
		Scopes s = new Scopes();
		s.measure();
	}
	
	public void scopeEmpty() {
		if(Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		scope.enter(empty);
		if(Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}
	public void scopeAlloc() {
		if(Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		scope.enter(alloc);
		if(Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}
	public void scopeFull() {
		if(Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		scope.enter(bigMethod);
		if(Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}
}
