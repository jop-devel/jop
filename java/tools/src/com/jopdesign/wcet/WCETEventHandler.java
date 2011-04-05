/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.wcet;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.EmptyAppEventHandler;
import com.jopdesign.common.KeyManager.CustomKey;
import com.jopdesign.common.KeyManager.KeyType;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.SymbolicMarker;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.wcet.annotations.BadAnnotationException;
import com.jopdesign.wcet.annotations.LoopBoundExpr;
import com.jopdesign.wcet.annotations.SourceAnnotationReader;
import com.jopdesign.wcet.annotations.SourceAnnotations;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;

/**
 * This class provides callback methods for AppInfo and performs the annotation loading.
 * TODO either rename this class or move the annotation-loading code into a dedicated class
 *      (which must then also contain dfaLoopBound()..)
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class WCETEventHandler extends EmptyAppEventHandler {

    public static final Logger logger = Logger.getLogger(WCETTool.LOG_WCET_PROJECT+".WCETEventHandler");

    // Using default loop bound will emit critical warning, but useful to
    // find all unbounded loop bounds
    public static final Long DEFAULT_LOOP_BOUND = 1024L;

    private CustomKey annotationKey;

    private WCETTool project;
    private SourceAnnotationReader annotationReader;

    public WCETEventHandler(WCETTool wcetTool) {
        this.project = wcetTool;
    }

    @Override
    public void onRegisterEventHandler(AppInfo appInfo) {
        // TODO attach annotations to CFG/blocks/instructions instead of classInfo
        annotationKey = appInfo.getKeyManager().registerKey(KeyType.STRUCT, "SourceAnnotations");
        annotationReader = new SourceAnnotationReader(project);
    }

    @Override
    public void onCreateControlFlowGraph(ControlFlowGraph cfg, boolean clean) {
        try {
            loadLoopAnnotations(cfg);
        } catch (BadAnnotationException e) {
            // TODO maybe do more than just log an error? (halt?)
            logger.error("Failed to load annotations for method "+cfg.getMethodInfo()+": "+e.getMessage(), e);
        }
    }

    public SourceAnnotations getAnnotations(ClassInfo cli) throws BadAnnotationException, IOException {
        SourceAnnotations annots = (SourceAnnotations) cli.getCustomValue(annotationKey);
        if(annots == null) {
            annots = annotationReader.readAnnotations(cli);
            cli.setCustomValue(annotationKey, annots);
        }
        return annots;

    }

    public void loadLoopAnnotations(ClassInfo classInfo) throws BadAnnotationException {
        for (MethodInfo method : classInfo.getMethods()) {
            if (!method.hasCode()) continue;
            loadLoopAnnotations(method.getCode().getControlFlowGraph(false));
        }
    }

    /**
     * load annotations for the flow graph.
     *
     * @param cfg the control flow graph of the method
     * @throws BadAnnotationException if an annotations is missing
     */
    public void loadLoopAnnotations(ControlFlowGraph cfg) throws BadAnnotationException {

    	SourceAnnotations wcaMap;
        MethodInfo method = cfg.getMethodInfo();
        MethodCode code = method.getCode();
        ExecutionContext eCtx = new ExecutionContext(cfg.getMethodInfo());
        
        try {
            wcaMap = getAnnotations(method.getClassInfo());
        } catch (IOException e) {
            throw new BadAnnotationException("IO Error reading annotation: " + e.getMessage(), e);
        }
        
        for (CFGNode n : cfg.getLoopColoring().getHeadOfLoops()) {
            BasicBlockNode headOfLoop = (BasicBlockNode) n;
            BasicBlock block = headOfLoop.getBasicBlock();
            // check if loopbound has already been loaded
            if (block.getLoopBound() != null) {
                // TODO maybe check if we already loaded annotations for this methodInfo before
                // or at least check if the source-annotation is tighter than what is currently set?
                continue;
            }
            // search for loop annotation in range
            int sourceRangeStart = code.getLineNumber(block.getFirstInstruction());
            int sourceRangeStop = code.getLineNumber(block.getLastInstruction());
            Collection<LoopBound> annots = wcaMap.annotationsForLineRange(sourceRangeStart, sourceRangeStop + 1);
            if (annots.size() > 1) {
                String reason = "Ambiguous Annotation [" + annots + "]";
                throw new BadAnnotationException(reason, block, sourceRangeStart, sourceRangeStop);
            }
            LoopBound loopAnnot = null;
            if (annots.size() == 1) {
                loopAnnot = annots.iterator().next();
            }
            // if we have loop bounds from DFA analysis, use them
            loopAnnot = dfaLoopBound(block, eCtx, loopAnnot);
            if (loopAnnot == null) {
// 		throw new BadAnnotationException("No loop bound annotation",
// 						 block,sourceRangeStart,sourceRangeStop);
                logger.error("No loop bound annotation: " + method + ":" + n +
                             ".\nApproximating with " + DEFAULT_LOOP_BOUND + ", but result is not safe anymore.");
                loopAnnot = LoopBound.boundedAbove(DEFAULT_LOOP_BOUND);
            }
            block.setLoopBound(loopAnnot);
        }
    }

    /**
     * Get a loop bound from the DFA for a certain loop and call string and
     * merge it with the annotated value.
     * @return The loop bound to be used for further computations
     */
    public LoopBound dfaLoopBound(BasicBlock headOfLoopBlock, ExecutionContext eCtx, LoopBound annotatedValue) {
    	
    	LoopBound dfaBound;
        LoopBounds lbs = project.getDfaLoopBounds();
        if(lbs != null) {
            MethodInfo methodInfo = headOfLoopBlock.getMethodInfo();
            // Insert a try-catch to deal with failures of the DFA analysis
            int bound;
            try {
                bound = lbs.getBound(project.getDfaTool(), headOfLoopBlock.getLastInstruction(),eCtx.getCallString());
            } catch(NullPointerException ex) {
                // TODO not cool ..
                ex.printStackTrace();
                bound = -1;
            }
            if(bound < 0) {
                logger.info("No DFA bound for " + methodInfo);
                dfaBound = annotatedValue;
            } else if(annotatedValue == null) {
                logger.info("Only DFA bound for "+methodInfo);
                dfaBound = LoopBound.boundedAbove(bound);
            } else {
                dfaBound = annotatedValue.clone();
                // More testing would be nice
                dfaBound.addBound(LoopBoundExpr.numUpperBound(bound), SymbolicMarker.LOOP_ENTRY); 
                long loopUb = annotatedValue.getSimpleLoopBound().upperBound(eCtx);
                if(bound < loopUb) {
                    logger.info("DFA analysis reports a smaller upper bound :"+bound+ " < "+loopUb+
                                " for "+methodInfo);
                } else if (bound > loopUb) {
                    logger.info("DFA analysis reports a larger upper bound: "+bound+ " > "+loopUb+
                                " for "+methodInfo);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("DFA and annotated loop bounds match for "+methodInfo);
                    }
                }
            }
        } else {
            dfaBound = annotatedValue;
        }
        return dfaBound;
    }

}
