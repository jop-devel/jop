package wcet.mrtc;
//import edu.uci.eecs.doc.clepsydra.loop.LoopBound;

/**
 * Simple iterative Fibonacci calculation used to calculate fib(30).
 *
 * WCET aspect: Parameter-dependent function, single-nested loop.
 *
 * Ported from C code written by Sung-Soo Lim for the SNU-RT benchmark suite with
 * modifications by Jan Gustafsson. See <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>.
 */
public class Fibonacci
{
    private int fib(int n)
    {
    	int i, fNew, fOld, temp;

    	fNew = 1;
    	fOld = 0;

    	//@LoopBound(max=29)
    	for (i = 2; i <= 30 && i <= n; i++)
    	{
    		temp = fNew;
    		fNew = fNew + fOld;
    		fOld = temp;
    	}

    	return fNew;
    }

    public static void main(String[] args)
    {
        Fibonacci f = new Fibonacci();
        
        f.fib(30);
    }
}
