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
package com.jopdesign.wcet08.ipet;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.jopdesign.wcet08.graphutils.IDProvider;
import com.jopdesign.wcet08.ipet.LinearConstraint.ConstraintType;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

/**
 * Simple, typed API for invoking LpSolve.
 * 
 * @param<T> type of variables. If you don't want typed variables, use {@link java.lang.Object}
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class LpSolveWrapper<T> {
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
	private static long solverTime = 0;
	public static double getSolverTime() { return ((double)solverTime)/1.0E9; }
	public static void resetSolverTime() { solverTime = 0; }

	private static Map<Integer,SolverStatus> readMap = null;
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
	private RawVector buildRawVector(LinearVector<T> inputVector) 
		throws LpSolveException {
		RawVector vec = new RawVector();
		vec.count = inputVector.size();
		vec.ixs = new int[vec.count];
		vec.coeffs = new double[vec.count];
		int i = 0;
		for(Entry<T,Long> e : inputVector.getCoeffs().entrySet()) {
			int objId = idProvider.getID(e.getKey());
			if(objId < 1 || objId > numVars) {
				throw new LpSolveException("Bad id: "+e+"has id "+objId+" not in [1.."+numVars+"]");
			}
			vec.ixs[i] = objId;
			vec.coeffs[i] = e.getValue();
			i++;
		}
		return vec;
	}

	private LpSolve lpsolve;
	private int numVars;
	private IDProvider<T> idProvider;
	
	/**
	 * Create a new (I)LP problem with the given number of variables. Note that
	 * variables are per default considered to be non-negative.
	 * @param numVars     number of variables
	 * @param idProvider  mapping variables to ids. The id of a variable has to be in the range
	 * 					  [1..numVars].
	 * @param intVars     if true, all variables are considered to be integral, otherwise rational
	 * @throws LpSolveException
	 */
	public LpSolveWrapper(int numVars, boolean intVars, IDProvider<T> idProvider) 
		throws LpSolveException {
		this.numVars = numVars;
		this.idProvider = idProvider;
		this.lpsolve = LpSolve.makeLp(0,numVars);

		lpsolve.setPrintSol(LpSolve.FALSE);
		lpsolve.setTrace(false);
		lpsolve.setDebug(false);
		lpsolve.setVerbose(LpSolve.SEVERE);

		for(int i = 1; i <= numVars; i++) {
			lpsolve.setInt(i, intVars);
		}
		lpsolve.setAddRowmode(true);
	}
	/**
	 * add a linear constraint to the the problem
	 * @param linearConstraint the linear constraint
	 * @throws LpSolveException
	 */
	public void addConstraint(LinearConstraint<T> linearConstraint) throws LpSolveException {
		LinearVector<T> row = linearConstraint.getLinearVectorOnLHS();
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
	public void setObjective(LinearVector<T> objVector, boolean doMax) 
		throws LpSolveException {
		RawVector rawVec = buildRawVector(objVector);
		this.lpsolve.setObjFnex(rawVec.count, rawVec.coeffs, rawVec.ixs);
		if(doMax) lpsolve.setMaxim();
		else      lpsolve.setMinim();
	}
	/**
	 * Solve the I(LP)
	 * @param objVec if non-null, write the solution into this array
	 * @return the objective value
	 * @throws LpSolveException
	 */
	public double solve(double[] objVec) throws LpSolveException {
		this.lpsolve.setAddRowmode(false);
		long start = System.nanoTime();
		int r = this.lpsolve.solve();
		long stop = System.nanoTime();
		LpSolveWrapper.solverTime +=(stop-start);
		SolverStatus st = getSolverStatus(r);
		if(st != SolverStatus.OPTIMAL) {
			throw new LpSolveException("Failed to solve LP problem: "+st);
		}
		if(objVec != null) this.lpsolve.getVariables(objVec);
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
	public void dumpToFile(File outFile) throws LpSolveException {
		outFile.delete();
		this.lpsolve.writeLp(outFile.getPath());		
	}
}
