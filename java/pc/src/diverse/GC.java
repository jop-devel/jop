package diverse;


  class GC {

/***************************************************************
* 
* The heap is represented is as the following three arrays.
* Thus, each node i has a left pointer "left[i]", a right pointer
* "right[i]", and a marked bit "marked[i]". 
*
* These arrays are non-null and are all of length "memSize". Each
* entry in the left and right arrays a either is the special
* constant NULL (-1), or else is a valid node index in the range
* [0,memSize).
*
**************************************************************/

public static boolean[] marked;
public static int left[];
public static int right[];
public static final int memSize = 10;
public static final int NULL = -1;

/**************************************************************
*
* The method GC takes at its argument a valid node index "root"
* in the range [0,memSize), marks all nodes that are reachable
* from that root, collects all the unmarked nodes into a free
* list, and returns the index of the root of this list.
*
* There are several correctness requirements on this method:
*
* * The left and right fields of node reachable from root are not changed
*
* * Any node on the freelist (that is, reachable from the result
* of this method) is not reachable from root
*
* * Any node that is not reachable from root is on the free list
*
* The first two requirements are soundness properties, whereas
* the last requirement is a completeness property.
*
**************************************************************/

public static int GCTest(int root) {

	for(int i=0; i<memSize; i++) {
		marked[i] = false;
	}

	mark(root);

	int freeList = NULL;

	for(int i=0; i<memSize; i++) {
		if( !marked[i] ) {
			left[i] = freeList;
			right[i] = NULL;
			freeList = i;
		}
	}

	return freeList;

}

/**************************************************************
*
* Marks all nodes that a reachable from "from".
*
**************************************************************/

private static void mark(int from) {

	if( !marked[from] ) {

		marked[from] = true;
		if( left[from] != NULL ) mark( left[from] );
		if( right[from] != NULL ) mark( right[from] );

	}

}

}

