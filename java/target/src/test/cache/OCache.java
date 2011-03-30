/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package cache;

import com.jopdesign.sys.Native;

import jembench.application.BenchLift;
import jembench.micro.*;
import jvm.obj.*;


/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class OCache {
	
	public int a;
	int b;
	int c;
	static int s;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		OCache oc = new OCache();
		OCache oc2 = new OCache();
		int i, j;
		com.jopdesign.io.SysDevice sys = com.jopdesign.io.IOFactory.getFactory().getSysDevice();

		oc.b = 15;
		oc.c = 255;
		// a marker for the ModelSim simulation
		sys.wd = 1;
		s = 15;
		
		oc.a = 1;
		i = oc.a;
		j = oc.a;
		i = oc.b;
		j = oc.b;
		i = oc.c;
		j = oc.c;
		oc.b = 64;
		i = oc.b;
		oc.a = 7;
		j = oc.a;
		int ref = Native.toInt(oc);
//		Native.putField(ref, 0, 2);
		i = Native.getField(ref, 0);
		i = oc.a;
//		// test caching of I/O devices
//		i = sys.cntInt;
//		j = sys.cntInt;
		sys.wd = 0;
		oc2.a = 3;
		i = oc.a;
		i = oc2.a;
		j = oc.a;
		j = oc2.a;
		sys.wd = 1;
//		GetField gf = new GetField();
//		BenchLift bl = new BenchLift();
		Basic btest = new Basic();
		sys.wd = 0;
//		gf.perform(10);
//		bl.perform(10);
		boolean result = btest.test();
		sys.wd = 1;
		System.out.println(result);
	}

}
