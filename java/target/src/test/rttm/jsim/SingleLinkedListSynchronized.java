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
package rttm.jsim;

import rttm.jsim.IncrementTest.Incrementer;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

/**
 * Test program for RTTM with a single double linked list (producer/consumer).
 * 
 * This Version uses "Synchronized" instead of TM
 * 
 * like in Herlihy & Moss, Transactional Memory: Architectural Support for Lock-Free Data Structures 1993 
 * 
 * @author Michael Muck
 */
public class SingleLinkedListSynchronized {
	
	private static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	private static int SIZE = sys.nrCpu+1;
	
	private static int MAX_ITERATIONS = 1000;
	
	private static SynchronizedLinkedList l = new SynchronizedLinkedList();
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ListMover mov[] = new ListMover[sys.nrCpu];
		
		for (int i=0; i<sys.nrCpu; ++i) {
			mov[i] = new ListMover(l);
			if(i > 0) {	
				Startup.setRunnable(mov[i], i-1);				
			}
		}	
		
		// PreFill List with sys.nrCpu elements
		for(int i=0; i<SIZE; ++i) {	
			l.insertAtHead(Integer.toString(i));	// for testing ... object is everytime the same
		}
		System.out.println("\nprefilled list:\n");
		/*
		l.reset();
		for(int i=0; i<SIZE; ++i) {
			System.out.print(l.next());
			System.out.print(" ");
		}
		*/
		
		int startTime, endTime;
		startTime = Native.rd(Const.IO_US_CNT);
		
		// start the other CPUs
		sys.signal = 1;
		// run one Runnable on this CPU
		mov[0].run();

		// wait for other CPUs to finish
		boolean finished = false;
		while (!finished) {
			finished = true;
			for(int i=0; i<sys.nrCpu; ++i) {
				if(mov[i].finished != true) {
					finished = false;
					break;
				}
			}
		}
		
		endTime = Native.rd(Const.IO_US_CNT);
		
		System.out.print("Time: ");
		System.out.print(endTime-startTime);
		System.out.println("\n");
		
		/*
		System.out.println("\nlist check:\n");
		l.reset();
		for(int i=0; i<SIZE; ++i) {
			System.out.print(l.next());
			System.out.print(" ");
		}
		*/
		
		System.out.println("Finished!");
		
		// write the magic string to stop the simulation
		System.out.println("\r\nJVM exit!\r\n");
	}
	
	static class ListMover implements Runnable {
		public boolean finished = false;
		
		private SynchronizedLinkedList myWorkingList;
		
		private int cnt = 0;
		
		public ListMover(SynchronizedLinkedList ll) {
			myWorkingList = ll;
		}
		
		public void run() {
			Object o = null;
			while (cnt < MAX_ITERATIONS) {
				
				synchronized(myWorkingList) {
					o = myWorkingList.removeFromTail();
				}
					
				if(o != null) {
					synchronized(myWorkingList) {
						myWorkingList.insertAtHead(o);
					}
					++cnt;
				}
				/*
				else {
					System.out.print("null");
				}
				*/
				
			}
			finished = true;
		}
	}
		
	static class SynchronizedLinkedList {
		public LinkedObject head;
		public LinkedObject tail;
		public LinkedObject current; 	// for iterations
		
		public SynchronizedLinkedList() {
			head = null;
			tail = null;
		}
		
		public void insertAtHead(Object newObject) {
			LinkedObject lo = new LinkedObject(newObject, null, null);
		
			//synchronized(l) {
				if(head == null) {	// list is empty
					head = lo;
					tail = head;
				}
				else {
					lo.next = head;		// save old head as new_head.next
					head.previous = lo;	// set previous in old head to new head
					head = lo;			// finally set new head as head
				}			
			//}		
		}	
		
		public Object removeFromTail() {
			Object o = null;
			
			//synchronized(l) {
				if(tail != null) {
					o = tail.thisObject;	// get object
					if( tail.previous == null ) {
						tail = null;
						head = null;
					}
					else {
						tail = tail.previous;	// tail.previous should never be emtpy
						tail.next = null;
					}
				}
			//}
			
			return o;
		}
	
		public void reset() {
			//synchronized(l) {
				current = head;
			//}
		}
		
		public Object next() {
			Object o = null;
			
			//synchronized(l) {
				if(current != null) {
					o = current.thisObject;				
					current = current.next;
				}
			//}
			
			return o;
		}
		
		static class LinkedObject {
			public Object thisObject;
			public LinkedObject next;
			public LinkedObject previous;
			
			public LinkedObject(Object o, LinkedObject prev, LinkedObject nex) {
				thisObject = o;
				previous = prev;				
				next = nex;
			}
		}
	}
}
