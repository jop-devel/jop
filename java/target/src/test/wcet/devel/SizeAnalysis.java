/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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


public class SizeAnalysis {
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
    	int min = 0x7fffffff;
    	int max = 0;
    	int val = 0;
    	init();
    	if (MEASURE_CACHE) Native.wrMem(1,CACHE_FLUSH);
    	invoke();
    	val = te-ts-to;
    	if (Config.MEASURE)       { 
    		System.out.print("max: "); System.out.println(val);
    	}
    }

    static void init() {
	arr1 = new int[10];
	arr2 = new int[5];
	arr3 = new int[15];
    }
	
    static void invoke() {
	measure();
	if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	if (MEASURE_CACHE) Native.rdMem(CACHE_DUMP);
    }
    
    static void measure() {
    	if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
    	copy(arr1,arr2);
    	copy(arr2,arr3);
    	copy(arr3,arr1);
    }
    
    static void copy(int a[], int b[]) {
	int l1 = a.length;
	int l2 = b.length;
	int lmin = (l1 > l2) ? l2 : l1;
	for(int i = 0; i < lmin; i++)
	    {
		a[i] = b[i];
	    }
    }

}
