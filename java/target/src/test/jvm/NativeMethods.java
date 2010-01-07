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

package jvm;

import com.jopdesign.sys.Native;

public class NativeMethods extends TestCase {

	int a, b, c;
	long l;
	char ch;
	NativeMethods ref;
	static NativeMethods sref;
	NativeMethods refa[];
	
	public String toString() {
		return "NativeMethods";
	}
	
	public boolean test() {

		boolean ok = true;
		int i, j, k;
		
		a = 1;
		b = 2; 
		c = 3;
		
		int ref = Native.toInt(this);
		ok &= Native.getField(ref, 0)==1;
		ok &= Native.getField(ref, 1)==2;
		ok &= Native.getField(ref, 2)==3;
		
		Native.putField(ref, 0, 111);
		Native.putField(ref, 1, 222);
		Native.putField(ref, 2, 333);
		
		ok &= a==111;
		ok &= b==222;
		ok &= c==333;
		
		// test caching with native access
		a = 123;
		i = a;
		// jopsys_getfield goes directly to the memory
		// as putfield invalidates this works
		j = Native.getField(ref, 0);
		ok &= i==j;
		a = 456;
		i = a;
		// this works, because putfield invalidates the cache
		Native.putField(ref, 0, 444);
		j = a;
		ok &= j==444;
		
		a = 1;
		i = a;
		j = a;
		Native.putField(ref, 1, 2);
		ok &= i==1 && j==1;
		i = a;
		Native.putField(ref, 0, 2);
		j = a;
		ok &= i==j;

		a = 123;
		i = a;
		j = a;
		k = Native.getField(ref, 0);
		ok = i==123 & j==123 & k==123;
		// update cache
		a = 456;
		i = Native.getField(ref, 0);
		j = a;
		ok = i==456 && j==456;

		return ok;
	}
	
}