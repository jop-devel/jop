/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package joptimizer.optimizer;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.cfg.block.CodeBlock;
import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.struct.MethodInfo;
import joptimizer.config.JopConfig;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractGraphAction;
import joptimizer.framework.actions.ActionException;
import joptimizer.optimizer.peephole.PeepGetfield;
import joptimizer.optimizer.peephole.PeepGoto;
import joptimizer.optimizer.peephole.PeepInc;
import joptimizer.optimizer.peephole.PeepLoadPop;
import joptimizer.optimizer.peephole.PeepNop;
import joptimizer.optimizer.peephole.PeepOptimization;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A peephole bytecode optimizer.
 * 
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class PeepholeOptimizer extends AbstractGraphAction {
    
    public static final String ACTION_NAME = "peephole";

    private List optimizer;
    private int[] matches;

    private static final Logger logger = Logger.getLogger(PeepholeOptimizer.class);

    public PeepholeOptimizer(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
    }

    public void appendActionArguments(List options) {
    }

    public String getActionDescription() {
        return "Run some peephole optimizations.";
    }

    public boolean doModifyClasses() {
        return true;
    }

    public boolean configure(JopConfig config) {

        optimizer = new LinkedList();

        // TODO get list of used optimizers from config

        optimizer.add(new PeepNop());
        optimizer.add(new PeepGoto());
        optimizer.add(new PeepGetfield());
        optimizer.add(new PeepInc());
        optimizer.add(new PeepLoadPop());

        return true;
    }

    public int getGraphStage() {
        return STAGE_STACK_TO_BYTECODE;
    }

    public int getRequiredForm() {
        return 0;
    }

    public void startAction() throws ActionException {
        for (Iterator it = optimizer.iterator(); it.hasNext();) {
            PeepOptimization optimization = (PeepOptimization) it.next();
            optimization.startOptimizer();
        }
        matches = new int[optimizer.size()];
    }

    public void finishAction() throws ActionException {
        int m = 0;
        for (Iterator it = optimizer.iterator(); it.hasNext();) {
            PeepOptimization optimization = (PeepOptimization) it.next();
            optimization.finishOptimizer();
            
            if (logger.isInfoEnabled()) {
                logger.info("Found {" + matches[m] + "} matches for {" + optimization.getClass().getName() + "}.");
            }
            m++;
        }
    }

    public void execute(MethodInfo methodInfo, ControlFlowGraph graph) throws ActionException {

        int m = 0;
        for (Iterator it = optimizer.iterator(); it.hasNext();) {
            PeepOptimization optimization = (PeepOptimization) it.next();

            if ( !optimization.startGraph(graph) ) {
                logger.warn("Could not start optimization {"+optimization.getClass().getName()+"}, skipped.");
                continue;
            }

            Class firstClass = optimization.getFirstStmtClass();

            for (int i = 0; i < graph.getBlockCount(); i++ ) {

                CodeBlock code = graph.getBlock(i).getCodeBlock();
				boolean match = false;

                for (int j = 0; j < code.size(); j++) {
                    StmtHandle handle = code.getStmtHandle(j);

                    if ( firstClass == null || firstClass.isAssignableFrom( handle.getStatement().getClass() ) ) {

                        StmtHandle next = optimization.processStatement(handle);

                        if ( next != null ) {
                            matches[m]++;
							match = true;
                            // TODO set loop to next
                            next.dispose();
                        }
                        
                    }

                    handle.dispose();
                }

				if (match) {
					graph.setModified(true);
				}
            }

            m++;
        }

		try {
			methodInfo.getMethodCode().compileGraph();
		} catch (GraphException e) {
			if ( getJopConfig().doIgnoreActionErrors() ) {
				logger.warn("Could peephole optimize {"+methodInfo.getFQMethodName()+"}, skipping.", e);
			} else {
				throw new ActionException("Could not get CFG for method.", e);
			}
		}

    }
}
