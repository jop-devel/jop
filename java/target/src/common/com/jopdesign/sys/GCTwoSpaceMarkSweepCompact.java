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

/*
 * Created on 16.06.2005
 *
 */
package com.jopdesign.sys;


/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class GCTwoSpaceMarkSweepCompact {
	
	static int mem_start;		// read from memory
	static final int MEM_SIZE = 30000;
//	static final int MEM_SIZE = 256000; // in words (262144)
	static int full_heap_size;
	/**
	 * The handle contains following data:
	 * 0 pointer to the object in the heap
	 * 1 pointer to the method table or 0 when the handle is free
	 * 2 size - could be in class info
	 *   + GC mark (could be part of the object pointer)
	 * 3 flags to mark reference fields - could be in class info
	 *   -1 means more than 31 fields -> look in class info....
	 * 4 pointer to next handle of same type (used or free)
	 */
	static final int HANDLE_SIZE = 5;
	static final int OFF_PTR = 0;
	static final int OFF_MTAB = 1;
	static final int OFF_SIZE = 2;
	static final int OFF_TYPE = 3;
	
	static final int IS_OBJ = 0;
	static final int IS_REFARR = 1;
	static final int IS_VALARR = 2;
	
	static final int OFF_NEXT = 4;
	static final int TYPICAL_OBJ_SIZE = 10;
	static int handle_cnt;
	static int heap_size;
	
	
	static int stackStart;
	static int heapStartA, heapStartB;
	static boolean useA;
	static int heapEnd;
	static int heapPtr;
	
	static int freeList;
	static int useList;
	
	static int addrStaticRefs;
	
	static Object monitor;

	static void init(int addr) {
		
		addrStaticRefs = addr;
		
		mem_start = Native.rdMem(0);
		full_heap_size = MEM_SIZE-mem_start;
		handle_cnt = full_heap_size/2/(TYPICAL_OBJ_SIZE+HANDLE_SIZE);
//		handle_cnt = 200;
		heap_size = (full_heap_size-handle_cnt*HANDLE_SIZE)/2;
		
		heapStartA = mem_start+handle_cnt*HANDLE_SIZE;
		heapStartB = heapStartA+heap_size;
		
// we canot call System.out here!
// System.out.println("Heap: "+heapStartA+" "+heapStartB+" "+(heapStartB+heap_size));
// System.out.println("Size: "+heap_size+" words");
		useA = true;
		heapPtr = heapStartA;
		stackStart = heapStartB;
		heapEnd = heapPtr+heap_size;
		
		freeList = 0;
		useList = 0;
		sp = 0;
		for (int i=0; i<handle_cnt; ++i) {
			addToFreeList(mem_start+i*HANDLE_SIZE);
		}
		// clean the heap
		for (int i=heapPtr; i<heapEnd; ++i) {
			Native.wrMem(0, i);
		}

		// allocate the monitor
		monitor = new Object();
	}
	
	static void addToFreeList(int ref) {
		// pointer to former freelist head
		Native.wrMem(freeList, ref+OFF_NEXT);
		// mark handle as free
		Native.wrMem(0, ref+OFF_MTAB);
		freeList = ref;
	}
	static void addToUseList(int ref) {		
		// add to used list
		Native.wrMem(useList, ref+OFF_NEXT);
		useList = ref;
	}
	
	static int getHandle(int ref, int size) {
		int addr = freeList;
		freeList = Native.rdMem(freeList+OFF_NEXT);
		Native.wrMem(ref, addr);
		// mark handle as non free
		// will be class info...
		Native.wrMem(1, addr+OFF_MTAB);
		// should be from the class info
		Native.wrMem(size, addr+OFF_SIZE);
		// add to used list
		addToUseList(addr);
		return addr;
	}
	static int translate(int ref) {
		return Native.rdMem(ref);
	}
	
	static int sp;
	static void push(int ref) {
		
		// if (ref==0) return;		// that's a null pointer
		// Only objects that are referenced by a handle in the
		// handle area are considered for GC.
		// Null pointer and references to static strings are not
		// investigated.
		if (ref<mem_start || ref>=mem_start+handle_cnt*HANDLE_SIZE) return;
		// Is this handle on the free list?
		// Is possible when using conservative stack scanning
		if (Native.rdMem(ref+OFF_MTAB)==0) return;
		
		Native.wrMem(ref, stackStart+sp);
		++sp;
		if (sp==heap_size) {	// do we really need this check???
			// mark stack should be large enough if the same size
			// as the heap
			// However, think about concurrent allocation space
			// during mark.
			System.out.println("GC stack overflow!");
			System.exit(1);
		}
	}
	static int pop() {

		if (sp==0) {
			return -1;
		}
		--sp;
		return Native.rdMem(stackStart+sp);
	}
	
	static void getRoots() {

		synchronized (monitor) {
			// add static refs to root list
			int addr = Native.rdMem(addrStaticRefs);
			int cnt = Native.rdMem(addrStaticRefs+1);
			for (int i=0; i<cnt; ++i) {
				push(Native.rdMem(addr+i));
			}
			// add complete stack to the root list
			int i = Native.getSP();
			for (int j=Const.STACK_OFF; j<=i; ++j) {
				push(Native.rdIntMem(j));
			}
		}
	}
	
	static void mark() {
		
		int i;
		sp = 0;
		
		getRoots();
		while (sp!=0) {
			int ref = pop();
			int size = Native.rdMem(ref+OFF_SIZE);
			if (size<0) {
				// allready marked
				continue;
			}
			size |= 0x80000000;
			Native.wrMem(size, ref+OFF_SIZE);
			
			// get pointer to object
			int addr = Native.rdMem(ref);
			int flags = Native.rdMem(ref+OFF_TYPE);
			if (flags==IS_VALARR) {
				// is an array
			} else if (flags==IS_REFARR) {
				// is an array of references
				size = Native.rdMem(addr-1);
				for (i=0; i<size; ++i) {
					push(Native.rdMem(addr+i));
				}
				// However, multinewarray does probably NOT work
			} else {		
				// it's a plain object
				
				// get pointer to method table
				flags = Native.rdMem(addr-1);
				// get real flags
				flags = Native.rdMem(flags-2);
				
				for (i=0; flags!=0; ++i) {
					if ((flags|1)!=0) {
						int child = Native.rdMem(addr+i);
						push(child);
					}
					flags >>= 1;
				}				
			}
		}
	}
	
	static void sweep() {
		
		int ref = useList;
		useList = 0;
		while (ref!=0) {
			int size = Native.rdMem(ref+OFF_SIZE);
			// read next element, as it is destroyed
			// by addTo*List()
			int next = Native.rdMem(ref+OFF_NEXT);
			if (size<0) {
				size &= 0x7fffffff;
				Native.wrMem(size, ref+OFF_SIZE);
				addToUseList(ref);
			} else {
				addToFreeList(ref);
			}
			ref = next;
		}
	}
	static void compact() {

		// switch from and to space
		useA = !useA;
		if (useA) {
			heapPtr = heapStartA;
			stackStart = heapStartB;
		} else {
			heapPtr = heapStartB;			
			stackStart = heapStartA;
		}
		heapEnd = heapPtr+heap_size;
		
		int ref = useList;
		while (ref!=0) {
			int addr = Native.rdMem(ref+OFF_PTR);
			--addr; // copy also the mtab pointer or arrays size!
//			System.out.println(ref+" move from "+addr+" to "+heapPtr);
			int size = Native.rdMem(ref+OFF_SIZE);
			for (int i=0; i<size; ++i) {
				int val = Native.rdMem(addr+i);
				Native.wrMem(val, heapPtr+i);
			}
			// update object pointer to the new location
			Native.wrMem(heapPtr+1, ref+OFF_PTR);
			heapPtr += size;
			ref = Native.rdMem(ref+OFF_NEXT);
		}
		// clean rest of the heap
		for (int i=heapPtr; i<heapEnd; ++i) {
			Native.wrMem(0, i);
		}
		// for tests clean also the former from space
		for (int i=stackStart; i<stackStart+heap_size; ++i) {
			Native.wrMem(0, i);
		}
	}
	
	static void gc_alloc() {
		System.out.println();
		System.out.println("GC allocation triggered");
		gc();
	}

	public static void gc() {
		System.out.print("GC called, ");
//		System.out.print(free());
//		System.out.print(" words remaining - now ");
/* disabled for now (BG)
 * TODO: get stack roots from threads!
*/
		mark();
		sweep();
		compact();

		System.out.print(freeMemory());
		System.out.println(" bytes free");
		
	}
	
	static int free() {
		return heapEnd-heapPtr;
	}
	
	/**
	 * Allocate a new Object. Invoked from JVM.f_new(cons);
	 * @param cons pointer to class struct
	 * @return address of the handle
	 */
	static int newObject(int cons) {
		
		int size = Native.rdMem(cons);			// instance size
		// we are NOT using JVM var h at address 2 for the
		// heap pointer anymore.
		++size;		// for the additional method pointer
		
//System.out.println("new "+heapPtr+" size "+size);
		if (heapPtr+size >= heapEnd) {
			gc_alloc();
		}
		if (heapPtr+size >= heapEnd) {
			// still not enough memory
			System.out.println("Out of memory error!");
			System.exit(1);
		}
		if (freeList==0) {
//			System.out.println("Run out of handles!");
			// is this a good place to call gc????
			// better check available handles on newObject
			gc_alloc();
		}
		if (freeList==0) {
			System.out.println("Still out of handles!");
			System.exit(1);
		}

		// we need the object size.
		// in the heap or in the handle structure
		// or retrive it from the class info
		int ref = getHandle(heapPtr+1, size);
		// ref. flags used for array marker
		Native.wrMem(IS_OBJ, ref+OFF_TYPE);
		
		Native.wrMem(cons+3, heapPtr);		// pointer to method table in objectref-1

		// zero could be done on a flip and at initialization!
		for (int i=1; i<size; ++i) {
			Native.wrMem(0, heapPtr+i);		// zero object
		}
		int addr = heapPtr+1;
		heapPtr += size;
/*
 * 
//		if (Startup.started) {
			JVMHelp.wr("new ");
			JVMHelp.wrSmall(addr);
			JVMHelp.wrSmall(ref);
			JVMHelp.wrSmall(cons);
			JVMHelp.wr("\r\n");			
//		}
*/
		return ref;
	}
	
	static int newArray(int size, boolean isRef) {
		
		// we are NOT using JVM var h at address 2 for the
		// heap pointer anymore.
		++size;		// for the additional size field
		
//System.out.println("new "+heapPtr+" size "+size);
		if (heapPtr+size >= heapEnd) {
			gc_alloc();
		}
		if (heapPtr+size >= heapEnd) {
			// still not enough memory
			System.out.println("Out of memory error!");
			System.exit(1);
		}
		if (freeList==0) {
//			System.out.println("Run out of handles!");
			// is this a good place to call gc????
			// better check available handles on newObject
			gc_alloc();
		}
		if (freeList==0) {
			System.out.println("Still out of handles!");
			System.exit(1);
		}

		int ref = getHandle(heapPtr+1, size);
		// ref. flags used for array marker
		if (isRef) {
			Native.wrMem(IS_REFARR, ref+OFF_TYPE);
		} else {
			Native.wrMem(IS_VALARR, ref+OFF_TYPE);
		}

		// we need the array size.
		// in the heap or in the handle structure
		Native.wrMem(size-1, heapPtr);		// pointer to method table in objectref-1

		for (int i=1; i<size; ++i) {
			Native.wrMem(0, heapPtr+i);		// zero array
		}
		heapPtr += size;
		return ref;
		
	}
	
	// not used in JOP
	static int getField(int ref, int off) {
		return Native.rdMem(translate(ref)+off);
	}
	// not used in JOP
	static void setField(int ref, int off, int val) {
		Native.wrMem(val, translate(ref)+off);
	}

	/**
	 * @return
	 */
	public static int freeMemory() {
		return free()*4;
	}

	/**
	 * @return
	 */
	public static int totalMemory() {
		return heap_size*4;
	}
	
}
