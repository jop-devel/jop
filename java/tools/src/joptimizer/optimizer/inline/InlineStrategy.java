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
package joptimizer.optimizer.inline;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.struct.AppStruct;
import com.jopdesign.libgraph.struct.MethodInfo;
import joptimizer.config.JopConfig;
import joptimizer.framework.actions.ActionException;

import java.util.List;

/**
 * This interface will be used to implement the inlining strategy.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface InlineStrategy {

    void setup(InlineHelper helper, AppStruct appStruct, JopConfig config);

    InlineHelper getInlineHelper();

    AppStruct getAppStruct();

    InvokeResolver getInvokeResolver();

    void appendActionArguments(String actionId, List options);

    boolean configure(String actionName, String actionId, JopConfig config);

    /**
     * Initialize the algorithm. Called when the action is started.
     */
    void initialize() throws ActionException;

    /**
     * Get number of inlined methods (so far).
     * @return number of inlined methods.
     */
    int getInlineCount();

    void execute() throws ActionException;

    void execute(MethodInfo methodInfo, ControlFlowGraph graph) throws ActionException;
}
