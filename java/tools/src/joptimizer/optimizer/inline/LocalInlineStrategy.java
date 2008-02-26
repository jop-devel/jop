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
import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.statements.Statement;
import com.jopdesign.libgraph.cfg.statements.common.InvokeStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadReturn;
import com.jopdesign.libgraph.cfg.statements.stack.StackGoto;
import com.jopdesign.libgraph.cfg.statements.stack.StackInvoke;
import com.jopdesign.libgraph.cfg.statements.stack.StackReturn;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.MethodCode;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.TypeException;
import joptimizer.config.ArchTiming;
import joptimizer.config.BoolOption;
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
public class LocalInlineStrategy extends AbstractInlineStrategy {

    private class ResultContainer {

        private float gain;
        private CheckResult result;

        private ResultContainer(float gain, CheckResult result) {
            this.gain = gain;
            this.result = result;
        }
    }

    private class PriorityComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if ( ((ResultContainer) o1).gain > ((ResultContainer) o2).gain ) {
                return -1;
            } else if ( ((ResultContainer) o1).gain < ((ResultContainer) o2).gain ) {
                return 1;
            } else {
                return o1.hashCode() - o2.hashCode();
            }
        }
    }

    private static final String CONF_TOPDOWN = "topdown";

    private InvokeResolver resolver;
    private CallGraph callgraph;
    private int inlineCount;
    private boolean topDown;

    private static final Logger logger = Logger.getLogger(LocalInlineStrategy.class);

    public LocalInlineStrategy() {
        // TODO extend/wrap with own resolver, do callgraph thinning, use callgraph infos for resolving too
        resolver = new BasicInvokeResolver();
        topDown = false;
    }

    public InvokeResolver getInvokeResolver() {
        return resolver;
    }

    public void appendActionArguments(String actionId, List options) {
        options.add(new BoolOption(actionId, CONF_TOPDOWN,
                "Run callgraph in top-down order instead of bottom-up order."));
    }

    public boolean configure(String actionName, String actionId, JopConfig config) {
        topDown = config.isEnabled(actionName, actionId, CONF_TOPDOWN);
        return true;
    }

    public boolean doTopDown() {
        return topDown;
    }

    public void setTopDown(boolean topDown) {
        this.topDown = topDown;
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
            CGMethod method;

            if ( topDown ) {
                method = callgraph.getMethod(callgraph.getMethodCount() - (i--) );
            } else {
                method = callgraph.getMethod(--i);
            }

            MethodInfo methodInfo = method.getMethodInfo();
            if ( methodInfo.isAbstract() ) {
                continue;
            }

            try {
                MethodCode methodCode = methodInfo.getMethodCode();

                execute(methodInfo, methodCode.getGraph());
                methodCode.compileGraph();

            } catch (GraphException e) {
                if ( getJopConfig().doIgnoreActionErrors() ) {
                    logger.warn("Could not inline method {"+methodInfo.getFQMethodName()+"}, skipping.", e);
                } else {
                    throw new ActionException("Could not get CFG for method.", e);
                }
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

        evalInvokes(methodInfo, sorted, helper.findInlines(methodInfo, graph) );

        while ( !sorted.isEmpty() ) {
            ResultContainer rs = (ResultContainer) sorted.first();
            sorted.remove(rs);

            if ( !helper.checkSize(rs.result, currentSize) ) {
                continue;
            }

            // do inlining, calc new sizes, find new invokes
            if ( logger.isInfoEnabled() ) {
                logger.info("+ Inlining {" + rs.result.getInvokedMethod().getFQMethodName() + "}, size {" +
                        rs.result.getSrcCodeSize() + "}, gain {" + rs.gain + "}.");
            }
            InlineResult irs = helper.doInline(rs.result);
            currentSize = helper.calcMethodSize(rs.result, currentSize, irs);
            List parentMethods = helper.getParentMethods(rs.result);
            inlineCount++;
            modified = true;

            evalInvokes(methodInfo, sorted, helper.findInlines(methodInfo, graph, irs, parentMethods));
        }

        if ( modified ) {
            helper.setGraphModified(graph);
        }

        if ( logger.isInfoEnabled() ) {
            logger.info("Finished inlining, modified: {" + modified +"}");
        }
    }

    /**
     * Evaluate invocations, add to sorted invoke-list.
     * @param invoker the method which contains the invocation.
     * @param sorted the list of sorted and rated invoke checkresults.
     * @param invokes a list of checkresults of invocations.
     */
    private void evalInvokes(MethodInfo invoker, TreeSet sorted, Collection invokes) {

        for (Iterator it = invokes.iterator(); it.hasNext();) {
            CheckResult result = (CheckResult) it.next();

            float gain = evalInvoke(invoker, result);

            // TODO get threshold from options, should depend on optimize for speed/size
            // Do not inline if gain is too low (costs memory size only)
            if ( gain > 4.0f ) {
                sorted.add(new ResultContainer(gain, result));
            }
        }

    }

    /**
     * Evaluate a single (checked) invoke, calculate a gain value.
     *
     * @param invoker
     * @param result
     * @return
     */
    private float evalInvoke(MethodInfo invoker, CheckResult result) {

        // TODO this is a veeery simple if/loop detection, make better loop/nested if detection

        BasicBlock block = result.getStmt().getBlock();
        List prev = block.getIngoingEdges();

        // default values for no loop, no branch
        float pInvoke = 1.0f;
        int loop = 1;

        while ( prev.size() == 1 ) {
            block = ((BasicBlock.Edge)prev.get(0)).getSourceBlock();
            if ( block.getTargetCount() > 0 ) {
                pInvoke = 0.5f;
                break;
            }
            prev = block.getIngoingEdges();
        }
        if ( prev.size() > 1 ) {
            loop = 8;
        }

        float gain = calcGain(invoker, result, pInvoke, 1.0f, loop, 0);

        gain = applySizeHeuristics(invoker, result, gain);

        return gain;
    }

    private float applySizeHeuristics(MethodInfo invoker, CheckResult result, float gain) {

        // give small methods higher gain, should always be inlined
        // TODO get threshold and factor from options
        if ( result.getSrcCodeSize() < 6 ) {
            return gain * 1.5f;
        }

        CGMethod cgInvoker = callgraph.getMethod(invoker);
        CGMethod cgInvoked = callgraph.getMethod(result.getInvokedMethod());

        if ( cgInvoker == null || cgInvoked == null ) {
            logger.warn("Could not find invoker or invoked method in callgraph.");
            return gain;
        }

        int invokerCnt = cgInvoker.getInvokeCount(false);
        int invokedCnt = cgInvoked.getInvokeCount(true);

        // TODO get from options
        float expInvoker = 0.2f;
        float expInvoked = 0.2f;

        // more invokations of the invokers results in lower gain, as invokation with cache-miss is more expensive.
        // TODO make dependent on invoked-size?
        if ( invokerCnt > 0 && expInvoker > 0.0f ) {
            gain = (float) (gain / Math.pow(invokerCnt, expInvoker));
        }
        // if invoked method is invoked somewhere else, reduce gain so large methods which are often used will not
        // be inlined to save memory.
        if ( invokedCnt > 0 ) {
            gain = (float) (gain / Math.pow(invokedCnt, expInvoked));
        }

        return gain;
    }

    /**
     * Estimate the gain in speed by inlining. Higher values are better.
     *
     * @param invoker the method which contains the invocation.
     * @param rs the statement to check.
     * @param pInvoke the propability that the invoke will be reached at least once within the invoker method, 0 to 1.
     * @param pMiss estimated fraction of number of cache misses to total invokations of the invoker method, 0 to 1.
     * @param misses the number of expected invokes with cache misses.
     * @param hits the number of expected invokes with cache hits.
     * @return number of expected saved clock cycles per invoker invokation, a negative value means degradation of performance.
     */
    private float calcGain(MethodInfo invoker, CheckResult rs, float pInvoke, float pMiss, int misses, int hits) {

        InvokeStmt stmt = (InvokeStmt) rs.getStmt().getStatement();

        StackInvoke stackStmt;
        if ( stmt instanceof StackInvoke ) {
            stackStmt = (StackInvoke) stmt;
        } else {
            stackStmt = new StackInvoke(stmt.getMethodConstant(), stmt.getInvokeType());
        }

        int invokerSize = invoker.getMethodCode().getCodeSize();
        int methodSize = rs.getSrcCodeSize();
        int deltaSize = getInlineHelper().getCodeInliner().getDeltaBytecode(rs);

        ArchTiming timing = getJopConfig().getArchConfig().getArchTiming();
        int cyclesUncached = timing.getInvokeCycles(stackStmt, methodSize, true);
        int cyclesCached = timing.getInvokeCycles(stackStmt, methodSize, false);
        int cyclesLoad = timing.getCacheMemCycles(methodSize + deltaSize);
        
        int cyclesGoto = timing.getCycles(StackGoto.GOTO);
        int cyclesReturn = 0;

        // calculate cycles of all return statements in srcgraph, use 'worst case' (invoker is cached, gain is minimal)
        for (Iterator it = rs.getSrcGraph().getBlocks().iterator(); it.hasNext();) {
            BasicBlock block = (BasicBlock) it.next();
            for (Iterator it2 = block.getCodeBlock().getStatements().iterator(); it2.hasNext();) {
                Statement stmt2 = (Statement) it2.next();

                if ( stmt2 instanceof StackReturn ) {
                    cyclesReturn += timing.getReturnCycles((StackReturn) stmt2, invokerSize, false) - cyclesGoto;
                } else if ( stmt2 instanceof QuadReturn ) {
                    StackReturn ret = new StackReturn(((QuadReturn)stmt2).getType());
                    cyclesReturn += timing.getReturnCycles(ret, invokerSize, false) - cyclesGoto;
                }
            }
        }

        // gain increases by number of executions of the statement (cached and uncached)
        float gain = (float)(cyclesUncached * misses + cyclesCached * hits) * pInvoke;

        // gain also increases because returns are replaced with gotos
        gain += (float)(cyclesReturn * (misses + hits)) * pInvoke;

        // gain decreases as invoker method takes longer to load on cache miss
        gain -= (float)( cyclesLoad ) * pMiss;

        return gain;
    }

}
