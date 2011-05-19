/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2009, Martin Schoeberl (martin@jopdesign.com)
  Author:                  Benedikt Huber (benedikt.huber@gmail.com)
  
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
package wcet.algorithms;

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/** 
 *  Implementation of Euclid's greatest common divisor algorithm (implementation using modulo).
 *  Interesting because % is currently a Java Implemented Bytecode
 */
public class GreatestCommonDivisor {
	static int ts,te,to;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;

		measure(1134903170,1836311903);
		
		if (Config.MEASURE) System.out.println(te-ts-to);		
	}

	public static void measure(int a, int b) {
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		gcd(a,b);
		if(Config.MEASURE)  te = Native.rdMem(Const.IO_CNT);		
	}
	// See [Introduction to Algorithms, Cormen at al.]: if a > b >= 1, and EUCLID(a,b) performs k >= 1 recursive calls, a >= F(k+2), b >= F(k+1)
	// As a is at most 2**31 < F(47), by a < F(45+2), gcd needs at most 44 loop iterations
	// [see example for 45 loop iterations at end of file]
	// If b is 0, we return a immediately, if a is 0, swap in the first loop iteration.
	// If(b > a) we have a swap and an additional loop iteration
	//  ==> at most 45 loop iterations
	public static int gcd(int a, int b) {
	    if(a < 0) a = -a;
	    if(b < 0) b = -b;
	    int temp;
	    while(b != 0) { // @WCA loop<=45
	        temp = b;
	        b    = a % b; 
	        a    = temp;
	    }
	    return a;
	}
}

// (0,(2971215073 (L!),1836311903))
// (1,(1836311903,1134903170))
// (2,(1134903170,701408733))
// (3,(701408733,433494437))
// (4,(433494437,267914296))
// (5,(267914296,165580141))
// (6,(165580141,102334155))
// (7,(102334155,63245986))
// (8,(63245986,39088169))
// (9,(39088169,24157817))
// (10,(24157817,14930352))
// (11,(14930352,9227465))
// (12,(9227465,5702887))
// (13,(5702887,3524578))
// (14,(3524578,2178309))
// (15,(2178309,1346269))
// (16,(1346269,832040))
// (17,(832040,514229))
// (18,(514229,317811))
// (19,(317811,196418))
// (20,(196418,121393))
// (21,(121393,75025))
// (22,(75025,46368))
// (23,(46368,28657))
// (24,(28657,17711))
// (25,(17711,10946))
// (26,(10946,6765))
// (27,(6765,4181))
// (28,(4181,2584))
// (29,(2584,1597))
// (30,(1597,987))
// (31,(987,610))
// (32,(610,377))
// (33,(377,233))
// (34,(233,144))
// (35,(144,89))
// (36,(89,55))
// (37,(55,34))
// (38,(34,21))
// (39,(21,13))
// (40,(13,8))
// (41,(8,5))
// (42,(5,3))
// (43,(3,2))
// (44,(2,1))
// (45,(1,0))