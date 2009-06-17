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
	
	/**
	 * A direct mapped cache with line size of one word. Size should be a power of 2.
	 */
	static class DirectMapped {
		
		int tag[], data[];
		boolean valid[];
		int rdCnt;
		int hitCnt;
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
	}
	
	/**
	 * A direct mapped cache with line size of one word. Size should be a power of 2.
	 */
	static class LRU {
		
		int tag[], data[];
		boolean valid[];
		int rdCnt;
		int hitCnt;

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
	}

//	DirectMapped hcache = new DirectMapped(32, 3);
	final static int SIZE = 512;
	LRU hcache = new LRU(SIZE);
	LRU alencache = new LRU(SIZE);
	DirectMapped cpcache = new DirectMapped(SIZE, 0);
	DirectMapped mtabcache = new DirectMapped(SIZE, 0);

	DCacheSim(String fn, IOSimMin ioSim, int max) {
		super(fn, ioSim, max);
	}

	int readMem(int addr, Access type) {

		int data = super.readMem(addr, type);
		// TODO: use access type
		switch (type) {
		case HANDLE:
			data = hcache.read(addr, data);
			break;
		case ALEN:
			data = alencache.read(addr, data);
			break;
		case CONST:
			data = cpcache.read(addr, data);
			break;
		case MTAB:
			data = mtabcache.read(addr, data);
			break;
		default:
			break;
		}
		return data;

	}

	// do we write allocate?
	void writeMem(int addr, int data, Access type) {
		super.writeMem(addr, data, type);
	}

	void stat() {
		super.stat();
		System.out.println("Cache statistics");
		System.out.println("Handle:");
		System.out.printf("\t%3d\t& %10d & %10d & %2d\\%% \\\\%n",
				hcache.tag.length, hcache.rdCnt, hcache.hitCnt, (hcache.hitCnt*1000/hcache.rdCnt+5)/10);
		System.out.println("Array length:");
		System.out.printf("\t%3d\t& %10d & %10d & %2d\\%% \\\\%n",
				alencache.tag.length, alencache.rdCnt, alencache.hitCnt, (alencache.hitCnt*1000/alencache.rdCnt+5)/10);		
		System.out.println("Constant pool:");
		System.out.printf("\t%3d\t& %10d & %10d & %2d\\%% \\\\%n",
				cpcache.tag.length, cpcache.rdCnt, cpcache.hitCnt, (cpcache.hitCnt*1000/cpcache.rdCnt+5)/10);
		System.out.println("Method table:");
		System.out.printf("\t%3d\t& %10d & %10d & %2d\\%% \\\\%n",
				mtabcache.tag.length, mtabcache.rdCnt, mtabcache.hitCnt, (mtabcache.hitCnt*1000/mtabcache.rdCnt+5)/10);
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
