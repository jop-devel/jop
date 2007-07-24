/**
 * 
 */
package wcet.components.outputwr;

import java.io.PrintStream;

import lpsolve.LpSolve;
import wcet.components.lpsolver.ILpSolverConstants;
import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;

/**
 * @author Elena Axamitova
 * @version 0.1 26.05.2007
 * 
 * Writes the relevant result data of the lp solver lpsolve to the output.
 */
public class LpSolveResultWriter implements IAnalyserComponent {
    /**
     * Shared data store.
     */
    private IDataStore dataStore;
    
    /**
     * Analyser output
     */
    private PrintStream output;

    public LpSolveResultWriter(IDataStore ds){
	this.dataStore = ds;
    }
    
    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#getOnlyOne()
     */
    public boolean getOnlyOne() {
	return false;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#getOrder()
     */
    public int getOrder() {
	return IGlobalComponentOrder.OUTPUT_WRITER;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#init()
     */
    public void init() throws InitException {
	this.output = this.dataStore.getOutput();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    public String call() throws Exception {
	LpSolve problem = (LpSolve) this.dataStore.getObject(ILpSolverConstants.LPSOLVE_RESULT_KEY);
	/* objective value */
	this.output.println(problem.getStatustext(problem.getStatus()));
        this.output.println("Objective value: " + problem.getObjective());
        /* variable values */
        double[] row = new double[problem.getNcolumns()];
        problem.getVariables(row);
        for(int j = 0; j < row.length; j++)
            this.output.println(problem.getColName(j + 1) + ": " + row[j]);
        /* we are done now */
        this.output.flush();
	return "$$$ LP writer for lpsolve result ended. $$$";
    }

}
