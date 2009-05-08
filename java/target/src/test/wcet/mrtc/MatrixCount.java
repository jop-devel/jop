package wcet.mrtc;

/**
 * Counts and sums non-negative numbers in a matrix.
 *
 * WCET aspect: Nested loops, well-structured code.
 *
 * Ported from C code written by Jan Gustafsson for the <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>.
 */
public class MatrixCount
{
    private static final int MAX_SIZE = 10;
    
    private int[][] array = new int[MAX_SIZE][MAX_SIZE];
    private int seed;
    private int posTotal, negTotal, posCnt, negCnt;

    public MatrixCount()
    {
        initSeed();
    	initialize(array);
    }
    
    // Initializes the seed used in the random number generator.
    private void initSeed()
    {
    	seed = 0;
    }
    
    // Generates random integers between 0 and 8095
    private int randomInteger()
    {
    	seed = ((seed * 133) + 81) % 8095;

    	return seed;
    }

    // Intializes the given array with random integers.
    private void initialize(int[][] array)
    {
    	for (int outer = 0; outer < MAX_SIZE; outer++)
    	{
    		for (int inner = 0; inner < MAX_SIZE; inner++)
    		{
    			array[outer][inner] = randomInteger();
    		}
    	}
    }
    
    public void count()
    {
    	for (int outer = 0; outer < MAX_SIZE; outer++)
    	{
            for (int inner = 0; inner < MAX_SIZE; inner++)
            {
    			if (array[outer][inner] >= 0)
    			{
    				posTotal += array[outer][inner];
    				posCnt++;
                }
                else
                {
                	negTotal += array[outer][inner];
                	negCnt++;
                }
            }
    	}
    }

    public static void main(String[] args)
    {
        MatrixCount c = new MatrixCount();

        c.count();
    }
}
