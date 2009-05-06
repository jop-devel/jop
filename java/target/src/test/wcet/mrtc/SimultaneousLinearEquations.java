package wcet.mrtc;

/**
 * Simultaneous linear equations by LU decomposition (from the book C Programming
 * for EEs by Hyun Soon Ahn). The arrays a[][] and b[] are input and the array x[]
 * is output row vector. The variable n is the number of equations.
 *
 * WCET aspect: Test the effect of conditional flows.
 *
 * Ported from C code written by Sung-Soo Lim for the SNU-RT benchmark suite with
 * modifications by Jan Gustafsson. See <a
 * href="http://www.mrtc.mdh.se/projects/wcet/benchmarks.html">Maelardalen WCET
 * Benchmarks</a>.
 */
public class SimultaneousLinearEquations
{
    private static final int NMAX = 50;
    private static final int N = 5;
    
    private int[][] a;
    private int[] b;
    private int[] x;
    private int[] y;
    
    public SimultaneousLinearEquations()
    {
        a = new int[NMAX][NMAX];
        b = new int[NMAX];
        x = new int[NMAX];
        y = new int[NMAX + 1];
    }
    
    private void ludcmp(int nmax, int n)
    {
        int w;

        //@LoopBound(max=N)
        for (int i = 0; i < n; i++)	// @WCA loop<=5
        {
            //@LoopBound(max=N)
        	// triangular loop vs. i
            for (int j = i + 1; j <= n; j++) // @WCA loop<=5
            {
                w = a[j][i];

                if (i != 0)
                {
                    // sub-loop is conditional, done all iterations except first of the OUTER loop                    
                    //@LoopBound(max=N)
                    for (int k = 0; k < i; k++) // @WCA loop<=5
                        w -= a[j][k] * a[k][i];
                }

                // FIXME: Cannot do division because idiv bytecode is implemented in software on JOP, causing problems for Clepsydra. Replace with multiplication for now.
                // a[j][i] = w / a[i][i];
                a[j][i] = w * a[i][i];
            }

            //@LoopBound(max=N)
        	// triangular loop vs. i
            for (int j = i + 1; j <= n; j++) // @WCA loop<=5 
            {
                w = a[i + 1][j];

                //@LoopBound(max=N)
                // triangular loop vs. i
                for (int k = 0; k <= i; k++) // @WCA loop<=5  
                    w -= a[i + 1][k] * a[k][j];

                a[i + 1][j] = w;
            }
        }

        y[0] = b[0];

        //@LoopBound(max=N)
        // iterates n times
        for (int i = 1; i <= n; i++) // @WCA loop<=5
        {
            w = b[i];

            //@LoopBound(max=N)
        	// triangular sub loop
            for (int j = 0; j < i; j++) // @WCA loop<=5
                w -= a[i][j] * y[j];

            y[i] = w;
        }

        // FIXME: Cannot do division because idiv bytecode is implemented in software on JOP, causing problems for Clepsydra. Replace with multiplication for now.
        // x[n] = y[n] / a[n][n];
        x[n] = y[n] * a[n][n];

        //@LoopBound(max=N)
    	// iterates n times
        for (int i = n - 1; i >= 0; i--) // @WCA loop<=5
        {
            w = y[i];

            //@LoopBound(max=N)
        	// triangular sub loop
            for (int j = i + 1; j <= n; j++) // @WCA loop<=5
                w -= a[i][j] * x[j];

            // FIXME: Cannot do division because idiv bytecode is implemented in software on JOP, causing problems for Clepsydra. Replace with multiplication for now.
            // x[i] = w / a[i][i];
            x[i] = w * a[i][i];
        }
    }

    public void run()
    {
        // Initialize the matrices
        //@LoopBound(max=N+1)
        for (int i = 0; i <= N; i++) // @WCA loop<=6
        {
            int w = 0;  // data to fill in cells

            //@LoopBound(max=N+1)
            for (int j = 0; j <= N; j++) // @WCA loop<=6
            {
                a[i][j] = (i + 1) + (j + 1);

                if (i == j)  // only once per loop pass
                    a[i][j] *= 2;

                w += a[i][j];
            }

            b[i] = w;
        }

        ludcmp(NMAX, N);
    }
    
    public static void main(String[] args)
    {
        SimultaneousLinearEquations s = new SimultaneousLinearEquations();

        s.run();
    }
}
