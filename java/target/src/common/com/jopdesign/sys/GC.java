/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)

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


package com.jopdesign.sys;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import joprt.RtThread;
import joprt.SwEvent;

/**
 *     Real-time garbage collection for JOP
 *     
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class GC {
	
	static int mem_start;		// read from memory
	// get a effective heap size with fixed handle count
	// for our RT-GC tests
	static int full_heap_size;
	
	// Used in newObject and newArray to locate the object/array
	private static final int HEADER_SIZE = 4;

	/**
	 * Fields in the handle structure.
	 * 
	 * WARNING: Don't change the size as long
	 * as we do conservative stack scanning.
	 */
	static final int HANDLE_SIZE = 8;

	/**
	 * The handle contains following data:
	 * 0 pointer to the object in the heap or 0 when the handle is free
	 * 1 pointer to the method table or length of an array
	 * 2 size - could be in class info
	 * 3 type info: object, primitve array or ref array
	 * 4 pointer to next handle of same type (used or free)
	 * 5 gray list
	 * 6 space marker - either toSpace or fromSpace
	 * 7 pointer to object's lock
	 *
	 * !!! be carefule when changing the handle structure, it's
	 * used in System.arraycopy() and probably in jvm.asm!!!
	 */
	public static final int OFF_PTR = 0;
	public static final int OFF_MTAB_ALEN = 1;
	public static final int OFF_SIZE = 2;
	public static final int OFF_TYPE = 3;
	
	// size != array length (think about long/double)
	
	// use array types 4..11 are standard boolean to long
	// our addition:
	// 1 reference
	// 0 a plain object
	public static final int IS_OBJ = 0;
	public static final int IS_REFARR = 1;
	
	/**
	 * Free and Use list.
	 */
	static final int OFF_NEXT = 4;
	/**
	 * Threading the gray list. End of list is 'special' value -1.
	 * 0 means not in list.
	 */
	static final int OFF_GREY = 5;
	/**
	 * Special end of list marker -1
	 */
	static final int GREY_END = -1;
	/**
	 * Denote in which space the object is
	 */
	static final int OFF_SPACE = 6;

	/**
	 * Field that points to the object's lock
	 */
	static final int OFF_LOCK = 7;

	/**
	 * Trigger GC when free memory is below this margin
	 */
	static final int GC_MARGIN = 256;
	/**
	 * Whether to use a double barrier or to allocate new objects
	 * anthracite
	 */
	static final boolean DOUBLE_BARRIER = false;

		
	static final int TYPICAL_OBJ_SIZE = 5;
	static int handle_cnt;
	/**
	 * Size of one semi-space, complete heap is two times
	 * the semi_size
	 */
	static int semi_size;
	
	
	static int heapStartA, heapStartB;
	static boolean useA;

	static int fromSpace;
	static int toSpace;
	/**
	 * Points to the start of the to-space after
	 * a flip. Objects are copied to copyPtr and
	 * copyPtr is incremented.
	 */
	static int copyPtr;
	/**
	 * Points to the end of the to-space after
	 * a flip. Objects are allocated from the end
	 * and allocPtr gets decremented.
	 */
	static int allocPtr;
	
	static int freeList;
	static int useList;
	static int grayList;
	
	static int addrStaticRefs;
	
	static Object mutex;
	
	static OutOfMemoryError OOMError;

	// Memory allocation pointer used before we enter the ImmortalMemory 
	static int allocationPointer;

	static SysDevice sys = IOFactory.getFactory().getSysDevice();

	/**
	 * Provide static bounds for WCET analysis
	 */
	static final int MAX_CPUS = 8;
	static final int MAX_THREADS = 20;
	static final int MAX_STATIC_REFS = 64;
	static final int MAX_HEAP_SIZE = (1024-100)*1024/4;
	static final int MAX_HANDLE_CNT = MAX_HEAP_SIZE/(2*TYPICAL_OBJ_SIZE+HANDLE_SIZE);
	static final int MAX_SEMI_SIZE = (MAX_HEAP_SIZE-MAX_HANDLE_CNT*HANDLE_SIZE)/2;

	static void checkLimits() {
		if (Native.rd(Const.IO_CPUCNT) > MAX_CPUS) {
			log("warning: real number of cores exceeds assumed number of cores");
			log("real: ", Native.rd(Const.IO_CPUCNT));
			log("max: ", MAX_CPUS);
		}
		if (Native.rdMem(addrStaticRefs+1) > MAX_STATIC_REFS) {
			log("warning: real number of static refs exceeds assumed number of static refs");
			log("real: ", Native.rdMem(addrStaticRefs+1));
			log("max: ", MAX_STATIC_REFS);
		}
		if (full_heap_size > MAX_HEAP_SIZE) {
			log("warning: real heap size exceeds assumed maximum heap size");
			log("real: ", full_heap_size);
			log("max: ", MAX_HEAP_SIZE);
		}
		if (handle_cnt > MAX_HANDLE_CNT) {
			log("warning: real number of handles exceeds assumed maximum number of handles");
			log("real: ", handle_cnt);
			log("max: ", MAX_HANDLE_CNT);
		}
		if (semi_size > MAX_SEMI_SIZE) {
			log("warning: real semi space size exceeds assumed maximum semi space size");
			log("real: ", semi_size);
			log("max: ", MAX_SEMI_SIZE);
		}
	}

	static void init(int mem_size, int addr) {
		
		addrStaticRefs = addr;
		mem_start = Native.rdMem(0);
		// align mem_start to 8 word boundary for the
		// conservative handle check		
		mem_start = (mem_start+7)&0xfffffff8;
		if(Config.USE_SCOPES) {
			allocationPointer = mem_start;
			// clean immortal memory
			for (int i=mem_start; i<mem_size; ++i) {
				Native.wrMem(0, i);
			}
			// Create the Scope that represents immortal memory
			RtThreadImpl.initArea = Memory.getImmortal(mem_start, mem_size-1);
		} else {
			full_heap_size = mem_size-mem_start;
			handle_cnt = full_heap_size/(2*TYPICAL_OBJ_SIZE+HANDLE_SIZE);
			semi_size = (full_heap_size-handle_cnt*HANDLE_SIZE)/2;
			
			checkLimits();

			heapStartA = mem_start+handle_cnt*HANDLE_SIZE;
			heapStartB = heapStartA+semi_size;
			
			//              log("");
			//              log("memory size", mem_size);
			//              log("handle start ", mem_start);
			//              log("heap start (toSpace)", heapStartA);
			//              log("fromSpace", heapStartB);
			//              log("heap size (bytes)", semi_size*4*2);
			
			useA = true;
			copyPtr = heapStartA;
			allocPtr = copyPtr+semi_size;
			toSpace = heapStartA;
			fromSpace = heapStartB;
            
			freeList = 0;
			useList = 0;
			grayList = GREY_END;
			for (int i=0; i<handle_cnt; ++i) {
				int ref = mem_start+i*HANDLE_SIZE;
				// pointer to former freelist head
				Native.wrMem(freeList, ref+OFF_NEXT);
				// mark handle as free
				Native.wrMem(0, ref+OFF_PTR);
				freeList = ref;
				Native.wrMem(0, ref+OFF_GREY);
				Native.wrMem(0, ref+OFF_SPACE);
				Native.wrMem(0, ref+OFF_LOCK);
			}
			// clean the heap
			int end = heapStartA+2*semi_size;
			for (int i=heapStartA; i<end; ++i) {
				Native.wrMem(0, i);
			}
			concurrentGc = false;
		}
		
		// allocate the monitor
		mutex = new Object();

		OOMError = new OutOfMemoryError();
	}
	
	public static Object getMutex() {
		return mutex;
	}
	
	/**
	 * Add object to the gray list/stack
	 * @param ref
	 */
	static void push(int ref) {

		// null pointer check is in the following handle check
		
		// Only objects that are referenced by a handle in the
		// handle area are considered for GC.
		// Null pointer and references to static strings are not
		// investigated.
		if (ref<mem_start || ref>=mem_start+handle_cnt*HANDLE_SIZE) {
			return;
		}
		// does the reference point to a handle start?
		// happens in concurrent
		if ((ref&0x7)!=0) {
			return;
		}

		synchronized (mutex) {
			// Is this handle on the free list?
			// Is possible when using conservative stack scanning
			if (Native.rdMem(ref+OFF_PTR)==0) {
				// that happens in concurrent!
				return;
			}
						
			// Is it black?
			// Can happen from a left over from the last GC cycle, can it?
			// -- it's checked in the write barrier
			// -- but not in mark....
			if (Native.rdMem(ref+OFF_SPACE)==toSpace) {
				return;
			}
			
			// only objects not already in the gray list
			// are added
			if (Native.rdMem(ref+OFF_GREY)==0) {
				// pointer to former gray list head
				Native.wrMem(grayList, ref+OFF_GREY);
				grayList = ref;			
			}			
		}		
	}
	/**
	 * switch from-space and to-space
	 */
	static void flip() {
		synchronized (mutex) {
			// if (grayList!=GREY_END) log("GC: gray list not empty");

			useA = !useA;
			if (useA) {
				copyPtr = heapStartA;
				fromSpace = heapStartB;
				toSpace = heapStartA;
			} else {
				copyPtr = heapStartB;			
				fromSpace = heapStartA;
				toSpace = heapStartB;
			}
			allocPtr = copyPtr+semi_size;
		}
	}

	/**
	 * Scan all thread stacks atomic.
	 *
	 */
	static void getStackRoots() {
		int i, j, k, cnt, cpus;
		Scheduler sched;
		RtThreadImpl [] ref;
		
		if (concurrentGc) {
			cpus = sys.nrCpu;
			for (i = cpus-1; i >= 0; --i) { // @WCA loop <= MAX_CPUS
				// we fire the scanner event for this CPU last, so we do
				// not delay the start on other CPUs
				if (i == sys.cpuId)
					continue;
				sched = Scheduler.sched[i];
				if (sched.scanner != null) {
					ref = sched.ref;
					cnt = ref.length;
					for (j = 0; j < cnt; j++) { // @WCA loop <= MAX_THREADS outer
						ref[j].scan = true;
					}
					sched.scanner.fire();
				} else {
					throw OOMError;
				}
			}

			// fire event for current CPU
			i = sys.cpuId;
			sched = Scheduler.sched[i];
			ref = sched.ref;
			if (sched.scanner != null) {					
				cnt = ref.length;
				for (j = 0; j < cnt; j++) { // @WCA loop <= MAX_THREADS
					ref[j].scan = true;
				}
				sched.scanner.fire();
			} else {
				throw OOMError;
			}
			
			// wait for everyone to finish root scanning
			for (i = 0; i < cpus; i++) { // @WCA loop <= MAX_CPUS
				sched = Scheduler.sched[i];
				ref = sched.ref;
				cnt = ref.length;
				for (j = 0; j < cnt; j++) { // @WCA loop <= MAX_THREADS outer
					while (ref[j].scan) { // @WCA loop <= 1
						/* wait for root scanning threads to do the work */
					}
				}
			}
		} else {
			// add stack of the current thread to the root list
			ScanEvent.getOwnStackRoots();

			// add stacks of all other threads to the root list
 			cpus = sys.nrCpu;
 			for (i = 0; i < cpus; i++) { // @WCA loop <= MAX_CPUS
				ref = Scheduler.sched[i].ref;
 				if (ref != null) {
					cnt = ref.length;
					for (j = 0; j < cnt; j++) { // @WCA loop <= MAX_THREADS outer
						synchronized(mutex) {						
							int[] mem = ref[j].stack;
							// sp starts at Const.STACK_OFF
							int sp = ref[j].sp - Const.STACK_OFF;
							for (k = 0; k <= sp; ++k) {
								push(mem[k]);
							}
						}
 					}
  				}
 			}
		}

	}

	/**
	 * Scan all static fields
	 *
	 */
	private static void getStaticRoots() {
		int i;
		// add static refs to root list
		int addr = Native.rdMem(addrStaticRefs);
		int cnt = Native.rdMem(addrStaticRefs+1);
		for (i=0; i<cnt; ++i) { // @WCA loop <= MAX_STATIC_REFS
			push(Native.rdMem(addr+i));
		}
	}
	
	static void markAndCopy() {
		
		int i, ref;

		Object mtx = mutex;

		// log("stack");
		getStackRoots();			
		// log("static");
		getStaticRoots();
		// log("trace");
		for (;;) {

			// pop one object from the gray list
			synchronized (mtx) { // @WCA loop <= MAX_HANDLE_CNT
				ref = grayList;
				if (ref==GREY_END) { 
					break;
				}
				grayList = Native.rdMem(ref+OFF_GREY);
				Native.wrMem(0, ref+OFF_GREY);		// mark as not in list
			}

			// push all children
				
			// get pointer to object
			int addr = Native.rdMem(ref);
			int flags = Native.rdMem(ref+OFF_TYPE);
			if (flags==IS_REFARR) {
				// is an array of references
				int size = Native.rdMem(ref+OFF_MTAB_ALEN);
				for (i=0; i<size; ++i) { // @WCA loop <= MAX_SEMI_SIZE outer
					push(Native.rdMem(addr+i));
				}
				// However, multianewarray does probably NOT work
			} else if (flags==IS_OBJ){
				// it's a plain object				
				// get pointer to method table
				flags = Native.rdMem(ref+OFF_MTAB_ALEN);
				// get real flags
				flags = Native.rdMem(flags+Const.MTAB2GC_INFO);
				for (i=0; flags!=0; ++i) {
					// @WCA loop <= 32
					// @WCA loop <= MAX_SEMI_SIZE outer
					if ((flags&1)!=0) {
						push(Native.rdMem(addr+i));
					}
					flags >>>= 1;
				}				
			}

			// already moved, happens because of
			// - anthracite objects
			// - objects pushed during scanning
			if (Native.rdMem(ref+OFF_SPACE)==toSpace) {
				continue;
			}			
			
			// now copy it - color it BLACK			
			int size;
			int dest;

			synchronized(mtx) {
				size = Native.rdMem(ref+OFF_SIZE);
				dest = copyPtr;
				copyPtr += size;			

				// set it BLACK
				Native.wrMem(toSpace, ref+OFF_SPACE);

				// TODO: lock guards against int2ext and ext2int, should be eliminated
				Native.lock();

				if (size>0) {
					// copy it
					Native.wr(addr, Const.IO_CCCP_SRC);
					Native.wr(dest, Const.IO_CCCP_DST);
					Native.wr(1, Const.IO_CCCP_ACT);
					for (i = 0; i < size; i++) { // @WCA loop <= MAX_SEMI_SIZE outer
						Native.wr(i, Const.IO_CCCP_POS);
					}
				}

				// update object pointer to the new location
				Native.wrMem(dest, ref+OFF_PTR);

				Native.unlock();

				if (size>0) {
					// wait until everybody uses the new location
					for (i = 0; i < 10; i++); // @WCA loop = 10
					// turn off address translation
					Native.wr(0, Const.IO_CCCP_ACT);
				}
			}
		}
	}
	
	/**
	 * Sweep through the 'old' use list and move garbage to free list.
	 */
	static void sweepHandles() {

		int ref;
		Object mtx = mutex;
	
		synchronized (mtx) {
			ref = useList;		// get start of the list
			useList = 0;		// new uselist starts empty
		}
		
		while (ref!=0) { // @WCA loop <= MAX_HANDLE_CNT
			// read next element, as it is destroyed
			// by addTo*List()
			int next = Native.rdMem(ref+OFF_NEXT);
			synchronized (mtx) {
				// a BLACK one
				if (Native.rdMem(ref+OFF_SPACE)==toSpace) {
					// add to used list
					Native.wrMem(useList, ref+OFF_NEXT);
					useList = ref;					
				// a WHITE one
				} else {
					// pointer to former freelist head
					Native.wrMem(freeList, ref+OFF_NEXT);
					freeList = ref;					
					// mark handle as free
					Native.wrMem(0, ref+OFF_PTR);
				}		
			}
			ref = next;
		}
	}

	/**
	 * Clean the from-space 
	 */
	static void zapSemi() {

		// clean the from-space to prepare for the next
		// flip
		int end = fromSpace+semi_size;
		for (int i=fromSpace; i<end; ++i) { // @WCA loop <= MAX_SEMI_SIZE
			Native.wrMem(0, i);
		}
	}

	public static void triggerGc() {

		log("GC triggered on CPU", sys.cpuId);

		// scopes and GC cannot be mixed
		if (Config.USE_SCOPES) {
			log("No GC when scopes are used");
			System.exit(1);
		}

		// nasty things would happen if we allowed this
		if (concurrentGc) {
			// OOMError.fillInStackTrace();
			throw OOMError;
		}
				
		if (sys.nrCpu <= 1 || sys.signal == 0) {
			// stop-the world GC on (de facto) uniprocessor
			gc();
		} else {
			// only trigger if not running already
			if (!gcRunning) {
				gcRunning = true;
				gcRunnerId = sys.cpuId;
				
				// start GC events on all CPUs
				int cpus = sys.nrCpu;
				for (int i = cpus-1; i >= 0; --i) { // @WCA loop <= MAX_CPUS
					if (Scheduler.sched[i].collector != null) {					
						// log("Fire event on CPU", i);
						Scheduler.sched[i].collector.fire();
					} else if (Startup.cpuStart[i] != null) {
						log("Stop-the-world GC on CMP needs GC events");
						System.exit(1);
					}
				}
			}
		}
	}

	public static void gc() {
//  		log("GC called - free memory:", freeMemory());

		// log("start GC");

		// log("flip");
		flip();
		// log("m&c");
		markAndCopy();
		// log("sweep");
		sweepHandles();
		// log("zap");
		zapSemi();	

		// log("end GC");

//  		log("GC end - free memory:",freeMemory());
	}
	
	static int free() {
		return allocPtr-copyPtr;
	}
	
	/**
	 * Size of scratchpad memory in 32-bit words
	 * @return
	 */
	public static int getScratchpadSize() {
		return Startup.spm_size;
	}

	/**
	 * Allocate a new Object. Invoked from JVM.f_new(cons);
	 * @param cons pointer to class struct
	 * @return address of the handle
	 */
	static int newObject(int cons) {

		int size = Native.rdMem(cons);			// instance size
		
		if (Config.USE_SCOPES) {
			// allocate in scope
			int ptr = allocationPointer;
			if(RtThreadImpl.initArea == null) {
				allocationPointer += size+HEADER_SIZE;
			} else {
				Memory sc = null;
				if (RtThreadImpl.mission) {
					Scheduler s = Scheduler.sched[RtThreadImpl.sys.cpuId];
					sc = s.ref[s.active].currentArea;                       
				} else {
					sc = RtThreadImpl.initArea;
				}
				if (sc.allocPtr+size+HEADER_SIZE > sc.endLocalPtr) {
					// OOMError.fillInStackTrace();
					throw OOMError;
				}
				sc.allocPtr += size+HEADER_SIZE;
				
				//Add scope info to pointer of newly created object
				if (Config.ADD_REF_INFO) {
					ptr = ptr | (sc.level << 25);   
				}
				//Add scope info to object's handler field
				// Native.wrMem(sc.level, ptr+OFF_SCOPE);
			}
			Native.wrMem(ptr+HEADER_SIZE, ptr+OFF_PTR);
			Native.wrMem(size, ptr+OFF_SIZE); // Just defining all headers
			Native.wrMem(cons+Const.CLASS_HEADR, ptr+OFF_MTAB_ALEN);
			Native.wrMem(0, ptr+OFF_TYPE);
			// TODO: memory initialization is needed
			// either on scope creation+exit or in new
			return ptr;             
		}

		synchronized (mutex) {
			if (copyPtr+size+GC_MARGIN >= allocPtr || freeList==0) {
				if (!concurrentGc) {
					log("Run out of memory on CPU", sys.cpuId);
					// that's the stop-the-world GC
					triggerGc();
				} else {
					// concurrent GC could not keep up
					throw OOMError;
				}
			}			
		}
		
		while (gcRunning) {
			// wait for the GC to finish
		}

		int ref;
		
		synchronized (mutex) {
			if (copyPtr+size >= allocPtr || freeList==0) {
				// make sure we actually freed enough memory
				// OOMError.fillInStackTrace();
				throw OOMError;
			}
			// we allocate from the upper part
			allocPtr -= size;
			// get one from free list
			ref = freeList;
			freeList = Native.rdMem(ref+OFF_NEXT);
			// and add it to use list
			Native.wrMem(useList, ref+OFF_NEXT);
			useList = ref;
			// pointer to real object, also marks it as non free
			Native.wrMem(allocPtr, ref); // +OFF_PTR
			// should be from the class info
			Native.wrMem(size, ref+OFF_SIZE);
			// mark it as BLACK - means it will be in toSpace
			Native.wrMem(toSpace, ref+OFF_SPACE);
			// TODO: should not be necessary - now just for sure
			// Native.wrMem(0, ref+OFF_GREY);
			// BTW: when we create mutex we synchronize on the not yet
			// created Object!
			// ref. flags used for array marker
			Native.wrMem(IS_OBJ, ref+OFF_TYPE);
			// pointer to method table in the handle
			Native.wrMem(cons+Const.CLASS_HEADR, ref+OFF_MTAB_ALEN);
			// TODO: should not be necessary - now just for sure
			// Native.wrMem(0, ref+OFF_LOCK);

			if (!DOUBLE_BARRIER) {
				// allocate anthracite
				Native.wrMem(GC.grayList, ref+GC.OFF_GREY);
				GC.grayList = ref;
			}
		}

		return ref;
	}
	
	static int newArray(int size, int type) {
		
		if (size < 0) {
			throw new NegativeArraySizeException();
		}

		int arrayLength = size;
		
		// long or double array
		if((type==11)||(type==7)) size <<= 1;
		// reference array type is 1 (our convention)
		
		if (Config.USE_SCOPES) {
			// allocate in scope
			int ptr = allocationPointer;
			if(RtThreadImpl.initArea == null) {
				allocationPointer += size+HEADER_SIZE;
			} else {
				Memory sc = null;
				if (RtThreadImpl.mission) {
					Scheduler s = Scheduler.sched[RtThreadImpl.sys.cpuId];
					sc = s.ref[s.active].currentArea;                       
				} else {
					sc = RtThreadImpl.initArea;
				}
				if (sc.allocPtr+size+HEADER_SIZE > sc.endLocalPtr) {
					// OOMError.fillInStackTrace();
					throw OOMError;
				}
				sc.allocPtr += size+HEADER_SIZE;
				
				//Add scope info to pointer of newly created object
				if (Config.ADD_REF_INFO) {
					ptr = ptr | (sc.level << 25);   
				}
				//Add scope info to object's handler field
				// Native.wrMem(sc.level, ptr+OFF_SCOPE);
			}
			Native.wrMem(ptr+HEADER_SIZE, ptr+OFF_PTR);
			Native.wrMem(size, ptr+OFF_SIZE); // Just defining all headers
			Native.wrMem(arrayLength, ptr+OFF_MTAB_ALEN);
			Native.wrMem(type, ptr+OFF_TYPE);
			// TODO: memory initialization is needed
			// either on scope creation+exit or in new
			return ptr;             
		}

		synchronized (mutex) {
			if (copyPtr+size+GC_MARGIN >= allocPtr || freeList==0) {
				if (!concurrentGc) {
					log("Run out of memory on CPU", sys.cpuId);
					// that's the stop-the-world GC
					triggerGc();
				} else {
					// concurrent GC could not keep up
					throw OOMError;
				}
			}			
		}

		while (gcRunning) {
			// wait for the GC to finish
		}

		int ref;
		
		synchronized (mutex) {
			if (copyPtr+size >= allocPtr || freeList==0) {
				// make sure we actually freed enough memory
				// OOMError.fillInStackTrace();
				throw OOMError;
			}

			// we allocate from the upper part
			allocPtr -= size;
			// get one from free list
			ref = freeList;
			freeList = Native.rdMem(ref+OFF_NEXT);
			// and add it to use list
			Native.wrMem(useList, ref+OFF_NEXT);
			useList = ref;
			// pointer to real object, also marks it as non free
			Native.wrMem(allocPtr, ref); // +OFF_PTR
			// should be from the class info
			Native.wrMem(size, ref+OFF_SIZE);
			// mark it as BLACK - means it will be in toSpace
			Native.wrMem(toSpace, ref+OFF_SPACE);
			// TODO: should not be necessary - now just for sure
			// Native.wrMem(0, ref+OFF_GREY);
			// ref. flags used for array marker
			Native.wrMem(type, ref+OFF_TYPE);
			// array length in the handle
			Native.wrMem(arrayLength, ref+OFF_MTAB_ALEN);
			// TODO: should not be necessary - now just for sure
			// Native.wrMem(0, ref+OFF_LOCK);

			if (!DOUBLE_BARRIER) {
				// allocate anthracite
				Native.wrMem(GC.grayList, ref+GC.OFF_GREY);
				GC.grayList = ref;
			}
		}

		return ref;		
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
		return semi_size*4;
	}
	  
