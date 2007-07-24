/**
 * 
 */
package wcet.components.lpsolver;

/**
 * @author Elena Axamitova
 * @version 0.1 24.05.2007
 */
public interface ILpSolverConstants {
    /**
     * Order of LP solver lpsolve
     */
    public static final int LPSOLVE_SOLVER_ORDER = 1;
    
    /**
     * Order of LP solver from or objects
     */
    public static final int OR_SOLVER_ORDER = 3;
    
    /**
     * Key to the lpsolve result in data store
     */
    public static final String LPSOLVE_RESULT_KEY = "LpSolver.LpSolveResultKey";
    
    /**
     * Key to the or objects result in data store
     */
    public static final String OR_RESULT_KEY = "LpSolver.ORResultKey";
}
