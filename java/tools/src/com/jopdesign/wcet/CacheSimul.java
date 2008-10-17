/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)
  Copyright (C) 2006, Rasmus Ulslev Pedersen

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

package com.jopdesign.wcet;

import org.apache.bcel.classfile.Method;

public class CacheSimul {

	// that's not nice. Why is the clinit
	// not invoked???
	static MethodCache mc = new MethodCache(2048, 16, false);
	{
		System.out.println("cach clinit");
		clear();
	}
	// return true on a cache hit
	public static boolean get(Method m) {

		boolean hit = mc.invoke(m);
//		System.out.println(m.getName()+" hit="+hit);
		return hit;
	}

	// return true on a cache hit
	public static boolean invoke(Method m) {

		boolean hit = mc.invoke(m);
		System.out.println(m.getName()+" invoke hit="+hit);
		return hit;
	}
	// return true on a cache hit
	public static boolean ret(Method m) {

		boolean hit = mc.ret(m);
		System.out.println(m.getName()+" return hit="+hit);
		return hit;
	}

	public static void clear() {
		System.out.println("clear the cache");
		mc = new MethodCache(2048, 16, false);
	};
}

/**
 *
 * Simulation of the variable block method cache
 * @see http://www.jopdesign.com/doc/jtres_cache.pdf
 * @author martin
 *
 */
class MethodCache {

	Method[] ctag;
//	int[] clen;		// for different replace policy

	int next = 0;
	int currentBlock = 0;
	int numBlocks;
	int blockSize;

	boolean stackNext;

	MethodCache(int size, int num, boolean stkNxt) {

		numBlocks = num;
		blockSize = size/numBlocks;
		ctag = new Method[numBlocks];
		for (int i=0; i<numBlocks; ++i) {
			ctag[i] = null;
		}

		stackNext = stkNxt;
	}



	boolean invoke(Method m) {

		boolean hit = testCache(m);
		return hit;
	}

	boolean ret(Method m) {

/*
	stack-next policy
*/
		if (stackNext) {
			next = currentBlock;
		}
		boolean hit = testCache(m);
		return hit;

	}

	boolean testCache(Method m) {

		for (int i=0; i<numBlocks; ++i) {
			if (ctag[i]==m) {	// HIT
				currentBlock = i;
				return true;
			}
		}

		// not found

		int len = (m.getCode().getCode().length + 3)/4;
		currentBlock = next;
		// it's '<=' in the VHDL implementation
		// simpler than correct rounding up, one block
		// waste on a method with exact length n*blockSize
		for (int i=0; i<=len*4/blockSize; ++i) {
			ctag[next] = null;				// block in use
			++next;
			next %= numBlocks;
// System.out.print(i+" "+next+" - ");
		}
		ctag[currentBlock] = m;		// start block
// for (int i=0; i<numBlocks; ++i) System.out.print(ctag[i]+" "); System.out.println("next="+next+" len="+len);

		return false;
	}

	public String toString() {

		return "Variable block cache "+(numBlocks*blockSize/1024)+
			" KB & "+numBlocks;
	}
}
