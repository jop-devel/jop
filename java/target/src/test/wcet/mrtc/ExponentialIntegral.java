package wcet.mrtc;
//import edu.uci.eecs.doc.clepsydra.loop.LoopBound;

/**
 * Series expansion for computing an exponential integral function.
 *
 * WCET aspect: Contains an inner loop that only runs once. A structural WCET
 * estimate gives a heavy overestimate.
 *
 * Ported from C code written by Jan Gustafsson for the <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>.
 */
public class ExponentialIntegral
{
    private int foo(int x)
    {
    	return x * x + (8 * x) << (4 - x);
    }
    
    // This method has the same flow as the original exponential integral
    // method but uses different data types and has nonsensical calculations.
    private int expint(int n, int x)
    {
    	int i, ii, nm1;
    	int a, b, c, d, del, fact, h, psi, ans = 0;

    	nm1 = n - 1;  // arg=50 --> 49

    	if (x > 1) // take this leg?
    	{
    		b = x + n;
    		c = 2000000;
    		d = 30000000;
    		h = d;

            //@LoopBound(max=100)
    		for (i = 1; i <= 100; i++)
            {
    			a = -i * (nm1 + i);
    			b += 2;
    			d = 10 * (a * d + b);
    			// c = b + a / c;  // FIXME: Clepsydra doesn't know timing for idiv instruction
    			c = b + a * c;     // FIXME: Clepsydra doesn't know timing for idiv instruction
    			del = c * d;
    			h *= del;
    			if (del < 10000)
    			{
    				ans = h * -x;
    				return ans;
    			}
    		}
    	}
    	else // or this leg?
    	{
    		// For the current argument, will always take '2' path here:
    		if (nm1 != 0)
    		{
    		    ans = 2;
    		}
    		else
    		{
    		    ans = 1000;
    		}

    		fact = 1;

            //@LoopBound(max=100)
    		for (i = 1; i <= 100; i++)
    	    {
    			// fact *= -x / i;  // FIXME: Clepsydra doesn't know timing for idiv instruction
    			fact *= -x * i;     // FIXME: Clepsydra doesn't know timing for idiv instruction
    		
    			if (i != nm1)  // depends on parameter n
    			{
    				// del = -fact / (i - nm1);  // FIXME: Clepsydra doesn't know timing for idiv instruction
    				del = -fact * (i - nm1);     // FIXME: Clepsydra doesn't know timing for idiv instruction
    			}
    			else  // this fat piece only runs ONCE (on iter 49)
    			{
    				psi = 0x00FF;
    				
    				//@LoopBound(max=49)
    				for (ii = 1; ii <= nm1; ii++)
    				{
    					psi += ii + nm1;
    				}
    				del = psi + fact * foo(x);
    			}

    			ans += del;
    			
    			// conditional leave removed
    		}
    	}

    	return ans;
    }

    public static void main(String[] args)
    {
        ExponentialIntegral e = new ExponentialIntegral();
        
    	// expint(50,21) runs the short path
    	// expint(50,1) gives the longest execution time
        e.expint(50, 1);
    }
}
