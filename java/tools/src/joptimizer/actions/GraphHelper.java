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
package joptimizer.actions;

import joptimizer.config.BoolOption;
import joptimizer.config.ConfigurationException;
import joptimizer.config.JopConfig;
import joptimizer.config.StringOption;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractMethodAction;
import joptimizer.framework.actions.ActionException;
import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.struct.MethodCode;
import com.jopdesign.libgraph.struct.MethodInfo;

import java.util.List;

/**
 * Just a simple helper action to call some functions on graphs.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class GraphHelper extends AbstractMethodAction {

    public static final String ACTION_NAME = "graphhelper";

    public static final String CONF_TRANSFORM = "transform";
    public static final String CONF_MODIFY = "setmodify";

    private String transform;
    private boolean setModified;

    public GraphHelper(String name, JOPtimizer joptimizer) {
        super(name, joptimizer);
    }

    public void appendActionArguments(String prefix, List options) {
        options.add(new StringOption(prefix + CONF_TRANSFORM,
                "Transform the graph to the requested form (stack,quad,bytecode)", "form"));
        options.add(new BoolOption(prefix + CONF_MODIFY,
                "Set the modified flag to the graph."));
    }

    public String getActionDescription() {
        return "Utility-Action, provides some actions to perform on graphs.";
    }

    public boolean doModifyClasses() {
        return true;
    }

    public boolean configure(String prefix, JopConfig config) throws ConfigurationException {
        transform = config.getOption(prefix + CONF_TRANSFORM);
        setModified = config.isEnabled(prefix + CONF_MODIFY);
        return true;
    }

    public void execute(MethodInfo methodInfo) throws ActionException {
        MethodCode code = methodInfo.getMethodCode();
        if ( code == null ) {
            return;
        }

        if ( setModified ) {
            try {
                code.getGraph().setModified(true);
            } catch (GraphException e) {
                throw new ActionException("Could not get graph from method {"+methodInfo.getFQMethodName()+"}.", e);
            }
        }

        if ( "bytecode".equals(transform) ) {
            try {
                code.compileGraph();
            } catch (GraphException e) {
                throw new ActionException("Could not compile graph to bytecode.", e);
            }
        } else if ( transform != null && !"".equals(transform) ) {
            ControlFlowGraph graph;
            try {
                graph = code.getGraph();
            } catch (GraphException e) {
                throw new ActionException("Could not get graph from method {"+methodInfo.getFQMethodName()+"}.", e);
            }

            try {
                if ( "stack".equals(transform) ) {
                    graph.transformTo(ControlFlowGraph.TYPE_STACK);
                } else if ( "quad".equals(transform) ) {
                    graph.transformTo(ControlFlowGraph.TYPE_QUAD);
                }
            } catch (GraphException e) {
                throw new ActionException("Could not transform graph.", e);
            }
        }
    }

}
