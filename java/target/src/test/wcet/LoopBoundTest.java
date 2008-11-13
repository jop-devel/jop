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

import com.jopdesign.sys.*;

public class LoopBoundTest {

	static int ts, te, to;
        static final String MSG = "Loop bound analysis rocks !";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;

		/* int loopiters = */ measure();
		System.out.println(te-ts-to);
		// System.out.print("Sum of loop iterations = ");
		// System.out.println(loopiters);
	}
	
	static int measure() {
		ts = Native.rdMem(Const.IO_CNT);
		//int iters = 0;
		int val = 123;
		/* WC: 0,3,..,99 -> 34 */
		for(int j = 0; j < 100; j+= 3) { // @WCA loop=34
		    //iters++;
		    val = val*val+1;
		}

		/* WC: 0,4,..,96  --> 25 */
		int i=0;val=val-val+3;
		while(i<100) { // @WCA loop<=25
		    //iters++;
		    switch(val % 4) {
		    case 0: i+=5; break;
		    case 1: i+=4; break;
		    case 2: i+=3; 
		    case 3: i+=3;
		    }
		    i++;
		    val = val+4;
		}
		/* WC: [0,3,..,99] -> 34 */
		i = 0;val=val-val+2;
		while(i<100) { // @WCA loop<=34
		    //iters++;
		    i+=5;
		    switch(val % 4) {
		    case 0: i-=2; break;
		    case 1: i--; break;
		    case 2: i-=1; 
		    case 3: i-=2; break;
		    }
		    i++;
		    val = val+4;
		}
		calcLen(MSG);
		te = Native.rdMem(Const.IO_CNT);
		return 0;
	}
        /* Loop bound assumes that the argument has length <= 30 ... 
	 * We know there is only one call to calcLen with a fixed length arg,
	 * but in general callstrings would be nice here */
        public static int calcLen(String s) {
	    int len = 0;
	    for(int i = 0; i < s.length(); i++) { // @WCA loop<=30
		len++;
	    }
	    return len;
        }
}
