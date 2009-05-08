package wcet.mrtc;

//import edu.uci.eecs.doc.clepsydra.loop.LoopBound;

/**
 * Non-recursive version of quicksort algorithm.
 *
 * WCET aspect: The program sorts 20 numbers in an array. Loop nesting of 3 levels.
 *
 * Ported from C code written by Sung-Soo Lim for the SNU-RT benchmark suite with
 * modifications by Jan Gustafsson. See <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>. The original code sorted floating-point numbers; the Java port
 * sorts integers.
 */
public class QuicksortNonRecursive
{
    private int[] arr = {
        0,  // This element is ignored; the code was designed for 1-based arrays
        // Put the elements in reverse order to try to drive the worst case
        // (For straightforward implementation of quicksort, worst case is when all elements are in order!)
        20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1
    };

    // Auxiliary storage to simulate stack
    private int[] istack = new int[10];  // Stack size must be at least 2 lg(n)
    
    // When sub-arrays get smaller than M, insertion sort is used because it's faster for small arrays
    private static final int M = 7;

    private void swap(int[] arr, int a, int b)
    {
        int temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }

    public void sort()
    {
    	int ir = arr.length - 1;  // Subtract one because this algorithm assumes 1-based arrays
    	int l = 1;
    	int jstack = 0;
    	int a;
    	boolean broke = false;

        //@LoopBound(max=10)  // max=n/2
    	while (!broke) // @WCA loop<=10
    	{
    		if (ir - l < M)
    		{
        	    //@LoopBound(max=M-1)
    			for (int j = l + 1; j <= ir; j++) // @WCA loop<=6
    			{
    				a = arr[j];
    		
    				int i;

                    //@LoopBound(max=M-1)
    				for (i = j - 1; i >= l && !broke; i--) // @WCA loop<=6
    				{
    					if (arr[i] <= a)
    						broke = true;  // break;
    		
    		            if (!broke)
    					    arr[i + 1] = arr[i];
    					    
    					if (broke)
    					    i++;    // Not necessary if break statement is used above
    				}
    				
    				broke = false;
    		
    				arr[i + 1] = a;
    			}
    			
    			if (jstack == 0)
    				broke = true;  // break;
    		    
    		    if (!broke)
    		    {
        			ir = istack[jstack--];
        			l = istack[jstack--];
    		    }
    		}
    		else
    		{
    			int k = (l + ir) >> 1;
    			
    			swap(arr, k, l + 1);
    			
    			if (arr[l] > arr[ir])
    			{
    				swap(arr, l, ir);
    			}
    			
    			if (arr[l + 1] > arr[ir])
    			{
    				swap(arr, l + 1, ir);
    			}
    			
    			if (arr[l] > arr[l + 1])
    			{
    				swap(arr, l, l + 1);
    			}
    			
    			int i = l + 1;
    			int j = ir;
    			a = arr[l + 1];

    			//@LoopBound(max=10)  // max=n/2
    			while (!broke) // @WCA loop<=10
    			{
        			//@LoopBound(max=10)  // max=n/2
                    do { 
        				i++;
                    }
    				while (arr[i] < a); // @WCA loop<=10

                    //@LoopBound(max=10)  // max=n/2
    				do { 
        				j--;
    				}
    				while (arr[j] > a); // @WCA loop<=10

    				if (j < i)
    					broke = true;  // break;
    					
    				if (!broke)
    				    swap(arr, i, j);
    			}
    			
    			
    			broke = false;
    			
    			arr[l + 1] = arr[j];
    			arr[j] = a;
    			jstack += 2;

                // if (jstack > istack.length)
                //     throw new RuntimeException("Stack too small for sort");

    			if (ir - i + 1 >= j - l)
    			{
    				istack[jstack] = ir;
    				istack[jstack - 1] = i;
    				ir = j - 1;
    			}
    			else
    			{
    				istack[jstack] = j - 1;
    				istack[jstack - 1] = l;
    				l = i;
    			}
    		}
    	}
    }

    public static void main(String[] args)
    {
        QuicksortNonRecursive q = new QuicksortNonRecursive();

        q.sort();  
    }
}
