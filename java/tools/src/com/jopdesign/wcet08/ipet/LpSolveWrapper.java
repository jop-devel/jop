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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import com.jopdesign.wcet08.ipet.LinearConstraint.ConstraintType;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class LpSolveWrapper {
	private class RawVector {
		int count;
		int[] ixs;
		double[] coeffs;
	}
	private<T> RawVector buildRawVector(LinearVector<T> inputVector) {
		RawVector vec = new RawVector();
		vec.count = inputVector.size();
		vec.ixs = new int[vec.count];
		vec.coeffs = new double[vec.count];
		int i = 0;
		for(Entry<T,Long> e : inputVector.getCoeffs().entrySet()) {
			vec.ixs[i] = objMapping.get(e.getKey());
			vec.coeffs[i] = e.getValue();
			i++;
		}
		return vec;
	}

	private LpSolve lpsolve;
	private Map<? extends Object, Integer> objMapping;
	private int numVars;

	public LpSolveWrapper(Map<?,Integer> objMapping, boolean intVars) 
		throws LpSolveException {
		checkMapping(objMapping);
		this.numVars = objMapping.size();
		this.lpsolve = LpSolve.makeLp(0,numVars);
		lpsolve.setPrintSol(0);
		lpsolve.setTrace(false);
		lpsolve.setDebug(false);
		lpsolve.setVerbose(3);
//		lpsolve.putMsgfunc(null,null,0);
		for(int i = 1; i <= numVars; i++) {
			lpsolve.setInt(i, intVars);
		}
		this.objMapping = objMapping;
	}
	/**
	 * Check that the objet map is valid: its codomain has to be a consecutive range of
	 * integers. Rather expensive, so should only be run in debugging mode.
	 * @param objmap
	 * @throws LpSolveException when the map isn't valid
	 */
	private void checkMapping(Map<?, Integer> objmap) throws LpSolveException {
		List<Integer> vals = new Vector<Integer>(objmap.values());
		Collections.sort(vals);
		if(vals.isEmpty()) {
			throw new LpSolveException("Empty problem");
		} else if(vals.get(0) != 1) {
			throw new LpSolveException("First variable has to mapped to 1");
		}
		for(int i = 1; i<vals.size();i++) {
			if(vals.get(i-1)+1 != vals.get(i)) {
				throw new LpSolveException("Objects have to be mapped to a consecutive list of integers");
			}
		}
	}
	public<T> void addConstraint(LinearConstraint<T> lc) throws LpSolveException {
		LinearVector<T> rowVector = lc.getLinearVectorOnLHS();
		RawVector rawVec = buildRawVector(rowVector);
		int constrType = mapConstraintType(lc.getConstraintType());
		double rh = lc.getInhomogenousTermOnRHS();
		this.lpsolve.addConstraintex(rawVec.count, rawVec.coeffs, rawVec.ixs, constrType , rh);
	}
	public<T> void setObjective(LinearVector<T> vec) throws LpSolveException {
		RawVector rawVec = buildRawVector(vec);
		this.lpsolve.setObjFnex(rawVec.count, rawVec.coeffs, rawVec.ixs);
	}
	public double solve(double[] objVec) throws LpSolveException {
		lpsolve.setMaxim();
//		if(logger.getLevel().isGreaterOrEqual(Level.TRACE)) {
//			try {
//				for(Entry e : this.objMapping.entrySet()) {
//					logger.trace("[LP] --" + e);
//				}
//				File tmpFile = File.createTempFile("LpSolveWrapper", ".lp");
//				lpsolve.writeLp(tmpFile.toString());
//				BufferedReader frw = new BufferedReader(new FileReader(tmpFile));
//				String l;
//				while(null != (l=frw.readLine())) { logger.trace("[LP] "+l); }
//			} catch(Exception e) {
//				logger.error("Failed to write and log .lp file");
//			}
//		}
		
		int r = this.lpsolve.solve();
		switch(r) {
		case LpSolve.OPTIMAL: break;
		default: throw new LpSolveException("Unexpected return code from solve(): "+r);
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
}
