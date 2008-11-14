package wcet.mrtc;
//import edu.uci.eecs.doc.clepsydra.loop.LoopBound;

/**
 * Binary search for the array of 15 integer elements.
 *
 * WCET aspect: Completely structured.
 *
 * Ported from C code written by Sung-Soo Lim for the SNU-RT benchmark suite with
 * modifications by Jan Gustafsson. See <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>.
 */
public class BinarySearch
{
    private static class Data
    {
    	public int key;
    	public int value;

        public Data(int key, int value)
        {
            this.key = key;
            this.value = value;
        }
    }

    private static final int DATA_SIZE = 15;
    private Data[] data;

    public BinarySearch()
    {
        data = new Data[DATA_SIZE];
        
        for (int i = 0; i < DATA_SIZE; i++)
        {
            data[i] = new Data(i, i * 100);
        }
    }
    
    public int binarySearch(int x)
    {
    	int fvalue, mid, up, low;
        
        low = 0;
        up = 14;
        fvalue = -1;  // all data are positive

        // @LoopBound(max=4)  // max=log(DATA_SIZE)
    	while (low <= up) // @WCA loop=4
    	{
        	mid = (low + up) >> 1;

        	if (data[mid].key == x)  // found
        	{
        		up = low - 1;
        		fvalue = data[mid].value;
    		}
    		else if (data[mid].key > x)
        	{
    			up = mid - 1;
    		}
    		else
    		{
        		low = mid + 1;
    		}
    	}

    	return fvalue;
    }
    
    public static void main(String[] args)
    {
        BinarySearch b = new BinarySearch();
        
        b.binarySearch(-1);  // Use non-existent key to drive worst-case performance
    }
}
