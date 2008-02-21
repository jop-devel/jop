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

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.struct.MethodCode;
import com.jopdesign.libgraph.struct.MethodInfo;
import joptimizer.config.ConfigurationException;
import joptimizer.config.JopConfig;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractMethodAction;
import joptimizer.framework.actions.ActionCollection;
import joptimizer.framework.actions.ActionException;
import joptimizer.framework.actions.GraphAction;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a {@link joptimizer.framework.actions.MethodAction} which
 * uses several other actions to transform the code of a method using several other
 * Actions in a given order.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class MethodTransformAction extends AbstractMethodAction implements ActionCollection {

    public static final String ACTION_NAME = "codetransform";

    private List[] actions;

    private static final Logger logger = Logger.getLogger(MethodTransformAction.class);

    public MethodTransformAction(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);

        actions = new List[3];
        for (int i = 0; i < actions.length; i++) {
            actions[i] = new LinkedList();
        }
    }

    public void appendActionArguments(List options) {
        for (int i = 0; i < actions.length; i++) {
            for (Iterator it = actions[i].iterator(); it.hasNext();) {
                ((GraphAction)it.next()).appendActionArguments(options);
            }
        }
    }

    public String getActionDescription() {
        return "Transforms methodcode by several GraphActions.";
    }

    public boolean doModifyClasses() {
        return true;
    }

    public boolean configure(JopConfig config) throws ConfigurationException {
        boolean configured = true;
        for (int i = 0; i < actions.length; i++) {
            for (Iterator it = actions[i].iterator(); it.hasNext();) {
                boolean ret = ((GraphAction)it.next()).configure(config);
                configured &= ret;
            }
        }
        return configured;
    }

    /**
     * add a graph to the transformation stage, as defined by the
     * constants in {@link GraphAction}.
     * @param stage the stage during which this action should be executed.
     * @param action the action to execute.
     */
    public void addAction(int stage, GraphAction action) {
        actions[stage].add(action);
    }


    public void startAction() throws ActionException {
        for (int i = 0; i < actions.length; i++) {
            Iterator it = actions[i].iterator();
            while (it.hasNext()) {
                GraphAction action = (GraphAction) it.next();
                action.startAction();
            }
        }
    }

    public void finishAction() throws ActionException {
        for (int i = 0; i < actions.length; i++) {
            Iterator it = actions[i].iterator();
            while (it.hasNext()) {
                GraphAction action = (GraphAction) it.next();
                action.finishAction();
            }
        }
    }

    public void execute(MethodInfo methodInfo) throws ActionException {

        MethodCode code = methodInfo.getMethodCode();
        if ( code == null ) {
            return;
        }

        ControlFlowGraph graph;
        try {
            graph = code.getGraph();
        } catch (GraphException e) {
            if ( getJopConfig().doIgnoreActionErrors() ) {
                Logger.getLogger(this.getClass()).warn("Could not create graph of {"+
                        methodInfo.getFQMethodName()+"}, skipping.", e);
                return;
            } else {
                throw new ActionException("Could not create graph for method {"+
                        methodInfo.getFQMethodName()+"}.", e);
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Running actions on method {" + methodInfo.getFQMethodName() + "}.");
        }

        // run actions on stack graph
        execActions(methodInfo, graph, GraphAction.STAGE_STACK_TO_QUAD);

        // perform actions on quadruple code
        if ( actions[GraphAction.STAGE_QUAD].size() > 0 ) {

            try {
                graph.transformTo(ControlFlowGraph.TYPE_QUAD);

                execActions(methodInfo, graph, GraphAction.STAGE_QUAD);

                graph.transformTo(ControlFlowGraph.TYPE_STACK);
            } catch (GraphException e) {
                if ( getJopConfig().doIgnoreActionErrors() ) {
                    Logger.getLogger(this.getClass()).warn("Could not transform graph to quad-form of {"+
                            methodInfo.getFQMethodName()+"}, skipping.", e);
                    return;
                } else {
                    throw new ActionException("Could not transform graph to quad-form for method {"+
                            methodInfo.getFQMethodName()+"}.", e);
                }
            }
        }

        execActions(methodInfo, graph, GraphAction.STAGE_STACK_TO_BYTECODE);

        try {
            code.compileGraph();
        } catch (GraphException e) {
            if ( getJopConfig().doIgnoreActionErrors() ) {
                Logger.getLogger(this.getClass()).warn("Could not compile graph of {"+
                        methodInfo.getFQMethodName()+"}, skipping.", e);
            } else {
                throw new ActionException("Could not compile graph for method {"+methodInfo.getFQMethodName()+"}.", e);
            }
        }
    }

    public Collection getActions() {

        // collect all actions from all steps
        List list = new LinkedList();

        for (int i = 0; i < actions.length; i++) {
            list.addAll(actions[i]);
        }

        return list;
    }

    private void execActions(MethodInfo methodInfo, ControlFlowGraph graph, int stage) throws ActionException {
        Iterator it = actions[stage].iterator();
        while (it.hasNext()) {
            GraphAction action = (GraphAction) it.next();
            
            if (logger.isInfoEnabled()) {
                logger.info("Starting sub-action {" + action.getActionName() +
                        "} in stage {" + getStageName(stage) + "}.");                
            }

            action.execute(methodInfo, graph);

            if (logger.isInfoEnabled()) {
                logger.info("Finished sub-action {" + action.getActionName() + "}.");                
            }
        }
    }

    private String getStageName(int stage) {
        switch ( stage ) {
            case GraphAction.STAGE_QUAD: return "quad";
            case GraphAction.STAGE_STACK_TO_BYTECODE: return "stack->bytecode";
            case GraphAction.STAGE_STACK_TO_QUAD: return "stack->quad";            
        }
        return "";
    }
}
