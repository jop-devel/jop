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
 * The purpose of this test is to verify that 'throw Error' statements
 * are removed before WCET analysis
 */
public class PruneAssertionErrors {
    static int ts, te, to;
    
    private static class LoopingAssertionError extends Error {
        public LoopingAssertionError(String msg) {
            loopRec(msg.length());
        }
        public static int loopRec(int x) {
            return x * loopRec(x-1) + 1;
        }
    }
    
    public static void main(String[] args) {
	    ts = Native.rdMem(Const.IO_CNT);
	    te = Native.rdMem(Const.IO_CNT);
	    to = te-ts;
	    invoke(to);
    }
	
    static void invoke(int x) {
        measure(x&~1);
    }
    
    static int measure(int even) {
        if((even&1) != 0) {
            throw new LoopingAssertionError("Should never happen. Also no problem with " + " allocations here");
        }
        return even*even;
    }
    
}
