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
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.code.SymbolicMarker;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.wcet.annotations.BadAnnotationException;
import com.jopdesign.wcet.annotations.LoopBoundExpr;
import com.jopdesign.wcet.annotations.SourceAnnotationReader;
import com.jopdesign.wcet.annotations.SourceAnnotations;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

    private Set<BasicBlock> printedLoopBoundInfoMessage = new HashSet<BasicBlock>();

    private boolean ignoreMissingLoopBounds = false;

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
    public void onCreateControlFlowGraph(ControlFlowGraph cfg) {
        try {
            loadLoopAnnotations(cfg);
        } catch (BadAnnotationException e) {
            // TODO should we throw an error here instead?
            // Since later analyses will expect loopbounds, they will most likely fail in some obscure way..
            logger.fatal("Failed to load loopbounds for method "+cfg.getMethodInfo()+": "+e.getMessage(), e);
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

    public void setIgnoreMissingLoopBounds(boolean ignoreMissingLoopBounds) {
        this.ignoreMissingLoopBounds = ignoreMissingLoopBounds;
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
        
        for (CFGNode n : cfg.getLoopColoring().getHeadOfLoops()) {
            BasicBlockNode headOfLoop = (BasicBlockNode) n;
            BasicBlock block = headOfLoop.getBasicBlock();
            // check if loopbound has already been loaded
            if (block.getLoopBound() != null) {
                // TODO maybe check if we already loaded annotations for this methodInfo before
                // or at least check if the source-annotation is tighter than what is currently set?
                continue;
            }

            Set<LoopBound> bounds = new HashSet<LoopBound>(2);

            InstructionHandle first = block.getFirstInstruction();
            InstructionHandle last = first;
            ClassInfo sourceInfo = method.getCode().getSourceClassInfo(block.getFirstInstruction());

            for (InstructionHandle ih : block.getInstructions()) {
                ClassInfo cls = method.getCode().getSourceClassInfo(ih);
                boolean isLast = ih.equals(block.getLastInstruction());
                if (!cls.equals(sourceInfo) || isLast) {
                    try {
                        wcaMap = getAnnotations(method.getCode().getSourceClassInfo(block.getFirstInstruction()));
                    } catch (IOException e) {
                        throw new BadAnnotationException("IO Error reading annotation: " + e.getMessage(), e);
                    }
                    if (isLast) {
                        last = ih;
                    }
                    // search for loop annotation in range
                    int sourceRangeStart = code.getLineNumber(first);
                    int sourceRangeStop = code.getLineNumber(last);
                    bounds.addAll(wcaMap.annotationsForLineRange(sourceRangeStart, sourceRangeStop + 1));

                    first = ih;
                }
                last = ih;
            }

            if (bounds.size() > 1) {
                String reason = "Ambiguous Annotation [" + bounds + "]";
                throw new BadAnnotationException(reason, code, block);
            }
            LoopBound loopAnnot = null;
            if (bounds.size() == 1) {
                loopAnnot = bounds.iterator().next();
            }
            // if we have loop bounds from DFA analysis, use them
            loopAnnot = dfaLoopBound(block, eCtx, loopAnnot);
            if (loopAnnot == null) {
// 		throw new BadAnnotationException("No loop bound annotation",
// 						 block,sourceRangeStart,sourceRangeStop);
                // Bit of a hack: if we load CFGs before the callgraph is constructed, this will log errors anyway
                if (ignoreMissingLoopBounds) {
                    logger.trace("No loop bound annotation: " + method + ":" + n +
                            " " + getLineRangeText(code, block) +
                            ".\nApproximating with " + DEFAULT_LOOP_BOUND + ", but result is not safe anymore.");
                } else if (project.getCallGraph() != null && !project.getCallGraph().containsMethod(method)) {
                    logger.debug("No loop bound annotation for non-WCET method: " + method + ":" + n +
                                 " " + getLineRangeText(code, block) +
                                 ".\nApproximating with " + DEFAULT_LOOP_BOUND);
                } else {
                    logger.error("No loop bound annotation: " + method + ":" + n +
                                 " " + getLineRangeText(code, block) +
                                 ".\nApproximating with " + DEFAULT_LOOP_BOUND + ", but result is not safe anymore.");
                }
                loopAnnot = LoopBound.defaultBound(DEFAULT_LOOP_BOUND);
            }
            block.setLoopBound(loopAnnot);
        }
    }

    private String getLineRangeText(MethodCode code, BasicBlock block) {
        return "[line " + code.getLineString(block.getFirstInstruction()) + "-" +
                          code.getLineString(block.getLastInstruction()) + "]";
    }

    /**
     * Get a loop bound from the DFA for a certain loop and call string and
     * merge it with the annotated value.
     * @return The loop bound to be used for further computations
     */
    public LoopBound dfaLoopBound(BasicBlock headOfLoopBlock, ExecutionContext eCtx, LoopBound annotatedBound) {
    	
        LoopBounds lbAnalysis = project.getDfaLoopBounds();
        if(lbAnalysis == null) return annotatedBound;
        
        MethodInfo methodInfo = headOfLoopBlock.getMethodInfo();
        int dfaUpperBound;
        
        // Insert a try-catch to deal with failures of the DFA analysis
        // FIXME: Bad style
        try {
        	dfaUpperBound = lbAnalysis.getBound(headOfLoopBlock.getLastInstruction(),eCtx.getCallString());
        } catch(NullPointerException ex) {
        	logger.error("Failed to retrieve DFA loop bound values", ex);
        	dfaUpperBound = -1;
        }
        if(dfaUpperBound < 0) {
        	if(! printedLoopBoundInfoMessage.contains(headOfLoopBlock)) {
        		logger.info("No DFA bound for " + methodInfo+"/"+headOfLoopBlock+
        				". Using manual bound: "+annotatedBound);
        		printedLoopBoundInfoMessage.add(headOfLoopBlock);
        	}
        	return annotatedBound;
        } 

        LoopBound loopBound;
        if(annotatedBound == null) {
        	loopBound = LoopBound.boundedAbove(dfaUpperBound);
        	logger.debug("Only DFA bound for "+methodInfo+"headOfLoopBlock");
        } else {
        	loopBound = annotatedBound.clone();
        	// More testing would be nice
        	loopBound.addBound(LoopBoundExpr.numUpperBound(dfaUpperBound), SymbolicMarker.LOOP_ENTRY); 
        	long loopUb = annotatedBound.getSimpleLoopBound().upperBound(eCtx);
        	if(dfaUpperBound < loopUb) {
        		/* This isn't unusual (context dependent loop bounds) */
        		if (logger.isDebugEnabled()) {
        			logger.debug("DFA analysis reports a smaller upper bound :"+dfaUpperBound+ " < "+loopUb+
        					" for "+methodInfo+"/"+headOfLoopBlock);
        		}
        	} else if (dfaUpperBound > loopUb) {
        		/* In principle this is possible, but usually a bad sign */
        		logger.warn("DFA analysis reports a larger upper bound: "+dfaUpperBound+ " > "+loopUb+
        				" for "+methodInfo);
        	} else {
        		if (logger.isDebugEnabled()) {
        			logger.debug("DFA and annotated loop bounds match for "+methodInfo);
        		}
        	}
        }

    	if(! printedLoopBoundInfoMessage.contains(headOfLoopBlock)) {
    		logger.info("DFA bound for " + methodInfo+"/"+headOfLoopBlock+
    				": "+loopBound+". Manual bound info: "+annotatedBound);
    		printedLoopBoundInfoMessage.add(headOfLoopBlock);
    	}
        return loopBound;
    }

}
