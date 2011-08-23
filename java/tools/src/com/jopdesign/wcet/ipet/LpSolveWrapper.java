/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.jopdesign.wcet.ipet;

import com.jopdesign.common.graphutils.IDProvider;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Simple, typed API for invoking LpSolve.
 *
 * @param <T> type of variables. If you don't want typed variables, use {@link java.lang.Object}
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class LpSolveWrapper<T> {
	
	/* FIXME: do know yet how to get correct information on variables after presolving.
	 * Therefore, presolving is only enabled if you just need a objective value */
	private static final int PRESOLVE_OPTIONS =
		  0
	  //  | LpSolve.PRESOLVE_COLS
	  //  | LpSolve.PRESOLVE_ROWS
	  //  |  LpSolve.PRESOLVE_LINDEP | LpSolve.PRESOLVE_SOS 
	      | LpSolve.PRESOLVE_ELIMEQ2    // doc says may lead to numerical stability problems; 
		                              // this is the presolver that brings significant speedup
	   // | LpSolve.PRESOLVE_PROBEFIX   // try to fix binary variables (little speedup)
	   // | LpSolve.PRESOLVE_PROBEREDUCE
	      | LpSolve.PRESOLVE_ROWDOMINATE
	  //  | LpSolve.PRESOLVE_COLDOMINATE
		;

	/**
	 * Status of the lp solver (typed copy of basic LP solve status numbers)
	 */
	public enum SolverStatus {
		NOMEMORY(LpSolve.NOMEMORY), OPTIMAL(0), SUBOPTIMAL(1), INFEASIBLE(2), UNBOUNDED(3),
		DEGENERATE(4), NUMFAILURE(5), USERABORT(6), TIMEOUT(7),
		PRESOLVED(9),PROCFAIL(10),PROCBREAK(11),FEASFOUND(12),NOFEASFOUND(13),
		UNKNOWN(-1);
		int statusCode;
		SolverStatus(int c) {
			this.statusCode = c;
		}
	}
	private static final long LP_SOLVE_SEC_TIMEOUT = 1200;
	private static long solverTime = 0, resetSolverTime = 0;

	/**
	 * Get time spend in the solver since the last call to {@link #resetSolverTime()}
     *
	 * @return the time spend in the solver in seconds
	 */
	public static double getSolverTime() { return solverTime/1.0E9; }

	public static double getTotalSolverTime() { return (resetSolverTime+solverTime)/1.0E9; }
	
	/**
	 * Reset the solver time statistic to 0 (does not affect totalSolverTime).
	 */
	public static void resetSolverTime() { resetSolverTime += solverTime; solverTime = 0; }

	
	private static Map<Integer,SolverStatus> readMap = null;

	/**
	 * Wrap the return code of lp_solve into a {@link SolverStatus} variable.
	 * @param code the code returned by the solver
	 * @return
	 */
	public static SolverStatus getSolverStatus(int code) {
		if(readMap == null) {
			readMap = new TreeMap<Integer,SolverStatus>();
			for(SolverStatus ss : SolverStatus.values()) {
				readMap.put(ss.statusCode, ss);
			}
		}
		SolverStatus status = readMap.get(code);
		if(status == null) return SolverStatus.UNKNOWN;
		else return status;
	}

	private class RawVector {
		int count;
		int[] ixs;
		double[] coeffs;
	}
	private RawVector buildRawVector(LinearVector<? extends T> inputVector)
		throws LpSolveException {
		RawVector vec = new RawVector();
		vec.count = inputVector.size();
		vec.ixs = new int[vec.count];
		vec.coeffs = new double[vec.count];
		int i = 0;
		for(Entry<? extends T,Long> e : inputVector.getCoeffs().entrySet()) {
			int objId = idProvider.getID(e.getKey());
			if(objId < 1 || objId > numVars) {
				throw new LpSolveException("Bad id: "+e+"has id "+objId+" not in [1.."+numVars+"]");
			}
			vec.ixs[i] = objId;
			long val = e.getValue();
			double dval;
			// FIXME: The Big M method is extremely sensitive to numeric instabilities
			// 1E7 works fine in practice, but may fail on arbitrary problems
			// Should do some research to find whether there are solutions to this problem,
			// but in general better avoid Big M and use statically derived constants
			if(val == Long.MAX_VALUE) {
				dval = 1.0E7;
			} else if (val == Long.MIN_VALUE) {
				dval = - (1.0E7);
			} else {
				dval = (double)val;
			}
			vec.coeffs[i] = dval;
			i++;
		}
		return vec;
	}

	private LpSolve lpsolve;
	private int numVars;
	private IDProvider<T> idProvider;
	private boolean isILP;

	/**
	 * Create a new (I)LP problem with the given number of variables. Note that
	 * variables are per default considered to be non-negative.
	 * @param numVars     number of variables
	 * @param idProvider  mapping variables to ids. The id of a variable has to be in the range
	 * 					  [1..numVars].
	 * @param isILP  if true, solves the ILP problem, otherwise the relaxed problem
	 * @throws LpSolveException
	 */
	public LpSolveWrapper(int numVars, boolean isILP, IDProvider<T> idProvider)
		throws LpSolveException {
		this.numVars = numVars;
		this.idProvider = idProvider;
		this.lpsolve = LpSolve.makeLp(0,numVars);
		this.isILP = isILP;
		
		lpsolve.setPrintSol(LpSolve.FALSE);
		lpsolve.setTrace(false);
		lpsolve.setDebug(false);
		lpsolve.setVerbose(LpSolve.SEVERE);

		for(int i = 1; i <= numVars; i++) {
			lpsolve.setInt(i, isILP ? true : false);
		}
		lpsolve.setAddRowmode(true);
	}

	/**
	 * add a linear constraint to the the problem
	 * @param linearConstraint the linear constraint
	 * @throws LpSolveException
	 */
	public void addConstraint(LinearConstraint<? extends T> linearConstraint) throws LpSolveException {
		LinearVector<? extends T> row = linearConstraint.getLinearVectorOnLHS();
		RawVector rawVector = buildRawVector(row);
		int constrType = mapConstraintType(linearConstraint.getConstraintType());
		double rh = linearConstraint.getInhomogenousTermOnRHS();
		this.lpsolve.addConstraintex(rawVector.count, rawVector.coeffs, rawVector.ixs, constrType , rh);
	}

	/**
	 * Set the objective of the (I)LP problem.
	 * @param <T> Type of variables
	 * @param objVector the objective vector
	 * @param doMax whether to maximize (if false, minimize)
	 * @throws LpSolveException
	 */
	public void setObjective(LinearVector<? extends T> objVector, boolean doMax)
		throws LpSolveException {
		RawVector rawVec = buildRawVector(objVector);
		this.lpsolve.setObjFnex(rawVec.count, rawVec.coeffs, rawVec.ixs);
		if(doMax) lpsolve.setMaxim();
		else      lpsolve.setMinim();
	}

	/**
	 * Turn row mode off - changes are expensive now,
	 * but possible to dump ILP
	 */
	public void freeze() {
		this.lpsolve.setAddRowmode(false);
	}

	private class SolverThread extends Thread {
		int result = -1;
		long solverTime = 0;
		LpSolveException exception = null;

		public SolverThread() {}
		@Override
		public void run() {
			long start = System.nanoTime();
			try {
				result = lpsolve.solve();
			} catch (LpSolveException e) {
				exception = e;
			}
			long stop = System.nanoTime();			
			solverTime = stop-start;
		}		
	}
		
	/**
	 * Solve the I(LP)
	 * @param objVec if non-null, write the solution into this array
	 * @return the objective value
	 * @throws LpSolveException
	 */
	public double solve(double[] objVec) throws LpSolveException {
		return solve(objVec, false);
	}

	
	/**
	 * Solve the I(LP)
	 * @param presolve whether to use presolving
	 * @return the objective value
	 * @throws LpSolveException
	 */
	public double solve(boolean preSolve) throws LpSolveException {
		return solve(null, preSolve);
	}

	/* either presolving, or a solution vector (at the moment) */
	private double solve(double[] objVec, boolean preSolve) throws LpSolveException {
		freeze();
		
		/* Presolving gives a speedup of factor 5 on the 'min-cache-blocks' problem for StartKfl */
		/* But, the flow in the variables seems to be wrong; need to check whether this can be fixed */
		if(preSolve) {
			lpsolve.setPresolve(PRESOLVE_OPTIONS,lpsolve.getPresolveloops());
		}
		lpsolve.setTimeout(LP_SOLVE_SEC_TIMEOUT);

	    SolverThread thr = new SolverThread();
		thr.start();
    	int cnt=0;
	    while(true) {
	    	boolean interrupted = false;
		    try {
				thr.join(1000);
			} catch (InterruptedException e) {
				interrupted = true;
			}
		    if(! thr.isAlive()) {
		    	break;
		    }
		    if(!interrupted) {
		    	if(++cnt == 1) {		    		
			    	System.err.print("LP Solve: Hard Problem, calculating: .");
			    	System.err.flush();
		    	} else {
		    		System.err.print(".");
			    	System.err.flush();
		    	}
		    }
	    }
	    if(cnt>0) System.err.println(cnt+" seconds");

		LpSolveWrapper.solverTime += (thr.solverTime);
		SolverStatus st = getSolverStatus(thr.result);
		if(objVec != null) this.lpsolve.getVariables(objVec);
		if(st != SolverStatus.OPTIMAL) {
			if(objVec != null) {
				int i = 0;
				for(double obj : objVec) {
					System.out.println(String.format("Objective entry %d: %.2f",i++,obj));
				}
			}
			throw new LpSolveException("Failed to solve LP problem: status="+st+", exc="+thr.exception);
			//		(objVec != null ? Arrays.toString(objVec) : " no info "));
		}
		return this.lpsolve.getObjective();
	}

	private int mapConstraintType(ConstraintType constraintType) {
		switch(constraintType) {
			case Equal : return LpSolve.EQ;
			case GreaterEqual : return LpSolve.GE;
			case LessEqual : return LpSolve.LE;
			default: throw new AssertionError("unexpected constraint type: "+constraintType);
		}
	}

	/**
	 * Dump the (I)LP problem to the given file
	 * @param outFile
	 * @throws LpSolveException
	 */
	public void dumpToFile(File outFile) throws LpSolveException {
		outFile.delete();
		try {
			this.lpsolve.writeLp(outFile.getPath());
		} catch(LpSolveException ex) {
			Logger.getLogger(this.getClass()).error("Failed to dump LP Solve Problem to "+outFile.getPath()+": "+ex);
		}
	}

	/**
	 * Mark the given variable (after adding it) as being binary
	 * @param dv
	 */
	public void setBinary(T dv) {
		if(isILP) {
			try {
				this.lpsolve.setBinary(this.idProvider.getID(dv), true);
			} catch (LpSolveException e) {
				throw new AssertionError("setBinary failed for dv "+dv);
			}
		}
	}
}