/************************************************************************************************/	

	static void log(String s, int i) {
		JVMHelp.wr(s);
		JVMHelp.wr(" ");
		JVMHelp.wrSmall(i);
		JVMHelp.wr("\n");
	}
	static void log(String s) {
		JVMHelp.wr(s);
		JVMHelp.wr("\n");
	}

/************************************************************************************************/	

	static boolean concurrentGc = false;

	public static void setConcurrent() {
		concurrentGc = true;
	}

	static volatile boolean gcRunning;
	static volatile int gcRunnerId;
	
	public static final class GCThread extends RtThread {
		
		public GCThread(int prio, int period) {
			super(prio, period);
		}
		public void run() {
			for (;;) {
				// log("G");
				// GC.log("<");
				// System.out.println(System.nanoTime());
				GC.gc();
				// GC.log(">");
				waitForNextPeriod();
			}
		}
	}

	public static final class STWGCEvent extends SwEvent {
		
		volatile boolean handshake;

		public STWGCEvent(int prio, int minTime) {
			this(prio, minTime, GC.sys.cpuId);
		}

		public STWGCEvent(int prio, int minTime, int cpu) {
			super(prio, minTime);
			setProcessor(cpu);
		}

		public void handle() {
			SysDevice sys = IOFactory.getFactory().getSysDevice();

			// synchronized(GC.mutex) {
			//    	GC.log("Handling GC event on CPU", sys.cpuId);
			// }

			handshake = true;
			if (GC.sys.cpuId == GC.gcRunnerId) {

				// synchronized(GC.mutex) {
				//    	GC.log("Waiting for handshakes");
				// }

				// wait for handshake
				int cpus = GC.sys.nrCpu;
				for (int i = cpus-1; i >= 0; --i) { // @WCA loop <= MAX_CPUS
					while (!Scheduler.sched[i].collector.handshake) { // @WCA loop <= 1
						/* wait for handshake from other CPUs */
					}
				}

				// synchronized(GC.mutex) {				
				// 	GC.log("Start STWGC");
				// }

				// the real stuff
				GC.gc();

				// synchronized(GC.mutex) {				
				// 	GC.log("Finished STWGC");
				// }

				// clear handshakes for future
				for (int i = cpus-1; i >= 0; --i) {
					Scheduler.sched[i].collector.handshake = false;
				}

				// let other CPUs continue again
				GC.gcRunning = false;
			} else {
				while (GC.gcRunning) {
					// wait for the GC to finish
				}
				// synchronized(GC.mutex) {
				//   	GC.log("Seen GC finish on CPU", sys.cpuId);
				// }
			}
		}
	}

	public static final class ScanEvent extends SwEvent {

		public static void getOwnStackRoots() {
			int	i, j;

			i = Native.getSP();			
			for (j = Const.STACK_OFF; j <= i; ++j) {
				push(Native.rdIntMem(j));
			}
			Scheduler sched = Scheduler.sched[GC.sys.cpuId];
			if (sched.ref != null) {
				sched.ref[sched.active].scan = false;
			}
		}

		public ScanEvent(int prio, int minTime) {
			this(prio, minTime, GC.sys.cpuId);
		}

		public ScanEvent(int prio, int minTime, int cpu) {
			super(prio, minTime);
			Scheduler.sched[cpu].scanner = this;
			Scheduler.sched[cpu].scanThres = prio+RtThreadImpl.MAX_PRIORITY+RtThreadImpl.RT_BASE;
			setProcessor(cpu);
		}

		public void handle() {
		
			int i, j;

			SysDevice sys = IOFactory.getFactory().getSysDevice();
			// synchronized (mutex) {
			// 	GC.log("Handling scan event on CPU", sys.cpuId);
			// }

			Scheduler sched = Scheduler.sched[GC.sys.cpuId];
			int cnt = sched.ref.length;

			for (i = 0; i < cnt; i++) {

				RtThreadImpl ref = sched.ref[i];

				if (ref.scan && ref.priority < sched.scanThres) {
						
					// threads cannot execute while we scan their
					// stacks, because we run at a higher priority

					int[] mem = ref.stack;
					// sp starts at Const.STACK_OFF
					int sp = ref.sp - Const.STACK_OFF;
					for (j = 0; j <= sp; ++j) {
						push(mem[j]);
					}
						
					ref.scan = false;
				}
			}

			// our own stack is empty
			sched.ref[sched.active].scan = false;
		}

	}

}
