package wcet.mrtc;

/**
 * A function to select the Nth smallest number in an array.
 *
 * WCET aspect: A lot of array calculations, loop nesting (3 levels).
 *
 * Ported from C code written by Sung-Soo Lim for the SNU-RT benchmark suite with
 * modifications by Jan Gustafsson. See <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>.
 *
 * The original code selected floating-point numbers; the Java port
 * selects integers.
 */
public class SelectSmallest
{
    private int[] arr = {
        0,  // This element is ignored; the code was designed for 1-based arrays
        // Put the elements in reverse order to try to drive the worst case
        20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1
    };
    
    private void swap(int[] arr, int a, int b)
    {
        int temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }

    // The parameters to function select are k and n. The function
    // selects the k-th largest number out of n original numbers.
    // Note: This code is based on the code from "Numerical Recipes in C, Second Edition". Someone changed it by adding flag variables to replace the return and break statements.
    public int select(int k, int n)
    {
    	int i, ir, j, l, mid;
    	int a, temp;
    	int flag, flag2;

    	l = 1;
    	ir = n;
    	flag = flag2 = 0;

        //@LoopBound(max=5)  // max=log(n)
    	while (flag == 0) // @WCA loop<=5
    	{
    		if (ir <= l + 1)
    		{
    			if (ir == l + 1 && arr[ir] < arr[l])
				{
					swap(arr, l, ir);
				}

    			flag = 1;  // return arr[k];
    		}
    		else if (flag == 0)
    		{
    			mid = (l + ir) >> 1;
    			
    			swap(arr, mid, l + 1);
    			
    			if (arr[l] > arr[ir])
    			{
    				swap(arr, l, ir);
    			}
    			
    			if (arr[l + 1] > arr[ir])
    			{
    				swap(arr, l +  1, ir);
    			}
    			
    			if (arr[l] > arr[l + 1])
    			{
    				swap(arr, l, l + 1);
    			}
    			
    			i = l + 1;
    			j = ir;
    			a = arr[l+1];

    			//@LoopBound(max=10) // max=n/2
    			while (flag2 == 0) {   // @WCA loop<=10
    				
                    //@LoopBound(max=10)  // max=n/2
                    do
                    {
        				i++;
                    }
    				while (arr[i] < a);	// @WCA loop<=10

			        //@LoopBound(max=10)  // max=n/2
    			    do
    			    {
        				j--;
    			    }
    				while (arr[j] > a); // @WCA loop<=10
    			
    				if (j < i)
    					flag2 = 1;  // break;
    			
    				if (flag2 == 0)
    					swap(arr, i, j);
                }
        
                flag2 = 0;
    			
    			arr[l+1] = arr[j];
    			arr[j] = a;
    			
    			if (j >= k)
    				ir = j - 1;
    			
    			if (j <= k)
    				l = i;
    		}
    	}
    	
    	return arr[k];
    }

    public static void main(String[] args)
    {
        SelectSmallest s = new SelectSmallest();
        
        s.select(10, 20);
    }
}
