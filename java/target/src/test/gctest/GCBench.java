/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Alberto Andreotti

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

// This is adapted from a benchmark written by John Ellis and Pete Kovac
// of Post Communications.
// It was modified by Hans Boehm of Silicon Graphics.
//
// 	This is no substitute for real applications.  No actual application
//	is likely to behave in exactly this way.  However, this benchmark was
//	designed to be more representative of real applications than other
//	Java GC benchmarks of which we are aware.
//	It attempts to model those properties of allocation requests that
//	are important to current GC techniques.
//	It is designed to be used either to obtain a single overall performance
//	number, or to give a more detailed estimate of how collector
//	performance varies with object lifetimes.  It prints the time
//	required to allocate and collect balanced binary trees of various
//	sizes.  Smaller trees result in shorter object lifetimes.  Each cycle
//	allocates roughly the same amount of memory.
//	Two data structures are kept around during the entire process, so
//	that the measured performance is representative of applications
//	that maintain some live in-memory data.  One of these is a tree
//	containing many pointers.  The other is a large array containing
//	double precision floating point numbers.  Both should be of comparable
//	size.
//
//	The results are only really meaningful together with a specification
//	of how much memory was used.  It is possible to trade memory for
//	better time performance.  This benchmark should be run in a 32 MB
//	heap, though we don't currently know how to enforce that uniformly.
//
//	Unlike the original Ellis and Kovac benchmark, we do not attempt
// 	measure pause times.  This facility should eventually be added back
//	in.  There are several reasons for omitting it for now.  The original
//	implementation depended on assumptions about the thread scheduler
//	that don't hold uniformly.  The results really measure both the
//	scheduler and GC.  Pause time measurements tend to not fit well with
//	current benchmark suites.  As far as we know, none of the current
//	commercial Java implementations seriously attempt to minimize GC pause
//	times.
//
//	Known deficiencies:
//		- No way to check on memory use
//		- No cyclic data structures
//		- No attempt to measure variation with object size
//		- Results are sensitive to locking cost, but we dont
//		  check for proper locking

package gctest;

import com.jopdesign.sys.GC;

class Node {
	Node left, right;
	int i, j;
	Node(Node l, Node r) { left = l; right = r; }
	Node() { }
}

public class GCBench {

	public static final int kStretchTreeDepth    = 5; //18;	// about 16Mb
	public static final int kLongLivedTreeDepth  = 5; //16;  // about 4Mb
	public static final int kArraySize  = 500; // 500000;  // about 4Mb
	public static final int kMinTreeDepth = 4;
	public static final int kMaxTreeDepth = 16;

	// Nodes used by a tree of a given size
	static int TreeSize(int i) {
	    	return ((1 << (i + 1)) - 1);
	}

	// Number of iterations to use for a given tree depth
	static int NumIters(int i) {
	    		int b = TreeSize(kStretchTreeDepth);
                return (b+b) / TreeSize(i);
        }

	// Build tree top down, assigning to older objects. 
	static void Populate(int iDepth, Node thisNode) {
		if (iDepth<=0) {
			return;
		} else {
			iDepth--;
			thisNode.left  = new Node();
			thisNode.right = new Node();
			Populate (iDepth, thisNode.left);
			Populate (iDepth, thisNode.right);
		}
	}

	// Build tree bottom-up
	static Node MakeTree(int iDepth) {
		if (iDepth<=0) {
			return new Node();
		} else {
			return new Node(MakeTree(iDepth-1),
					MakeTree(iDepth-1));
		}
	}

	static void PrintDiagnostics() {
		int lFreeMemory = GC.freeMemory();
		int lTotalMemory = GC.totalMemory();

		// System.out.print(" Total memory available="+ lTotalMemory + " bytes");
		// System.out.println("  Free memory=" + lFreeMemory + " bytes");
		System.out.print("Total Memory: ");
		System.out.println(lTotalMemory);
		System.out.print("Free Memory: ");
		System.out.println(lFreeMemory);
	}

	static void TimeConstruction(int depth) {
		Node    root;
		int    tStart, tFinish;
		int 	iNumIters = NumIters(depth);
		Node	tempTree;

		// System.out.println("Creating " + iNumIters + " trees of depth " + depth);
		System.out.print("Creating "); 
		System.out.print(iNumIters);
		System.out.print(' ');
		System.out.print("trees of depth "); 
		System.out.println(depth);
		tStart = (int) System.currentTimeMillis();
		for (int i = 0; i < iNumIters; ++i) {
			tempTree = new Node();
			Populate(depth, tempTree);
			tempTree = null;
		}
		tFinish = (int) System.currentTimeMillis();
		// System.out.println("\tTop down construction took "+ (tFinish - tStart) + "msecs");
		System.out.print("\tTop down construction took (msec): ");
		System.out.println(tFinish - tStart);
		tStart = (int) System.currentTimeMillis();
        for (int i = 0; i < iNumIters; ++i) {
                        tempTree = MakeTree(depth);
                        tempTree = null;
        }
        tFinish = (int) System.currentTimeMillis();
                // System.out.println("\tBottom up construction took "+ (tFinish - tStart) + "msecs");
		System.out.print("\tBottom up construction took (msec): ");
		System.out.println(tFinish - tStart);
	}

	public static void main(String args[]) {
		Node	root;
		Node	longLivedTree;
		Node	tempTree;
		int	tStart, tFinish;
		int	tElapsed;


		// System.out.println("Garbage Collector Test");
		// System.out.println(" Stretching memory with a binary tree of depth "+ kStretchTreeDepth);
		System.out.print(" Stretching memory with a binary tree of depth ");
		System.out.println(kStretchTreeDepth);
	
		PrintDiagnostics();
		tStart = (int) System.currentTimeMillis();

		// Stretch the memory space quickly
		tempTree = MakeTree(kStretchTreeDepth);
		tempTree = null;

		// Create a long lived object
		System.out.print(" Creating a long-lived binary tree of depth ");
		System.out.println(kLongLivedTreeDepth);
		// System.out.println(" Creating a long-lived binary tree of depth " +kLongLivedTreeDepth);
		longLivedTree = new Node();
		Populate(kLongLivedTreeDepth, longLivedTree);
//		GC.gc();
//		PrintDiagnostics();

		// Create long-lived array, filling half of it
		// System.out.println(" Creating a long-lived array of "+ kArraySize + " doubles");
		System.out.print(" Creating a long-lived array of ints ");
		System.out.println(kArraySize);
		int array[] = new int[kArraySize];
		for (int i = 0; i < kArraySize/2; ++i) {
			array[i] = i; //1.0/i;
		}
		PrintDiagnostics();

		for (int d = kMinTreeDepth; d <= kMaxTreeDepth; d += 2) {
			TimeConstruction(d);
		}

		if (longLivedTree == null) { // || array[1000] != 1.0/1000) {
			System.out.print("Failed!\n");
		    // System.out.println("Failed");
					// fake reference to LongLivedTree
					// and array
					// to keep them from being optimized away
		}
		tFinish = (int) System.currentTimeMillis();
		tElapsed = tFinish-tStart;
		PrintDiagnostics();
		// System.out.println("Completed in " + tElapsed + "ms.");
		System.out.print("Completed in (msec): ");
		System.out.println(tElapsed);
	}
} // class JavaGC
