package wcet.devel;

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * Binary search for the array of n integer elements.
 *
 * WCET aspect: Completely structured.
 *
 * Ported from C code written by Sung-Soo Lim for the SNU-RT benchmark suite with
 * modifications by Jan Gustafsson. See <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>.
 * <p>
 * Note: this implementation of binary search is both hard to analyze and
 *       inefficient. See rtlib/BinarySearch#bsearch for a fast and easy to
 *       analyze real-time variant.
 * </p>
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

	public static final int MAX_SIZE = 128;

	private Data[] data;
	private int size;

    public BinarySearch(int capacity)
    {
        data = new Data[capacity];
        
        for (int i = 0; i < capacity; i++) // @WCA loop <= 129
        // @WC3A loop <= $1
        {
            data[i] = new Data(i<<1, i * 100);
        }
    }
    int loopCount;
    public int binarySearch(int x)
    {
    	int fvalue, mid, up, low;
        
        low = 0;
        up = size-1;
        fvalue = -1;  // all data are positive

        /* Loop Bound <= floor ( log(up-low+1) / log 2 ) + 1
         * Proof by induction:
         * up-low = 0 => iterations <= 0 + 1 = 1
         *    Proof:     Assume mid = low = up
         *               (a) up'  = mid - 1 = low - 1 ==> up-low = -1
         *               (b) low' = mid + 1 = up + 1  ==> up-low = -1 
         * Assume for all k < n, up-low = k ==> iterations <= floor(ld [k+1]) + 1
         * We have to show that ==> up-low = n ==> iterations <= floor(ld [n+1]) + 1
         *     Proof:    Assume mid = (low + low + n) >> 1;
         *                      mid = floor( (2 low + n) / 2 );
         *                      mid = floor( low + n/2 ) = low + floor (n/2);
         *               (a) up' = mid - 1 = low + floor (n/2) - 1
         *                   (up-low)' = low + floor (n/2) - 1 - low = floor (n/2) - 1
         *                   ==> iterations <= floor(ld [floor(n/2) - 1 + 1]) + 1 + 1  
         *                                  <= floor(ld [floor(n/2)]) + 2
         *                                  <= floor(ld(n/2)) + 2
         *                                  =  floor(ld(n)) + 1
         *                                  <= floor(ld(n+1)) + 1
         *               (b) low' = mid + 1 = low + floor (n/2) + 1
         *                   (up-low)' = low+n -low-floor(n/2)-1 = n-floor(n/2)-1
         *                   ==> iterations <= (floor(ld [n-floor(n/2)-1 + 1]) + 1) + 1     
         *                                   = floor(ld[n-floor(n/2)]) + 2
         *                                  <= floor(ld[n-(n-1)/2]) + 2
         *                                   = floor(ld[(n+1)/2]) + 2
         *                                   = floor(ld(n+1)) + 1
         *                                  <= floor(ld(n+1)) + 1
         */
    	loopCount = 0;
        while (low <= up) // @WC1A loop <= bitlength(128)
        	              // @WCA loop <= bitlength(MAX_SIZE)
             	          // @WC3A:  loop <= bitlength($this.size)
    	{    
        	loopCount++;
        	mid = (low + up) >> 1;

    		if (data[mid].key == x)  // @WC4A: WCA flow <= 1 loop
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
    
	static int ts, te, to;
	private static BinarySearch b;

	public static void main(String[] args)
    {
		if(Config.MEASURE) {
			ts = Native.rdMem(Const.IO_CNT);
			te = Native.rdMem(Const.IO_CNT);
			to = te-ts;
		}
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
        b = new BinarySearch(MAX_SIZE);
		for(int i = 1; i <= MAX_SIZE; i++) {
			b.size = i;
//          double lb = Math.floor ( Math.log(i) / Math.log(2) ) + 1;
            int maxLb = 0;
            for(int j = -1; j < (i<<1)+1;++j) {
        		if(Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
                b.binarySearch(j);
        		if(Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
                if(b.loopCount > maxLb) maxLb = b.loopCount;
        		if (Config.MEASURE) {
        			int dt = te-ts-to;
        			if(dt > max) max = dt;
        			if(dt < min) min = dt;
                }
            }
			System.out.print("wcet[BinarySearch,n=");
			System.out.print(i);
			System.out.print("] ");
			System.out.print(min);
			System.out.print(" - ");
			System.out.print(max);
			System.out.print(", max. ");
			System.out.print(maxLb);
			System.out.println(" iterations");
        }
    }
}
