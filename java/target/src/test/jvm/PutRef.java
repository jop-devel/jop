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

import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

public class PutRef extends TestCase {
	
	PutRef ref;
	static PutRef sref;
	PutRef refa[];
	
	public String toString() {
		return "PutRef";
	}
	
	public boolean test() {

		boolean ok = true;
		boolean excCatched = false;
		
		PutRef nptest = null;
		
		try {
			nptest.ref = this;			
		} catch(NullPointerException npx) {
			excCatched = true;
		}
		
		ok = ok && excCatched;
		
		nptest = new PutRef();
		nptest.ref = this;
		ok = ok && (nptest.ref== this);
		
		ref = this;
		ok = ok && (ref== this);

		
		sref = this;
		ok = ok && (sref== this);

		excCatched = false;
		try {
			refa[0] = this;			
		} catch(NullPointerException npx) {
			excCatched = true;
		}
		ok = ok && excCatched;

		refa = new PutRef[1];
		refa[0] = this;
		ok = ok && (refa[0]==this);

		excCatched = false;
		try {
			refa[-1] = this;			
		} catch(ArrayIndexOutOfBoundsException npx) {
			excCatched = true;
		}
		ok = ok && excCatched;
		
		excCatched = false;
		try {
			refa[2] = this;			
		} catch(ArrayIndexOutOfBoundsException npx) {
			excCatched = true;
		}
		ok = ok && excCatched;
		return ok;
	}
	
}