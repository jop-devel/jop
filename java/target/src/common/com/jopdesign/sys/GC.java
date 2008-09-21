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


/**
 *     Real-time garbage collection for JOP
 *     
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class GC {
	
	/**
	 * Use either scoped memories or a GC.
	 * Combining scopes and the GC needs some extra work.
	 */
	final static boolean USE_SCOPES = false;
	
	
	static int mem_start;		// read from memory
	// get a effective heap size with fixed handle count
	// for our RT-GC tests
	static int full_heap_size;
	

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
	// TODO: useList is only used for a faster handle sweep
	// do we need it?
	static int useList;
	static int grayList;
	
	static int addrStaticRefs;
	
	static Object mutex;
	
	static boolean concurrentGc;
		
	static int roots[];

	static OutOfMemoryError OOMError;

	static void init(int mem_size, int addr) {
		
		addrStaticRefs = addr;
		mem_start = Native.rdMem(0);
		// align mem_start to 8 word boundary for the
		// conservative handle check
		
//mem_start = 261300;
		mem_start = (mem_start+7)&0xfffffff8;
//mem_size = mem_start + 2000;
		full_heap_size = mem_size-mem_start;
		handle_cnt = full_heap_size/2/(TYPICAL_OBJ_SIZE+HANDLE_SIZE);
		semi_size = (full_heap_size-handle_cnt*HANDLE_SIZE)/2;
		
		heapStartA = mem_start+handle_cnt*HANDLE_SIZE;
		heapStartB = heapStartA+semi_size;
		
//		log("");
//		log("memory size", mem_size);
//		log("handle start ", mem_start);
//		log("heap start (toSpace)", heapStartA);
//		log("fromSpace", heapStartB);
//		log("heap size (bytes)", semi_size*4*2);
		
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
		}
		// clean the heap
		int end = heapStartA+2*semi_size;
		for (int i=heapStartA; i<end; ++i) {
			Native.wrMem(0, i);
		}
		concurrentGc = false;
		
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
		// TODO: happens in concurrent
		if ((ref&0x7)!=0) {
//				log("a not aligned handle");
			return;
		}

		synchronized (mutex) {
			// Is this handle on the free list?
			// Is possible when using conservative stack scanning
			if (Native.rdMem(ref+OFF_PTR)==0) {
				// TODO: that happens in concurrent!
//				log("push of a handle with 0 at OFF_PRT!", ref);
				return;
			}
						
			// Is it black?
			// Can happen from a left over from the last GC cycle, can it?
			// -- it's checked in the write barrier
			// -- but not in mark....
			if (Native.rdMem(ref+OFF_SPACE)==toSpace) {
//				log("push: already in toSpace");
				return;
			}
			
			// only objects not allready in the gray list
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
			if (grayList!=GREY_END) log("GC: gray list not empty");

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
		int i, j, cnt;
		// only pushing stack roots need to be atomic
		synchronized (mutex) {
			// add complete stack of the current thread to the root list
//			roots = GCStkWalk.swk(RtThreadImpl.getActive(),true,false);
			i = Native.getSP();			
			for (j = Const.STACK_OFF; j <= i; ++j) {
				// disable the if when not using gc stack info
//				if (roots[j - Const.STACK_OFF] == 1) {
					push(Native.rdIntMem(j));
//				}
			}
			// Stacks from the other threads
			cnt = RtThreadImpl.getCnt();
			
			for (i = 0; i < cnt; ++i) {
				if (i != RtThreadImpl.getActive()) {
					int[] mem = RtThreadImpl.getStack(i);
					 // sp starts at Const.STACK_OFF
					int sp = RtThreadImpl.getSP(i) - Const.STACK_OFF;

//					roots = GCStkWalk.swk(i, false, false);

					for (j = 0; j <= sp; ++j) {
						// disable the if when not using gc stack info
//						if (roots[j] == 1) {
							push(mem[j]);
//						}
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
		for (i=0; i<cnt; ++i) {
			push(Native.rdMem(addr+i));
		}
	}
	
	static void markAndCopy() {
		
		int i, ref;
		
		if (!concurrentGc) {
			getStackRoots();			
		}
		getStaticRoots();
		for (;;) {
			
			// pop one object from the gray list
			synchronized (mutex) {
				ref = grayList;
				if (ref==GREY_END) {
					break;
				}
				grayList = Native.rdMem(ref+OFF_GREY);
				Native.wrMem(0, ref+OFF_GREY);		// mark as not in list
			}

			// allready moved
			// can this happen? - yes, as we do not check it in mark
			// TODO: no, it's checked in push()
			// What happens when the actuall scanning object is
			// again pushed on the gray stack by the mutator?
			if (Native.rdMem(ref+OFF_SPACE)==toSpace) {
				// it happens 
//				log("mark/copy allready in toSpace");
				continue;
			}
			
			// there should be no null pointers on the mark stack
//			if (Native.rdMem(ref+OFF_PTR)==0) {
//				log("mark/copy OFF_PTR=0!!!");
//				continue; 
//			}
			
				
			// push all childs
				
			// get pointer to object
			int addr = Native.rdMem(ref);
			int flags = Native.rdMem(ref+OFF_TYPE);
			if (flags==IS_REFARR) {
				// is an array of references
				int size = Native.rdMem(ref+OFF_MTAB_ALEN);
				for (i=0; i<size; ++i) {
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
					if ((flags&1)!=0) {
						push(Native.rdMem(addr+i));
					}
					flags >>>= 1;
				}				
			}
			
			// now copy it - color it BLACK			
			int size;
			int dest;

			synchronized(mutex) {
				size = Native.rdMem(ref+OFF_SIZE);
				dest = copyPtr;
				copyPtr += size;			

				// set it BLACK
				Native.wrMem(toSpace, ref+OFF_SPACE);
			}

			if (size>0) {
				// copy it
				for (i=0; i<size; i++) {
//  					Native.wrMem(Native.rdMem(addr+i), dest+i);
  					Native.memCopy(dest, addr, i);					
				}
			}

			// update object pointer to the new location
			Native.wrMem(dest, ref+OFF_PTR);
			// wait until everybody uses the new location
			for (i = 0; i < 10; i++);
			// turn off address translation
			Native.memCopy(dest, dest, -1);		
		}
	}
	
	/**
	 * Sweep through the 'old' use list and move garbage to free list.
	 */
	static void sweepHandles() {

		int ref;
		
		synchronized (mutex) {
			ref = useList;		// get start of the list
			useList = 0;		// new uselist starts empty
		}
		
		while (ref!=0) {
			
			// read next element, as it is destroyed
			// by addTo*List()
			int next = Native.rdMem(ref+OFF_NEXT);
			synchronized (mutex) {
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
		for (int i=fromSpace; i<end; ++i) {
			Native.wrMem(0, i);
		}
		// for tests clean also the remainig memory in the to-space
//		synchronized (mutex) {
//			for (int i=copyPtr; i<allocPtr; ++i) {
//				Native.wrMem(0, i);
//			}			
//		}
	}

	public static void setConcurrent() {
		concurrentGc = true;
	}
	static void gc_alloc() {
		log("GC allocation triggered");
		if (USE_SCOPES) {
			log("No GC when scopes are used");
			System.exit(1);
		}
		if (concurrentGc) {
			OOMError.fillInStackTrace();
			throw OOMError;
		} else {
			gc();
		}
	}

	public static void gc() {
//		log("GC called - free memory:", freeMemory());

		flip();
		markAndCopy();
		sweepHandles();
		zapSemi();	

//		log("GC end - free memory:",freeMemory());
		
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
		
		if (USE_SCOPES) {
			// allocate in scope
			Scope sc = null;
			if (RtThreadImpl.mission) {
				sc = RtThreadImpl.getCurrentScope();				
			}
			if (sc!=null) {
				int rem = sc.backingStore.length - sc.allocPtr;
				if (size+2 > rem) {
					OOMError.fillInStackTrace();
					throw OOMError;
				}
				int ref = sc.allocPtr;
				sc.allocPtr += size+2;
				int ptr = Native.toInt(sc.backingStore);
				ptr = Native.rdMem(ptr);
				ptr += ref;
				sc.backingStore[ref] = ptr+2;
				sc.backingStore[ref+1] = cons+Const.CLASS_HEADR;
				return ptr;
			}			
		}

		// that's the stop-the-world GC
		synchronized (mutex) {
			if (copyPtr+size >= allocPtr) {
				gc_alloc();
				if (copyPtr+size >= allocPtr) {
					// still not enough memory
					OOMError.fillInStackTrace();
					throw OOMError;
				}
			}			
		}
		synchronized (mutex) {
			if (freeList==0) {
				log("Run out of handles in new Object!");
				gc_alloc();
				if (freeList==0) {
					OOMError.fillInStackTrace();
					throw OOMError;
				}
			}			
		}
		
		int ref;
		
		synchronized (mutex) {
			// we allocate from the upper part
			allocPtr -= size;
			// get one from free list
			ref = freeList;
	//		if ((ref&0x07)!=0) {
	//			log("getHandle problem");
	//		}
	//		if (Native.rdMem(ref+OFF_PTR)!=0) {
	//			log("getHandle not free");
	//		}
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
			Native.wrMem(0, ref+OFF_GREY);
			// BTW: when we create mutex we synchronize on the not yet
			// created Object!
			// ref. flags used for array marker
			Native.wrMem(IS_OBJ, ref+OFF_TYPE);
			// pointer to method table in the handle
			Native.wrMem(cons+Const.CLASS_HEADR, ref+OFF_MTAB_ALEN);
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
		
		if (USE_SCOPES) {
			// allocate in scope
			Scope sc = null;
			if (RtThreadImpl.mission) {
				sc = RtThreadImpl.getCurrentScope();				
			}
			if (sc!=null) {
				int rem = sc.backingStore.length - sc.allocPtr;
				if (size+2 > rem) {
					OOMError.fillInStackTrace();
					throw OOMError;
				}
				int ref = sc.allocPtr;
				sc.allocPtr += size+2;
				int ptr = Native.toInt(sc.backingStore);
				ptr = Native.rdMem(ptr);
				ptr += ref;
				sc.backingStore[ref] = ptr+2;
				sc.backingStore[ref+1] = arrayLength;
				return ptr;
			}			
		}

		synchronized (mutex) {
			if (copyPtr+size >= allocPtr) {
				gc_alloc();
				if (copyPtr+size >= allocPtr) {
					// still not enough memory
					OOMError.fillInStackTrace();
					throw OOMError;
				}
			}			
		}
		synchronized (mutex) {
			if (freeList==0) {
				log("Run out of handles in new array!");
				gc_alloc();
				if (freeList==0) {
					OOMError.fillInStackTrace();
					throw OOMError;
				}
			}			
		}

		int ref;
		synchronized (mutex) {
			// we allocate from the upper part
			allocPtr -= size;
			// get one from free list
			ref = freeList;
	//		if ((ref&0x07)!=0) {
	//			log("getHandle problem");
	//		}
	//		if (Native.rdMem(ref+OFF_PTR)!=0) {
	//			log("getHandle not free");
	//		}
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
			Native.wrMem(0, ref+OFF_GREY);
			// ref. flags used for array marker
			Native.wrMem(type, ref+OFF_TYPE);
			// array length in the handle
			Native.wrMem(arrayLength, ref+OFF_MTAB_ALEN);
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
	
	/**
	 * Check if a given value is a valid handle.
	 * 
	 * This method traverse the list of handles (in use) to check
	 * if the handle provided belong to the list.
	 * 
	 * It does *not* check the free handle list.
	 * 
	 * One detail: the result may state that a handle to a 
	 * (still unknown garbage) object is valid, in case 
	 * the object is not reachable but still present 
	 * on the use list.
	 * This happens in case the object becomes unreachable
	 * during execution, but GC has not reclaimed it yet.
	 * Anyway, it's still a valid object handle.
	 * 
	 * @param handle the value to be checked.
	 * @return
	 */
	public static final boolean isValidObjectHandle(int handle)
	{
	  boolean isValid;
	  int handlePointer;
	  
	  // assume it's not valid and try to show otherwise 
	  isValid = false;
	  
	  // synchronize on the GC lock
	  synchronized (mutex) {
		// start on the first element of the list
	    handlePointer = useList;
	    
	    // traverse the list until the element is found or the list is over
	    while(handlePointer != 0)
	    {
	      if(handle == handlePointer)
	      {
	    	// found it! hence, it's a valid handle. Stop the search.
	    	isValid = true;
	    	break;
	      }
	      
	      // not found yet. Let's go to the next element and try again. 
	      handlePointer = Native.rdMem(handlePointer+OFF_NEXT);
	    }
	  }
	  
	  return isValid;
	}
  
  /**
   * Write barrier for an object field. May be used with regular objects
   * and reference arrays.
   * 
   * @param handle the object handle
   * @param index the field index
   */
  public static final void writeBarrier(int handle, int index)
  {
    boolean shouldExecuteBarrier = false;
    int gcInfo;
    
//    log("WriteBarrier: snapshot-at-beginning.");
    
    if (handle == 0)
    {
      throw new NullPointerException();
    }
    
    synchronized (GC.mutex)
    {
      // ignore objects with size zero (is this correct?)
      if(Native.rdMem(handle) == 0)
      {
//        log("ignore objects with size zero");
        return;
      }
      
      // get information on the object type.
      int type = Native.rdMem(handle + GC.OFF_TYPE);
      
      // if it's an object or reference array, execute the barrier
      if(type == GC.IS_REFARR)
      {
//        log("Reference array.");
        shouldExecuteBarrier = true;
      }
      
      if(type == GC.IS_OBJ)
      {
//        log("Regular object.");
        // get the object GC info from the class structure. 
        gcInfo = Native.rdMem(handle + GC.OFF_MTAB_ALEN) + Const.MTAB2GC_INFO;
        gcInfo = Native.rdMem(gcInfo);
        
//        log("GCInfo field: ", gcInfo);
        
        // if the correct bit is set for the field, it may hold a reference.
        // then, execute the write barrier.
        if((gcInfo & (0x01 << index)) != 0)
        {
//          log("Field can hold a reference. Execute barrier!");
          shouldExecuteBarrier = true;
        }
      }
      
      // execute the write barrier, if necessary.
      if(shouldExecuteBarrier)
      {
        // handle indirection
        handle = Native.rdMem(handle);
        // snapshot-at-beginning barrier
        int oldVal = Native.rdMem(handle+index);
        
//        log("Old val:       ", oldVal);
//        if(oldVal != 0)
//        {
//          log("Current space: ", Native.rdMem(oldVal+GC.OFF_SPACE));
//        }
//        else
//        {
//          log("Current space: NULL object.");
//        }
//        log("toSpace:       ", GC.toSpace);
        
        if (oldVal!=0 && Native.rdMem(oldVal+GC.OFF_SPACE)!=GC.toSpace) {
//          log("Executing write barrier for old handle: ", handle);
          GC.push(oldVal);
        }
      }
//      else
//      {
//        log("Should not execute the barrier.");
//      }
    }
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

}
