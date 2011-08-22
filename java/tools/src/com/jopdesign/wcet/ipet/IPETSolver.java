/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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
import com.jopdesign.common.misc.MiscUtils;

import lpsolve.LpSolveException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Purpose: Invoke an ILP solver, to solve an IPET problem.
 * An IPET problem consists of:<ul/>
 * <li/> Execution Edges
 * <li/> Constraints involving edges
 * <li/> Costs for edges
 * </ul>
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 */
public class IPETSolver<T> {

    /**
     * If you use the BIGM method, be aware that you risk numeric instabilities for larger flows,
     * and that the method will fail to work if the total flow exceeds BIGM
     */
    public static final long BIGM = Long.MAX_VALUE;

	private static final boolean USE_PRESOLVE = true;

    private List<LinearConstraint<T>> edgeConstraints = new ArrayList<LinearConstraint<T>>();
    private Map<T, Long> edgeCost = new HashMap<T, Long>();
    private Set<T> edgeSet = new HashSet<T>();

    private HashMap<T, Integer> edgeIdMap = null;
    private HashMap<Integer, T> idEdgeMap = null;

    private File outDir;

    private String problemName;


    /**
     * @param problemName A name for the IPET Problem (for debugging/logging purposes)
     * @param config      Configuration for the ILP solver
     */
    public IPETSolver(String problemName, IPETConfig config) {
        this.problemName = problemName;
        outDir = config.doDumpIlp() ? config.getOutDir() : null;
    }

    public void addConstraint(LinearConstraint<T> lc) {
        this.edgeConstraints.add(lc);
        for (T edge : lc.getLinearVectorOnLHS().getCoeffs().keySet()) {
            this.edgeSet.add(edge);
        }
    }

    public void addConstraints(Collection<LinearConstraint<T>> cs) {
        for (LinearConstraint<T> lc : cs) addConstraint(lc);
    }

    public void addConstraints(Collection<LinearConstraint<T>> cs, String debugMsg) {
        for (LinearConstraint<T> lc : cs) {
            System.err.println("[constraint][" + debugMsg + "]: " + lc);
            addConstraint(lc);
        }
    }

    public void addEdgeCost(T e, long cost) {
        if (this.edgeCost.containsKey(e)) edgeCost.put(e, edgeCost.get(e) + cost);
        else edgeCost.put(e, cost);
        this.edgeSet.add(e);
    }

    /**
     * @param key
     * @return
     */
    public long getEdgeCost(T key) {
        if (edgeCost.containsKey(key)) return edgeCost.get(key);
        else return 0;
    }

	public Map<T, Long> getCostVector() {		
		return this.edgeCost;
	}

	public void setCostVector(Map<T, Long> edgeCost) {
		this.edgeCost = edgeCost;
	}

    /**
     * Solve the max cost network flow problem using {@link LpSolveWrapper}.
     *
     * @param flowMapOut if not null, write solution into this map, assigning a flow to each edge
     * @return the cost of the solution
     * @throws Exception if the ILP solver fails
     */
    public double solve(Map<T, Long> flowMapOut) throws LpSolveException {
    	return solve(flowMapOut, true);
    }

