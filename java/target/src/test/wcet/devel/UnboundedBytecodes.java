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

/**
 * Purpose:
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class UnboundedBytecodes {
	/* Debugging signals to manipulate the cache */
	final static int CACHE_FLUSH = -51;
	final static int CACHE_DUMP = -53;

	final static boolean MEASURE_CACHE = false;

	static int ts, te, to;
	static int[] arr1;
	static int[] arr2;
	static int[] arr3;

	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		int r=0,val = 0;

		if (MEASURE_CACHE) Native.wrMem(1,CACHE_FLUSH);
		r = invoke_testNew();
		val = te-ts-to;
		if (Config.MEASURE) { System.out.print("#cycles testNew: ");System.out.println(val); }

		if (MEASURE_CACHE) Native.wrMem(1,CACHE_FLUSH);
		r = invoke_testNewArray();
		val = te-ts-to;
		if (Config.MEASURE) { System.out.print("#cycles testNew: ");System.out.println(val); }

		if (MEASURE_CACHE) Native.wrMem(1,CACHE_FLUSH);
		invoke();
		val = te-ts-to;
		if (Config.MEASURE) { System.out.print("#cycles measure: ");System.out.println(val); }
	}

	private static void invoke() {
		measure();
		if(Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	private static void measure() {
		if(Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		testNew();
		testNewArray();
	}

	private static int invoke_testNew() {
		if(Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		int r = testNew();
		if(Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
		return r;
	}

	private static int invoke_testNewArray() {
		if(Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		int r = testNewArray();
		if(Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
		return r;
	}

	private static int testNew() {
		String x = new String("abcde");
		int l = 0;
		for(int i = 0; i < 5; i++) {  // @WCA loop=5
			l += x.length();
			if(l > 0) x = new String("abcdef");
		}
		return l;
	}
	
	private static int testNewArray() {
		int r=0,l = 17;
		int arr[][] = new int[l][];
		for(int i = 0; i < 5; i++) {  // @WCA loop=5
			arr[i] = new int[l];
			arr[i][2] = l-i;
			if(i > 0) r += arr[i-1][2];
			l += 6;
		}		
		return r;
	}

}
