/**
 * 
 */
package wcet.components.lpsolver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import wcet.components.constraintsgen.IConstraintsGeneratorConstants;
import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.solver.IConstraint;
import wcet.framework.interfaces.solver.IConstraintTerm;
import wcet.framework.solver.LpSolveResult;
import wcet.framework.solver.ObjectiveFunction;

/**
 * @author Elena Axamitova
 * @version 0.1 24.05.2007
 * 
 * LP solver using the free lpsolve program. lpsolve has to be installed in the
 * system.
 */
public class LpSolveSolver implements IAnalyserComponent {
    /**
         * Shared data store.
         */
    private IDataStore dataStore;

    /**
         * The problem instance
         */
    private LpSolve lpSolve;

    /**
         * All constraints - from ConstraintsGenerator
         */
    private List<IConstraint> constraints;

    /**
         * Objecive function - from ConstraintsGenerator
         */
    private ObjectiveFunction objFunction;

    /**
         * saves mapping of variable name to column number that corresponds to
         * that variable in the problem
         */
    private HashMap<String, Integer> varNameToColNr = new HashMap<String, Integer>();

    public LpSolveSolver(IDataStore ds) {
	this.dataStore = ds;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#getOnlyOne()
         */
    public boolean getOnlyOne() {
	return true;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#getOrder()
         */
    public int getOrder() {
	return IGlobalComponentOrder.ILP_SOLVER
		+ ILpSolverConstants.LPSOLVE_SOLVER_ORDER;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#init()
         */
    public void init() throws InitException {
	// empty
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
    public String call() throws Exception {
	// get all needed data
	this.constraints = this.dataStore.getAllConstraints();
	this.objFunction = (ObjectiveFunction) this.dataStore
		.getObject(IConstraintsGeneratorConstants.OBJ_FUNCTION_KEY);

	this.createLpProblem();

	this.solveLpProblem();

	return "### LpSolve solver ended. ###";
    }

    private void solveLpProblem() throws LpSolveException {
	this.lpSolve.solve();
	/* a solution is calculated, now lets get some results */
	LpSolveResult result = new LpSolveResult();
	result.fill(this.lpSolve);
	this.dataStore.storeObject(ILpSolverConstants.LPSOLVE_RESULT_KEY,
		result);
    }

    private void createLpProblem() throws LpSolveException {
	HashMap<Integer, Double> constraintMap = new HashMap<Integer, Double>();
	// create an emty lp problem
	this.lpSolve = LpSolve.makeLp(0, 0);
	this.lpSolve.setLpName("LP Problem of " + this.dataStore.getTask()
		+ "." + this.dataStore.getMainMethodName());
	// the model is created in both modes
	// for a new constraint a new row is added
	// for every new variable a new column is added
	this.lpSolve.setAddRowmode(false);

	for (Iterator<IConstraint> iterator = this.constraints.iterator(); iterator
		.hasNext();) {
	    IConstraint currCnstr = iterator.next();
	    int rh = 0;
	    // transform the left side of the constraint - variable terms
                // stay left,
	    // constant terms go right (and change sign)
	    List<IConstraintTerm> leftSide = currCnstr.getLeftHandSide();
	    IConstraintTerm currTerm;
	    for (Iterator<IConstraintTerm> leftIterator = leftSide.iterator(); leftIterator
		    .hasNext();) {
		currTerm = leftIterator.next();
		if (currTerm.getVariable() == null) {
		    rh -= currTerm.getCoefficient();
		} else {
		    int currColNr = this.getColNrToVarName(currTerm
			    .getVariable());
		    double currColValue = currTerm.getCoefficient();
		    constraintMap.put(currColNr, currColValue);
		}
	    }
	    // transform the right side of the constraint - variable terms
                // go right
	    // and change sign, constant terms stay
	    List<IConstraintTerm> rightSide = currCnstr.getRightHandSide();
	    for (Iterator<IConstraintTerm> rightIterator = rightSide.iterator(); rightIterator
		    .hasNext();) {
		currTerm = rightIterator.next();
		if (currTerm.getVariable() == null) {
		    rh += currTerm.getCoefficient();
		} else {
		    int currColNr = this.getColNrToVarName(currTerm
			    .getVariable());
		    double currColValue = -1 * currTerm.getCoefficient();
		    constraintMap.put(currColNr, currColValue);
		}
	    }
	    // add the transformed constraint to the problem
	    this.addConstraintToProblem(currCnstr.getType(), currCnstr
		    .getName(), constraintMap, rh);
	    // prepare for next constraint
	    constraintMap.clear();
	}
	// transform the objective function
	for (Iterator<IConstraintTerm> objFuncIterator = this.objFunction
		.getRightHandSide().iterator(); objFuncIterator.hasNext();) {
	    IConstraintTerm currTerm = objFuncIterator.next();
	    int currColNr = this.getColNrToVarName(currTerm.getVariable());
	    double currColValue = currTerm.getCoefficient();
	    constraintMap.put(currColNr, currColValue);
	}
	// add the objective function to the problem
	this.addObjFuncToProblem(constraintMap);
    }

    /**
         * Transforms the HashMap of integers and doubles into arrays that
         * lpsolve understands and adds the objective function in lpsolve format to the
         * problem.
         * 
         * @param constraintMap -
         *                constraints
         * @throws LpSolveException -
         *                 passed from lpsolve
         */
    private void addObjFuncToProblem(HashMap<Integer, Double> constraintMap)
	    throws LpSolveException {
	if (this.objFunction.getType() == IConstraint.MAXIMIZE) {
	    this.lpSolve.setMaxim();
	} else {
	    this.lpSolve.setMinim();
	}
	int size = constraintMap.size();
	int[] colNr = new int[size];
	double[] rowValues = new double[size];
	int i = 0;
	for (Iterator<Integer> iterator = constraintMap.keySet().iterator(); iterator
		.hasNext();) {
	    int key = iterator.next();
	    colNr[i] = key;
	    rowValues[i] = constraintMap.get(key);
	    i++;
	}
	this.lpSolve.setObjFnex(size, rowValues, colNr);
    }
    /**
     * Transforms the HashMap of integers and doubles into arrays that
     * lpsolve understands and adds the constraint in lpsolve format to the
     * problem.
     * 
     * @param type - the type of the constraint (=,<,>=, ...)
     * @param name - optional name of the constraint
     * @param constraintMap - constraints
     * @param rh - constraint term
     * @throws LpSolveException - passed from lpsolve
     */
    private void addConstraintToProblem(int type, String name,
	    HashMap<Integer, Double> constraintMap, int rh)
	    throws LpSolveException {
	int size = constraintMap.size();
	int[] colNr = new int[size];
	double[] rowValues = new double[size];
	int i = 0;
	for (Iterator<Integer> iterator = constraintMap.keySet().iterator(); iterator
		.hasNext();) {
	    int key = iterator.next();
	    colNr[i] = key;
	    rowValues[i] = constraintMap.get(key);
	    i++;
	}
	int lpSolveType = 0;
	switch (type) {
	case IConstraint.EQUAL:
	    lpSolveType = LpSolve.EQ;
	    break;
	case IConstraint.LESS:
	case IConstraint.LESSEQUAL:
	    lpSolveType = LpSolve.LE;
	    break;
	case IConstraint.GREATER:
	case IConstraint.GREATEREQUAL:
	    lpSolveType = LpSolve.GE;
	    break;
	}
	this.lpSolve.addConstraintex(size, rowValues, colNr, lpSolveType, rh);
	if (name != null)
	    this.lpSolve.setRowName(this.lpSolve.getNrows(), name);
    }

    /**
     * Get the column number to the variable name. If not previously 
     * encountered, adds a new column to the problem.
     * @param varName
     * @return
     * @throws LpSolveException
     */
    private int getColNrToVarName(String varName) throws LpSolveException {
	Integer result = -1;
	result = this.varNameToColNr.get(varName);
	if (result == null) {
	    double[] colVal = new double[this.lpSolve.getNrows()];
	    this.lpSolve.addColumn(colVal);
	    result = this.lpSolve.getNcolumns();
	    this.varNameToColNr.put(varName, result);
	    this.lpSolve.setInt(result, true);
	    //TODO this sometimes throws access violation exception
	    //why, I do not know. It helps when debugging.
	    this.lpSolve.setColName(result, varName);
	}
	return result.intValue();
    }

    /**
     * Writes constraints to the file in lp format and makes 
     * the lpsolve read the problem from this file and solve it.
     * Not used right now.
     * the 
     * @throws LpSolveException
     */
    @SuppressWarnings("unused")
    private void createLpProblem2() throws LpSolveException {
	FileWriter tempLpWriter = null;
	String fileName = "problem_temp" + (new Random()).nextInt() + ".lp";
	try {
	    tempLpWriter = new FileWriter(fileName);
	} catch (IOException e) {
	    // ignore;
	}
	String objFuncString = "";
	objFuncString += this.objFunction.toString().replace('*', ' ') + ";\n";
	try {
	    tempLpWriter.write(objFuncString);
	} catch (IOException e1) {
	    // ignore;
	}
	for (Iterator<IConstraint> iterator = this.constraints.iterator(); iterator
		.hasNext();) {
	    IConstraint currConstraint = iterator.next();
	    String currConstrString = currConstraint.toString().replace('*',
		    ' ');
	    try {
		tempLpWriter.write(currConstrString + ";\n");
	    } catch (IOException e) {
		// ignore
	    }
	}
	try {
	    tempLpWriter.close();
	} catch (IOException e) {
	    // ignore;
	}
	this.lpSolve = LpSolve.readLp(fileName, LpSolve.CRITICAL,
		"LP Problem of " + this.dataStore.getTask() + "."
			+ this.dataStore.getMainMethodName());
	for (int i = 0; i < this.lpSolve.getNcolumns(); i++) {
	    this.lpSolve.setInt(i, true);
	}

    }

}
