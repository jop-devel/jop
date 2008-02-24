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


package test;

import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

class M {

	public static void main( String s[] ) {

		Dbg.initSerWait();

		int t1 = Native.rd(Const.IO_CNT);
		int t2 = Native.rd(Const.IO_CNT);
		int diff = t2-t1;

/*
		Dbg.intVal(diff);

		t1 = Native.rd(Const.IO_CNT);
		Util.mul(2, 3);
		t2 = Native.rd(Const.IO_CNT);
		t2 = t2-t1-diff;

		Dbg.intVal(t2);

		for (;;) ;
*/

		int val = 0;
		for (;;) {
			if (val - Native.rd(Const.IO_US_CNT) < 0) {
				Dbg.intVal(val);
				val = Native.rd(Const.IO_US_CNT) + 1000000;
			}
		}

	}
}