    /**
     * Solve the max cost network flow problem using {@link LpSolveWrapper}.
     *
     * @param flowMapOut if not null, write solution into this map, assigning a flow to each edge
     * @param isILP      if false, assumes all variables are rational (relaxed problem)
     * @return the cost of the solution
     * @throws LpSolveException 
     * @throws Exception if the ILP solver fails
     */
     public double solve(Map<T, Long> flowMapOut, boolean isILP) throws LpSolveException {
        IDProvider<Object> idProvider = this.generateMapping();
        LpSolveWrapper<Object> wrapper = new LpSolveWrapper<Object>(edgeSet.size(), isILP, idProvider);

        /* Add Constraints */
        for (LinearConstraint<T> lc : edgeConstraints) {
            wrapper.addConstraint(lc);
        }

        /* build cost objective */
        LinearVector<T> costVec = new LinearVector<T>();
        for (Entry<T, Long> entry : this.edgeCost.entrySet()) {

            long costFactor = entry.getValue();
            costVec.add(entry.getKey(), costFactor);
        }

        wrapper.setObjective(costVec, true);
        wrapper.freeze();

        File dumpFile = null;
        if (this.outDir != null) {
            try {
				dumpFile = dumpILP(wrapper);
			} catch (IOException e) {
				throw new LpSolveException("Failed to write ILP: " + e.getMessage());
			}
        }

        double sol;
        if (flowMapOut != null) {
            double[] objVec = new double[edgeSet.size()];
        	sol = Math.round(wrapper.solve(objVec));
        	for (int i = 0; i < idEdgeMap.size(); i++) {
                flowMapOut.put(idEdgeMap.get(i + 1), Math.round(objVec[i]));
            }
        } else {
        	try {
        		sol = Math.round(wrapper.solve(USE_PRESOLVE));
        	} catch(LpSolveException ex) {
        		throw new LpSolveException(ex.getMessage() + ". ILP dump: "+dumpFile);
        	}
        }
        return sol;
    }

    private File dumpILP(LpSolveWrapper<?> wrapper) throws LpSolveException, IOException {
        outDir.mkdirs();
        File outFile = File.createTempFile(MiscUtils.sanitizeFileName(this.problemName), ".lp", outDir);
        wrapper.dumpToFile(outFile);
        FileWriter fw = null;
        try {
            fw = new FileWriter(outFile, true);
        } catch (IOException e1) {
            throw new LpSolveException("Failed to open ILP file: "+e1.getMessage());
        }
        try {
            fw.append("/* Mapping: \n");
            for (Entry<T, Integer> e : this.edgeIdMap.entrySet()) {
                fw.append("    " + e.getKey() + " -> C" + e.getValue() + "\n");
            }
            fw.append(this.toString());
            fw.append("*/\n");
        } catch (IOException e) {
            throw new LpSolveException("Failed to write to ILP file: "+e.getMessage());
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                throw new LpSolveException("Failed to close ILP file: "+e.getMessage());
            }
        }
        return outFile;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Max-Cost-Flow problem with cost vector: ");
        boolean first = true;
        for (Entry<T, Long> e : edgeCost.entrySet()) {
            if (first) first = false;
            else s.append(" + ");
            s.append(e.getValue());
            s.append(' ');
            s.append(e.getKey());
        }
        s.append("\nFlow\n");
        for (LinearConstraint<T> lc : edgeConstraints) {
            s.append(lc);
            s.append('\n');
        }
        return s.toString();
    }

    /*-------------------------------------------------------------------------------------------
     * Why we do not need decision variables:
     *
     * We used them to say that if at least one execution edge in a set E has a frequency > 0,
     * the decision variable should be true, and associated some cost with the decision variable.
     *
     * But it is not necessary to use decision variables for this. Instead, add two cost variables
     * e_f and e_t for each edge e, and set e_f + e_t = e. Next, we will have a constraint on how
     * often the e_t edges are executed with respect to some linear expression lub.
     * This is modeled by adding a constraint {@code sum(e in E) e_t <= lub}. For decsision variables
     * lub is 1. Finally, we used to attribute cost c to the decision variable. This is done by
     * setting {@code cost(e_t) = cost(e_f) + cost(decision)}.
     *
     * Therefore, decision variables where unnecessary for all of the use cases we had. As they
     * additionally seem to add unneccessary complexity for the ILP solver, I've removed them.
     *
     *-------------------------------------------------------------------------------------------*/

    private IDProvider<Object> generateMapping() {
        this.edgeIdMap = new HashMap<T, Integer>();
        this.idEdgeMap = new HashMap<Integer, T>();

        int key = 1;
        for (T e : edgeSet) {
            edgeIdMap.put(e, key);
            idEdgeMap.put(key, e);
            key += 1;
        }
        /* create ID provider */
        return new IDProvider<Object>() {
            /* Note: No closures in java, so edgeIdvIdMap/idEdgeMap have to be instance variables */
            public Object fromID(int id) {
                return idEdgeMap.get(id);
            }

            public int getID(Object t) {
                return edgeIdMap.get(t);
            }
        };
    }



}
