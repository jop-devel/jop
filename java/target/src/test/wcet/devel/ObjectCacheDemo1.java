/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)

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

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/* A demo where our object cache analysis performs really well */
public class ObjectCacheDemo1 {
    /**
     * Set to false for the WCET analysis, true for measurement
     */
    final static int CACHE_FLUSH = -51;
    final static int CACHE_DUMP = -53;
    final static boolean MEASURE = true;
    final static boolean MEASURE_CACHE = true;

    final static int N = 1; // outer iteration count
    static int ts, te, to;
    static int cs, ce;

    public static class X {
        public int x1;
        public int x2;
        public X(int f1, int f2)
        {
            x1 = f1;
            x2 = f2;
        }
    }

    public static class Y {
        public int y1;
        public int y2;
        public Y(int f1, int f2)
        {
            y1 = f1;
            y2 = f2;
        }
    }

    /* static references */
    static X x;
    static Y y;

    public static void main(String[] args) {
        
        ts = Native.rdMem(Const.IO_CNT);
        te = Native.rdMem(Const.IO_CNT);
        to = te-ts;
        x = new X(2,3);
        y = new Y(3,4);
        int v = invoke();
        System.out.print("Value: ");
        System.out.println(v);
        int val = te-ts-to;
        System.out.println("Time: "+val);
    }
	
    static int invoke() {
        if (MEASURE_CACHE) Native.wrMem(1,CACHE_FLUSH);
        int v = measure();
        if (MEASURE) te = Native.rdMem(Const.IO_CNT);
        if (MEASURE_CACHE) Native.rdMem(CACHE_DUMP);
        return v;
    }
    // At most 2*N lines replaced
    static int measure() {
        if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
        int v = 0;
        for(int i = 0; i < N; i++)
            {
                x = new X(i,5);
                y = new Y(8,i);
                v += test(x,y);
            }
        return v;
    }

    static int test(X x, Y y)
    {
        int v = 0;
        if(y.y1 == 0) {
            v ^= x.x1;
        } else {
            v ^= x.x2;
        }
        for(int i = 0; i < 50; i++) {
            if(i % 2 == 0) {
                v ^= (x.x1 + x.x2);
            } else {
                v ^= (y.y1 + y.y2);
            }
        }
        return v;
    }	
}
