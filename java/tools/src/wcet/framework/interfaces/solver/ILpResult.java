/**
 * 
 */
package wcet.framework.interfaces.solver;

import java.util.HashSet;

/**
 * @author Elena Axamitova
 * @version 0.1 04.10.2007
 */
public interface ILpResult {
    
    public static int UNSOLVED = -1;
    
    public static int OPTIMAL = 0;

    public static int SUBOPTIMAL = 1;

    public static int INFEASIBLE = 2;

    public static int UNBOUNDED = 3;

    public static String[] RESULT_TEXT = {"OPTIMAL", "SUBOPTIMAL", "INFEASIBLE","UNBOUNDED"};
    
   /* 
    * Since I have no idea what these values below mean,
    * I do not use them. Maybe later.
    * 
    
    public static int DEGENERATE = 4;

    public static int NUMFAILURE = 5;

    public static int USERABORT = 6;

    public static int TIMEOUT = 7;

    public static int RUNNING = 8;

    public static int PRESOLVED = 9;*/
    
    public double getObjectiveValue();
    
    public String getName();
    
    public HashSet<String> getAllVarNames();
    
    public double getVarValue(String name);
    
    public int getStatus();
}
