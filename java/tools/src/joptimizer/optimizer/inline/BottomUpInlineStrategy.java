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

import com.jopdesign.libgraph.callgraph.CGMethod;
import com.jopdesign.libgraph.callgraph.CallGraph;
import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.MethodCode;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.TypeException;
import joptimizer.config.JopConfig;
import joptimizer.framework.actions.ActionException;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * A simple inline selector which orders invocations by the code size per method.
 * Inlining is done in a callgraph bottom-up manner. 
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BottomUpInlineStrategy extends AbstractInlineStrategy {

    private class ResultContainer {

        private ResultContainer(int priority, CheckResult result) {
            this.priority = priority;
            this.result = result;
        }

        private int priority;
        private CheckResult result;
    }

    private class PriorityComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((ResultContainer)o1).priority - ((ResultContainer)o2).priority;
        }
    }

    private InvokeResolver resolver;
    private CallGraph callgraph;
    private int inlineCount;

    private static final Logger logger = Logger.getLogger(BottomUpInlineStrategy.class);

    public BottomUpInlineStrategy() {
        // TODO extend/wrap with own resolver, do callgraph thinning, use callgraph infos for resolving too
        resolver = new BasicInvokeResolver();
    }

    public InvokeResolver getInvokeResolver() {
        return resolver;
    }

    public void initialize() throws ActionException {

        inlineCount = 0;
        callgraph = new CallGraph(getAppStruct());

        JopConfig config = getJopConfig();
        try {
            // add main method as root
            ClassInfo root = getAppStruct().getClassInfo(config.getMainClassName(), false);
            MethodInfo main = root.getMethodInfo(config.getMainMethodSignature());
            callgraph.addRoot(main);

            // add all methods of other root classes as root
            for (Iterator it = config.getRootClasses().iterator(); it.hasNext();) {
                String className = (String) it.next();
                if ( !className.equals(config.getMainClassName()) ) {
                    root = getAppStruct().getClassInfo(className, false);
                    callgraph.addRoot(root);
                }
            }

            logger.info("Building callgraph .. ");
            callgraph.buildGraph();

            logger.info("Sorting methods by topology .. ");
            if ( !callgraph.sortMethods() ) {
                logger.warn("Could not sort methods, ignoring..");
            }

            logger.info("Done setting up callgraph.");

        } catch (TypeException e) {
            throw new ActionException("Could not load create calltree.", e);
        }
    }

    public int getInlineCount() {
        return inlineCount;
    }

    public void execute() throws ActionException {

        // walk through the call graph in reverse order, exec inlining on all methods and compile the graphs
        int i = callgraph.getMethodCount();
        while (i > 0) {
            CGMethod method = callgraph.getMethod(--i);

            MethodInfo methodInfo = method.getMethodInfo();
            if ( methodInfo.isAbstract() ) {
                continue;
            }

            try {
                MethodCode methodCode = methodInfo.getMethodCode();

                execute(methodInfo, methodCode.getGraph());
                methodCode.compileGraph();

            } catch (GraphException e) {
                // TODO maybe ignore single errors, just don't do inlining?
                throw new ActionException("Could not get CFG for method.", e);
            }
        }

    }

    public void execute(MethodInfo methodInfo, ControlFlowGraph graph) throws ActionException {

        if ( logger.isInfoEnabled() ) {
            logger.info("Starting inlining on {" + methodInfo.getFQMethodName() + "}");
        }

        InlineHelper helper = getInlineHelper();
        boolean modified = false;

        TreeSet sorted = new TreeSet(new PriorityComparator());
        int currentSize = methodInfo.getMethodCode().getCodeSize();

        evalInvokes(sorted, helper.findInlines(methodInfo, graph) );

        while ( !sorted.isEmpty() ) {
            ResultContainer rs = (ResultContainer) sorted.first();
            sorted.remove(rs);

            if ( !helper.checkSize(rs.result, currentSize) ) {
                continue;
            }

            // do inlining, calc new sizes, find new invokes
            if ( logger.isInfoEnabled() ) {
                logger.info("+ Inlining {" + rs.result.getInvokedMethod().getFQMethodName() + "}, size {" +
                        rs.result.getSrcCodeSize() + "}");
            }
            InlineResult irs = helper.doInline(rs.result);
            currentSize = helper.calcMethodSize(rs.result, currentSize, irs);
            List parentMethods = helper.getParentMethods(rs.result);
            inlineCount++;
            modified = true;

            evalInvokes(sorted, helper.findInlines(methodInfo, graph, irs, parentMethods));
        }

        if ( modified ) {
            helper.setGraphModified(graph);
        }

        if ( logger.isInfoEnabled() ) {
            logger.info("Finished inlining, modified: {" + modified +"}");
        }
    }

    /**
     * Evaluate invocations, add to  
     * @param sorted
     * @param invokes
     */
    private void evalInvokes(TreeSet sorted, Collection invokes) {

        for (Iterator it = invokes.iterator(); it.hasNext();) {
            CheckResult result = (CheckResult) it.next();
            int priority = evalInvoke(result);
            sorted.add(new ResultContainer(priority, result));
        }

    }

    private int evalInvoke(CheckResult result) {
        // TODO tune evaluation function
        return result.getSrcCodeSize();
    }

}
