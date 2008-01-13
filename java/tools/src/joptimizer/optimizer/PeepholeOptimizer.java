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

import joptimizer.config.JopConfig;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractGraphAction;
import joptimizer.framework.actions.ActionException;
import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.struct.MethodInfo;

import java.util.List;

/**
 * A peephole bytecode optimizer.
 * 
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class PeepholeOptimizer extends AbstractGraphAction {
    
    public static final String ACTION_NAME = "peephole";

    public PeepholeOptimizer(String name, JOPtimizer joptimizer) {
        super(name, joptimizer);
    }

    public void appendActionArguments(String prefix, List options) {
    }

    public String getActionDescription() {
        return "Run some peephole optimizations.";
    }

    public boolean doModifyClasses() {
        return true;
    }

    public boolean configure(String prefix, JopConfig config) {
        return false;
    }

    public int getDefaultStage() {
        return STAGE_STACK_TO_BYTECODE;
    }

    public int getRequiredForm() {
        return 0;
    }

    public void execute(MethodInfo methodInfo, ControlFlowGraph graph) throws ActionException {
    }
}
