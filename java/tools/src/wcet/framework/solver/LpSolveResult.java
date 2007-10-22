/**
 * 
 */
package wcet.framework.solver;

import java.util.HashMap;
import java.util.HashSet;

import lpsolve.LpSolve;

import wcet.framework.interfaces.solver.ILpResult;

/**
 * @author Elena Axamitova
 * @version 0.1 04.10.2007
 */
public class LpSolveResult implements ILpResult {

    private HashMap<String, Double> result;

    private String name;

    private double objValue = -1;

    private int status = ILpResult.UNSOLVED;

    public LpSolveResult() {
	this.result = new HashMap<String, Double>();
    }

    public void fill(LpSolve solution) {
	try {
	    this.name = solution.getLpName();
	    this.objValue = solution.getObjective();
	    this.status = solution.getStatus();
	    double[] row = new double[solution.getNcolumns()];
	    solution.getVariables(row);
	    for (int j = 0; j < row.length; j++)
		this.result.put(solution.getColName(j + 1), row[j]);
	} catch (Exception e) {
	    this.status = ILpResult.UNSOLVED;
	}
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.solver.ILpResult#getAllVarNames()
         */
    public HashSet<String> getAllVarNames() {
	return new HashSet<String>(this.result.keySet());
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.solver.ILpResult#getName()
         */
    public String getName() {
	return this.name;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.solver.ILpResult#getObjectiveValue()
         */
    public double getObjectiveValue() {
	return this.objValue;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.solver.ILpResult#getVarValue(java.lang.String)
         */
    public double getVarValue(String name) {
	Double result = this.result.get(name);
	if (result == null)
	    return 0;
	else
	    return result;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.solver.ILpResult#getStatus()
         */
    public int getStatus() {
	return this.status;
    }

}
