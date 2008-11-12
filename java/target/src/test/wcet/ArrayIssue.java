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

import util.Timer;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.*;

/**
 * WCET timing issue - measurement differs from WCET analysis
 * @author martin
 *
 */
public class ArrayIssue {

	/**
	 * Set to false for the WCET analysis, true for measurement
	 */
	final static boolean MEASURE = false;
	static int ts, te, to;
	static int a[] = new int[1];

	public static void main(String[] args) {
		
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		measure();
		if (MEASURE) System.out.println(te-ts-to);
	}
	
	static void measure() {
		if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
		SysDevice sys = IOFactory.getFactory().getSysDevice();
		sys.wd = 1;
		a[0] = 0;
		if (MEASURE) te = Native.rdMem(Const.IO_CNT);		
	}
}
