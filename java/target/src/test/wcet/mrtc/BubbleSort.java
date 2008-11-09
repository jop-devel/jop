package wcet.mrtc;
//import java.util.Arrays;

//import edu.uci.eecs.doc.clepsydra.loop.LoopBound;

/**
 * Bubblesort program.
 *
 * WCET aspect: Tests the basic loop constructs, integer comparisons, and
 * simple array handling by sorting 100 integers.
 *
 * Ported from C code written by Jan Gustafsson for the <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>.
 */
public class BubbleSort
{
    // Use -1 for worst case, 1 for best case
    private static final int FACTOR = -1;
    
    private static final int NUM_ELEMENTS = 100;
    private int[] array = new int[NUM_ELEMENTS];
    
    public BubbleSort()
    {
        //@LoopBound(max=NUM_ELEMENTS)
        for (int i = 0; i < NUM_ELEMENTS; i++)
        {
            array[i] = i * FACTOR;
        }
    }
    
    /*
     * Sorts an array of integers of size NUM_ELEMENTS in ascending order.
     */
    public void bubbleSort()
    {
        //@LoopBound(max=NUM_ELEMENTS - 1)
    	for (int i = 1; i < NUM_ELEMENTS; i++) // @WCA loop=99
    	{
    	    //@LoopBound(max=NUM_ELEMENTS - 1)
    		for (int j = 0; j < NUM_ELEMENTS - 1; j++) // @WCA loop=99
    		{
    			if (array[j] > array[j + 1])
    			{
    			    // Swap array[j] with array[j + 1]
    				int temp = array[j];
    				array[j] = array[j + 1];
    				array[j + 1] = temp;
    			}
    		}
    	}
    }
    
    public static void main(String[] args)
    {
        BubbleSort b = new BubbleSort();

        b.bubbleSort();

//        System.out.println(Arrays.toString(b.array));
    }
}
