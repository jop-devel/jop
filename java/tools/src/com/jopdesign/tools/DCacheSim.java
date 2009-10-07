/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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
package com.jopdesign.tools;

import java.util.*;

/**
 * Extension of JopSim to simulation data caches
 * 
 * @author Martin Schoeberl
 * 
 */
public class DCacheSim extends JopSim {
	
	static abstract class Cache {
		int tag[], data[];
		boolean valid[];
		int rdCnt;
		int hitCnt;

		abstract int read(int addr, int val);
		
		abstract void inval();
		
		public String toString() {
			int percent = 0;
			if (rdCnt!=0) {
				percent = (hitCnt*1000/rdCnt+5)/10;
			}
			return String.format("\t%3d\t& %10d & %10d & %2d\\%% \\\\",
					tag.length, rdCnt, hitCnt, percent);
		}
	}
	/**
	 * A direct mapped cache with line size of one word. Size should be a power of 2.
	 */
	static class DirectMapped extends Cache {
		
		int shift;

		/**
		 * A shift is needed for the handle cache, as handles are at
		 * multiples of 8.
		 * @param size Cache size.
		 * @param shift of address bits for line determination.
		 */
		public DirectMapped(int size, int shift) {
			tag = new int[size];
			valid = new boolean[size];
			data = new int[size];
			this.shift = shift;
		}
		
		int read(int addr, int val) {
			++rdCnt;
			int line = (addr>>>shift)%tag.length;
			if (tag[line]==addr && valid[line]) {
				++hitCnt;
			} else {
				tag[line] = addr;
				data[line] = val;
				valid[line] = true;
			}
			return data[line];
		}

		@Override
		void inval() {
			for (int i=0; i<valid.length; ++i) {
				valid[i] = false;
			}
		}
	}
	
	/**
	 * A fully associative cache with line size of one word.
	 */
	static class LRU extends Cache {
		
		public LRU(int size) {
			tag = new int[size];
			valid = new boolean[size];
			data = new int[size];
		}
		
		int read(int addr, int val) {
			++rdCnt;
			int i=0;
			int len = tag.length;
			boolean hit = false;
			for (i=0; i<len; ++i) {
				if (tag[i]==addr && valid[i]) {
					hit = true;
					++hitCnt;
					break;
				}
			}
			if (hit) {
				val = data[i];
			}
			for (i=len-1; i>0; --i) {
				data[i] = data[i-1];
				tag[i] = tag[i-1];
				valid[i] = valid[i-1];
			}
			data[0] = val;
			tag[0] = addr;
			valid[0] = true;
			return data[0];
		}

		@Override
		void inval() {
			for (int i=0; i<valid.length; ++i) {
				valid[i] = false;
			}
		}
	}

//	DirectMapped hcache = new DirectMapped(32, 3);
	final static int CNT = 10;
	Cache hdmcache[] = new Cache[CNT];
	Cache hcache[] = new Cache[CNT];
	Cache alencache[] = new Cache[CNT];
	Cache mvbcache[] = new Cache[CNT];
	Cache cpcache[] = new Cache[CNT];
	Cache mtabcache[] = new Cache[CNT];
	Cache fieldcache[] = new Cache[CNT];
	Cache staticcache[] = new Cache[CNT];
	Cache arraycache[] = new Cache[CNT];

	DCacheSim(String fn, IOSimMin ioSim, int max) {
		super(fn, ioSim, max);
		for (int i=0; i<CNT; ++i) {
			hdmcache[i] = new DirectMapped(1<<i, 3);
			hcache[i] = new LRU(1<<i);
			alencache[i] = new LRU(1<<i);
			mvbcache[i] = new LRU(1<<i);
			cpcache[i] = new DirectMapped(1<<i, 0);
			mtabcache[i] = new DirectMapped(1<<i, 0);
			fieldcache[i] = new LRU(1<<i);
			staticcache[i] = new DirectMapped(1<<i, 0);
			arraycache[i] = new DirectMapped(1<<i, 0);
		}
	}

	int readMem(int addr, Access type) {

		int data = super.readMem(addr, type);
		for (int i=0; i<CNT; ++i) {
			switch (type) {
			case HANDLE:
				data = hcache[i].read(addr, data);				
				data = hdmcache[i].read(addr, data);				
				break;
			case ALEN:
				data = alencache[i].read(addr, data);
				break;
			case MVB:
				data = mvbcache[i].read(addr, data);
				break;
			case CONST:
				data = cpcache[i].read(addr, data);
				break;
			case MTAB:
				data = mtabcache[i].read(addr, data);
				break;
			case FIELD:
				// TODO: crashes when reading from cache
				// data = 
				fieldcache[i].read(addr, data);
				break;
			case STATIC:
				// TODO: crashes when reading from cache
				// data = 
				staticcache[i].read(addr, data);
				break;
			case ARRAY:
				// TODO: crashes when reading from cache
				// data = 
				arraycache[i].read(addr, data);
				break;
			case CLINFO:
			case IFTAB:
			case INTERN:
				break;
			default:
				break;
			}			
		}
		return data;

	}

	// do we write allocate?
	void writeMem(int addr, int data, Access type) {
		super.writeMem(addr, data, type);
	}

	void invalCache() {
		for (int i=0; i<CNT; ++i) {
			fieldcache[i].inval();
			staticcache[i].inval();
			arraycache[i].inval();
		}
	}

	void stat() {
		super.stat();
		System.out.println("Cache statistics");
		System.out.println("Handle (DM):");
		for (int i=0; i<CNT; ++i) {
			System.out.println(hdmcache[i]);				
		}
		System.out.println("Handle (LRU):");
		for (int i=0; i<CNT; ++i) {
			System.out.println(hcache[i]);				
		}
		System.out.println("Array length (LRU):");
		for (int i=0; i<CNT; ++i) {
			System.out.println(alencache[i]);				
		}
		System.out.println("Method vector base (LRU):");
		for (int i=0; i<CNT; ++i) {
			System.out.println(mvbcache[i]);				
		}
		System.out.println("Constant pool (DM):");
		for (int i=0; i<CNT; ++i) {
			System.out.println(cpcache[i]);				
		}
		System.out.println("Method table (DM):");
		for (int i=0; i<CNT; ++i) {
			System.out.println(mtabcache[i]);				
		}
		System.out.println("Field (LRU):");
		for (int i=0; i<CNT; ++i) {
			System.out.println(fieldcache[i]);				
		}
		System.out.println("Static (DM):");
		for (int i=0; i<CNT; ++i) {
			System.out.println(staticcache[i]);				
		}
		System.out.println("Array (DM):");
		for (int i=0; i<CNT; ++i) {
			System.out.println(arraycache[i]);				
		}
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {

		IOSimMin io;

		int maxInstr = getArgs(args);

		for (int i = 0; i < nrCpus; ++i) {
			io = new IOSimMin();
			io.setCpuId(i);
			js[i] = new DCacheSim(args[0], io, maxInstr);
			io.setJopSimRef(js[i]);
		}

		runSimulation();
	}

}
