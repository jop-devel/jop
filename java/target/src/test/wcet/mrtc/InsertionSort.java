package wcet.mrtc;

//import edu.uci.eecs.doc.clepsydra.loop.LoopBound;

/**
 * Insertion sort on a reversed array of size 10.
 *
 * WCET aspect: Input-data dependent nested loop with worst-case of (n^2)/2
 * iterations (triangular loop).
 *
 * Ported from C code written by Sung-Soo Lim for the SNU-RT benchmark suite with
 * modifications by Jan Gustafsson. See <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>.
 */
public class InsertionSort
{
    private static final int SIZE = 10;
    private int[] a;
    
    public InsertionSort()
    {
        a = new int[SIZE + 1];

    	a[0] = 0;
    	a[1] = 11;
    	a[2] = 10;
    	a[3] = 9;
    	a[4] = 8;
    	a[5] = 7;
    	a[6] = 6;
    	a[7] = 5;
    	a[8] = 4;
    	a[9] = 3;
    	a[10] = 2;
    }

    private void sort()
    {
        int count = 0;
        //@LoopBound(max=SIZE - 1)
    	for (int i = 2; i <= SIZE; i++)
    	{
    	    //@LoopBound(max=45)  // Worst-case loop bound: (n^2 - n) / 2
    		for (int j = i; a[j] < a[j - 1]; j--)
    		{
    			int temp = a[j];
    			a[j] = a[j - 1];
    			a[j - 1] = temp;
    		}
    	}
    }
    
    public static void main(String[] args)
    {
        InsertionSort b = new InsertionSort();

        b.sort();
    }
}
