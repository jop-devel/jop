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

/** This test checks whether the analysis is capable of refining
 *  the callgraph in presence of supertype implemenations:
 *  We have two subclasses T1,T2 and a superclass S
 *  The callchain is: T1.pub -> S.gen -> T1.impl
 *  and we want to ensure that the callgraph has no edge from
 *  S.gen -> T2.priv.
 *  The chain T1.pub -> S.gen -> T2.impl is very expensive
 *  (in the example), so the WCET analysis will deliver pessimistic
 *  results when callgraph pruning does not work properly.
*/  
public class ReceiverSuperImpl {
    /* Debugging signals to manipulate the cache */
    final static int CACHE_FLUSH = -51;
    final static int CACHE_DUMP = -53;

    final static boolean MEASURE_CACHE = false;
    
    static int ts, te, to;
    
    public static abstract class S {
        public void gen(int[] inp) {
            for(int i = 0; i < inp.length; i++) {
                impl(inp[i]);
            }
        }
        public abstract void pub(int[] inp);
        public abstract void impl(int v);
    }

    // T1: pub calls gen 10 times, impl is cheap
    public static class T1 extends S {
        int r;
        public void pub(int[] inp) {
        	for(int i = 0; i < 100; i++) {  //@WCA loop=100
        		gen(inp);
        	}
        }
        public void impl(int v) {
            r+=v;
        }        
    }

    // T2: pub calls gen 1 time, impl is expensive
    public static class T2 extends S {
        int r;
        public void pub(int[] inp) {
            gen(inp);
        }
        public void impl(int v) {
        	for(int i = 0; i < 100; i++) { //@WCA loop=100
        		r=(r*v)+1;
        	}
        }        
    }
    public static S obj1, obj2;

    public static int[] dat;
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
	if (val<min) min = val;
	if (val>max) max = val;
	
	if (Config.MEASURE) System.out.println(min);
	if (Config.MEASURE) System.out.println(max);
    }
    
    static void init() {
        obj1 = new T1();
        obj2 = new T2();
        dat = new int[4];
        dat[0] = 3; dat[1] = 2; dat[3] = 4; dat[4] = 8;
    }
	
    static void invoke() {
    	measure();
    	if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
    	if (MEASURE_CACHE) Native.rdMem(CACHE_DUMP);
    }
    
    static void measure() {
        obj1.pub(dat);
        obj2.pub(dat);
    }
}
