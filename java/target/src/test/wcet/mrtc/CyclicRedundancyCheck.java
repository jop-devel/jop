package wcet.mrtc;
//import edu.uci.eecs.doc.clepsydra.loop.LoopBound;

/**
 * A demonstration for CRC (Cyclic Redundancy Check) operation on 40 bytes of data.
 * The CRC is manipulated as two functions, icrc1 and icrc. icrc1 is for one
 * character and icrc uses icrc1 for a string. The input string is stored in array
 * lin[].  icrc is called two times, one for X-Modem string CRC and the other for
 * X-Modem packet CRC.
 *
 * WCET aspect: Complex loops, lots of decisions, loop bounds depend on function
 * arguments, function that executes differently the first time it is called.
 *
 * Ported from C code written by Sung-Soo Lim for the SNU-RT benchmark suite with
 * modifications by Jan Gustafsson. See <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>.
 */
public class CyclicRedundancyCheck
{
	private static final byte[] it = {0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15};

    // FIXME: The original C code used unsigned char and unsigned short types for these fields. They were changed to int types in the Java version to prevent casting to char/short/int, which currently causes problems for the Clepsydra WCET analyzer. However, this is wasteful of memory because int types take up 32 bits while chars take up 8 and shorts 16.
    private int[] lin;
	private int[] icrctb;
	private int[] rchr;
	private boolean initialized;
	
	public CyclicRedundancyCheck()
	{
	    lin = new int[256];
	    icrctb = new int[256];
	    rchr = new int[256];

        // Convert the character string to an integer array
	    byte[] linBytes = "asdffeagewaHAFEFaeDsFEawFdsFaefaeerdjgp".getBytes();
	    for (int i = 0; i < linBytes.length; i++)
	    {
	        lin[i] = linBytes[i];
	    }
	}

    private int loByte(int x)
    {
        return x & 0xFF;
    }
    
    private int hiByte(int x)
    {
        return x >> 8;
    }

    private int icrc1(int crc, int onech)
    {
    	int ans = crc ^ onech << 8;

        //@LoopBound(max=8)
    	for (int i = 0; i < 8; i++) // @WCA loop=8
    	{
    	    // In the code below, masking off the lower 16 bits simulates
    	    // the 16-bit overflow on the left shift that occurs implicitly
    	    // in the original C code as a result of the 16-bit short integer
    	    // data type.
    		if ((ans & 0x8000) != 0)
    		{
    			ans = ((ans <<= 1) & 0xFFFF) ^ 4129;
    		}
    		else
    		{
    			ans = (ans << 1) & 0xFFFF;
    		}
    	}

    	return ans;
    }

    private int icrc(int crc, int len, int jinit, int jrev)
    {
    	int tmp1, tmp2, j, cword = crc;

    	if (!initialized)
    	{
    		initialized = true;
    		
    		//@LoopBound(max=256)
    		for (j = 0; j <= 255; j++) // @WCA loop=256
    		{
    			icrctb[j] = icrc1(j << 8, 0);
    			rchr[j] = it[j & 0xF] << 4 | it[j >> 4];
    		}
    	}

    	if (jinit >= 0)
    	{
    		cword = jinit | (jinit << 8);
    	}
    	else if (jrev < 0)
    	{
    		cword = rchr[hiByte(cword)] | rchr[loByte(cword)] << 8;
    	}

        //@LoopBound(max=42)  // Maximum value depends on input parameter
    	for (j = 1; j <= len; j++) // @WCA loop=42
    	{
    		if (jrev < 0)
    		{
    			tmp1 = rchr[lin[j]] ^ hiByte(cword);
    		}
    		else
    		{
    			tmp1 = lin[j] ^ hiByte(cword);
    		}
    		
    		cword = icrctb[tmp1] ^ loByte(cword) << 8;
    	}
    	
    	if (jrev >= 0)
    	{
    		tmp2 = cword;
    	}
    	else
    	{
    		tmp2 = rchr[hiByte(cword)] | rchr[loByte(cword)] << 8;
    	}
    	
    	return tmp2;
    }
    
    public void crc(int i[])
    {
    	int i1, i2;
    	int n;

    	n = 40;
    	lin[n + 1] = 0;
    	i1 = icrc(0, n, 0, 1);
    	lin[n + 1] = hiByte(i1);
    	lin[n + 2] = loByte(i1);
    	i2 = icrc(i1, n + 2, 0, 1);
    	
    	i[0] = i1;
    	i[1] = i2;
    }

    public static void main(String[] args)
    {
        int[] i = new int[2];
        CyclicRedundancyCheck crc = new CyclicRedundancyCheck();
        crc.crc(i);

        System.out.println("i1=" + i[0]);
        System.out.println("i2=" + i[1]);
    }
}
