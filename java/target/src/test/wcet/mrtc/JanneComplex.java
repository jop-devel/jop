package wcet.mrtc;

/**
 * Nested loop program.
 *
 * WCET aspect: The inner loop's maximum number of iterations depends on the outer
 * loop's current iteration number.
 *
 * Ported from C code written by Andreas Ermedahl, Uppsala university, May 2000.
 * Incorporated into the <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a> by Jan Gustafsson.
 *
 * The example appeard for the first time in:
 * @InProceedings{Ermedahl:Annotations,
 *   author =       "A. Ermedahl and J. Gustafsson",
 *   title =        "Deriving Annotations for Tight Calculation of Execution Time",
 *   year =         1997,
 *   month =        aug,
 *   booktitle =    EUROPAR97,
 *   publisher =    "Springer Verlag",
 *   pages =        "1298-1307"
 * }
 *
 * The name probably derives from Jan Gustafsson's first name. "Janne" is a pet
 * form of "Jan" meaning "God is gracious."
 */
public class JanneComplex
{
    public void complex(int a, int b)
    {
        //@LoopBound(max=9)  // max=9 when a=1 and b=1
    	while (a < 30)	// @WCA loop<=9
    	{
    	    //@LoopBound(max=12)  // max=12 when a=1 and b=1
    		while (b < a)	// @WCA loop<=12
    		{
    			if (b > 5)
    			{
    				b *= 3;
    			}
    			else
    			{
    				b += 2;
    			}

    			if (b >= 10 && b <= 12)
    			{
    				a += 10;
    			}
    			else
    			{
    				a++;
    			}
    		}

    		a += 2;
    		b -= 10;
    	}
    }
    
    public static void main(String[] args)
    {
        JanneComplex j = new JanneComplex();

    	// a = [1..30] b = [1..30]
    	int a = 1, b = 1;

    	j.complex(a, b);
    }
}
