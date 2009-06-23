// project: spcmp paper
// example: bubble sort
// author:  Raimund Kirner, 22.06.2009


package sp;

import com.jopdesign.sys.Native;

/**
 * A real-time task that performs sorting via bubble sort.
 * This is a single-path program.
 *
 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
 */
class SPBubbleSort {
    final int SIZE = 20;
    int[] aDat; 
    int i;
    int j;
    int temp;
    boolean cond;

    // Standard Constructor 
    public SPBubbleSort() {
	aDat = new int[SIZE];
    }

    // read data from mem
    public void read() {
	for (int i=0; i < aDat.length; i++) {
	    aDat[i] = (i + 30) % SIZE;
	}
    } 

    // read data from mem
    public void write() {
    }

    // sort routine
    public void sort() {
	// Sortieren
	i = 0;
	while ( i < aDat.length) {  
	    j = aDat.length - 1;
	    while ( j >= i ) {
		cond = aDat[j] < aDat[j-1];
		// conventional swap:
		//if ( cond==true ) {
		//    temp  = aDat[j];
		//    aDat[j]   = aDat[j-1];
		//    aDat[j-1] = temp;
		//}
		//
		// single path swap:
		temp = aDat[j];
		aDat[j]   = Native.condMove(aDat[j-1],aDat[j],cond);
		aDat[j-1] = Native.condMove(temp,aDat[j-1],cond);
		j = j-1;
	    }
	    i = i+1;
	}
	
    }


    public static void main(String[] args) {
	SPBubbleSort spbs = new SPBubbleSort();

	spbs.read();

	spbs.sort();

	spbs.write();
    }
}



