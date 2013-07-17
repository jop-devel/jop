/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.common.code;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.processormodel.ProcessorModel;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.UnconditionalBranch;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * A lightweight basic block class, based on linked lists.<br/>
 * <p/>
 * See: A more elaborated attempt to add BasicBlocks to BCEL
 * http://findbugs.sourceforge.net/api/edu/umd/cs/findbugs/ba/BasicBlock.html
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class BasicBlock {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_CFG + ".BasicBlock");

    /**
     * Flow annotations for instructions.
     * Should only be used by {@link ControlFlowGraph}
     */
    static class FlowInfo {
        private boolean alwaysTaken = false;
        private boolean splitBefore = false;
        private boolean splitAfter = false;
        private boolean exit = false;
        private final List<FlowTarget> targets = new ArrayList<FlowTarget>();

        void addTarget(InstructionHandle ih, ControlFlowGraph.EdgeKind kind) {
            targets.add(new FlowTarget(ih, kind));
        }

        public boolean isAlwaysTaken() {
            return alwaysTaken;
        }

        public boolean doSplitBefore() {
            return splitBefore;
        }

        public boolean doSplitAfter() {
            return splitAfter;
        }

        public boolean isExit() {
            return exit;
        }

        public List<FlowTarget> getTargets() {
            return targets;
        }
    }

    /**
     * Represents targets of a control flow instruction
     */
    static class FlowTarget {
        private InstructionHandle target;
        private ControlFlowGraph.EdgeKind edgeKind;

        FlowTarget(InstructionHandle target, ControlFlowGraph.EdgeKind edgeKind) {
            this.target = target;
            this.edgeKind = edgeKind;
        }

        public InstructionHandle getTarget() {
            return target;
        }

        public ControlFlowGraph.EdgeKind getEdgeKind() {
            return edgeKind;
        }

        public void setTarget(InstructionHandle target) {
            this.target = target;
        }

        public void setEdgeKind(ControlFlowGraph.EdgeKind edgeKind) {
            this.edgeKind = edgeKind;
        }

        @Override
        public String toString() {
            return "FlowTarget<" + target.getPosition() + "," + edgeKind + ">";
        }
    }


    /**
     * Keys for the custom {@link InstructionHandle} attributes
     */
    private enum InstrField { FLOW_INFO }

    /**
     * @param ih the instruction handle to check
     * @return the flowInfo associated with an {@link InstructionHandle}
     */
    private static FlowInfo getFlowInfo(InstructionHandle ih) {
        return (FlowInfo) ih.getAttribute(InstrField.FLOW_INFO);
    }

    private final LinkedList<InstructionHandle> instructions = new LinkedList<InstructionHandle>();
    private final MethodCode methodCode;
    private FlowInfo exitFlowInfo;

    /**
     * Create a basic block
     *
     * @param methodCode The method code this basic block belongs to
     */
    public BasicBlock(MethodCode methodCode) {
        this.methodCode = methodCode;
    }

    public AppInfo getAppInfo() {
        return methodCode.getAppInfo();
    }

    public ClassInfo getClassInfo() {
        return methodCode.getClassInfo();
    }

    public MethodInfo getMethodInfo() {
        return methodCode.getMethodInfo();
    }

    /**
     * Get the constant pool associated with the method of this basic block
     *
     * @return the ConstantPoolGen of the ClassInfo
     */
    public ConstantPoolGen cpg() {
        return this.getMethodInfo().getClassInfo().getConstantPoolGen();
    }

    public FlowInfo getExitFlowInfo() {
        return exitFlowInfo;
    }

    private void setExitFlowInfo(FlowInfo exitFlowInfo) {
        // used only by the builder?
        this.exitFlowInfo = exitFlowInfo;
    }

    public void setLoopBound(LoopBound loopBound) {
        methodCode.setLoopBound(getLastInstruction(), loopBound);
    }

    public LoopBound getLoopBound() {
        // TODO we might need to handle block copy/split/.. to keep this value attached to the correct handle
        // we can only store and retrieve loopbounds here, but not call the DFA tool.
        // DFA should set its loopbounds after it finished its analysis.
        // Currently, the DFA loopbounds are set to the CFG by the WCETEventHandler on creation of the CFG
        return methodCode.getLoopBound(getLastInstruction());
    }

    /**
     * add an instruction to this basic block
     * @param ih the instruction to add
     */
    public void addInstruction(InstructionHandle ih) {
        this.instructions.add(ih);
    }

    /**
     * Get the first instruction of an basic block
     * (potential target of control flow instruction)
     *
     * @return the first instruction in this block.
     */
    public InstructionHandle getFirstInstruction() {
        return this.instructions.getFirst();
    }

    /**
     * Get the last instruction of an basic block
     * (potential control flow instruction)
     *
     * @return the last instruction of this block
     */
    public InstructionHandle getLastInstruction() {
        return this.instructions.getLast();
    }

    /**
     * Get the invoke instruction of the basic block (which must be
     * the only instruction in the basic block)
     *
     * @return the invoke instruction, or <code>null</code>, if the basic block doesn't
     *         contain an invoke instruction or if it is a special invoke.
     * @throws ControlFlowGraph.ControlFlowError
     *          if there is more than one invoke instruction in the block.
     * @see ProcessorModel#isSpecialInvoke(MethodInfo, Instruction)
     */
    public InstructionHandle getTheInvokeInstruction() {
        InstructionHandle theInvInstr = null;
        for (InstructionHandle ih : this.instructions) {
            if (!(ih.getInstruction() instanceof InvokeInstruction)) continue;
            InvokeInstruction inv = (InvokeInstruction) ih.getInstruction();
            if (this.getAppInfo().getProcessorModel().isSpecialInvoke(methodCode.getMethodInfo(), inv)) {
                continue;
            }
            if (theInvInstr != null) {
                throw new ControlFlowGraph.ControlFlowError("More than one invoke instruction in a basic block");
            }
            theInvInstr = ih;
        }
        return theInvInstr;
    }

    /**
     * @return the BranchInstruction of the basic block, or {@code null} if there is none.
     */
    public BranchInstruction getBranchInstruction() {
        Instruction last = this.getLastInstruction().getInstruction();
        return ((last instanceof BranchInstruction) ? ((BranchInstruction) last) : null);
    }

    /**
     * @return the list of {@link InstructionHandle}s, which make up this basic block
     */
    public List<InstructionHandle> getInstructions() {
        return this.instructions;
    }

    /**
     * @return number of bytes in this basic block
     */
    public int getNumberOfBytes() {
        int len = 0;
        for (InstructionHandle ih : this.instructions) {
            len += getAppInfo().getProcessorModel().getNumberOfBytes(
                    methodCode.getMethodInfo(), ih.getInstruction()
            );
        }
        return len;
    }

    /**
     * @return all source code lines this basic block maps to
     */
    public Map<ClassInfo,TreeSet<Integer>> getSourceLines() {
        Map<ClassInfo,TreeSet<Integer>> map = new HashMap<ClassInfo, TreeSet<Integer>>(2);

        for (InstructionHandle ih : instructions) {
            ClassInfo cls = methodCode.getSourceClassInfo(ih);
            TreeSet<Integer> lines = map.get(cls);
            if (lines == null) {
                lines = new TreeSet<Integer>();
                map.put(cls,lines);
            }

            int line = methodCode.getLineNumber(ih);
            if (line >= 0) lines.add(line);
        }

        return map;
    }
    
    /**
     * @return a human readable string representation of the location of the first instruction
     * in the basic block
     */
    public String getStartLine() {
    	ClassInfo cls = null;
    	int line = -1;
    	for(InstructionHandle ih : instructions) {
    		cls = methodCode.getSourceClassInfo(ih);
    		line = methodCode.getLineNumber(this.getFirstInstruction());
    		if(line >= 0) break;
    	}
    	if(line >= 0) {
    		 return cls.getSourceFileName()+":"+line;
    	} else {
    		 return getMethodInfo().getClassInfo().getSourceFileName()+":"+getMethodInfo().getFQMethodName();    		
    	}
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicBlock that = (BasicBlock) o;

        if (!instructions.equals(that.getInstructions())) return false;
        if (!getMethodInfo().equals(that.getMethodInfo())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = instructions.hashCode();
        result = 31 * result + methodCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BasicBlock: " +
                instructions;
    }

    /*---------------------------------------------------------------------------
     *  Control flow graph construction, compilation
     *---------------------------------------------------------------------------
     */

    /**
     * Override this class to get specific basic block partitioning
     */
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    public static class InstructionTargetVisitor extends EmptyVisitor {
        private FlowInfo flowInfo;
        private HashSet<InstructionHandle> targeted;
        private MethodCode methodCode;

        protected InstructionTargetVisitor(MethodCode m) {
            this.targeted = new HashSet<InstructionHandle>();
            this.methodCode = m;
        }

        public boolean isTarget(InstructionHandle ih) {
            return targeted.contains(ih);
        }

        public void visitInstruction(InstructionHandle ih) {
            ih.accept(this);
            if (methodCode.getAppInfo().getProcessorModel().isImplementedInJava(methodCode.getMethodInfo(), ih.getInstruction())) {
                flowInfo.splitBefore = true;
                flowInfo.splitAfter = true;
            }
        }

        @Override
        public void visitBranchInstruction(BranchInstruction obj) {
            flowInfo.splitAfter = true; /* details follow in goto/if/jsr/select */
        }

        @Override
        public void visitGotoInstruction(GotoInstruction obj) {
            flowInfo.addTarget(obj.getTarget(), ControlFlowGraph.EdgeKind.GOTO_EDGE);
            this.targeted.add(obj.getTarget());
            flowInfo.alwaysTaken = true;
        }

        @Override
        public void visitIfInstruction(IfInstruction obj) {
            flowInfo.addTarget(obj.getTarget(), ControlFlowGraph.EdgeKind.BRANCH_EDGE);
            this.targeted.add(obj.getTarget());
        }

        @Override
        public void visitSelect(Select obj) {
            super.visitSelect(obj);
            flowInfo.addTarget(obj.getTarget(), ControlFlowGraph.EdgeKind.SELECT_EDGE);
            this.targeted.add(obj.getTarget());
            // Note that getTargets() does not include the default target
            for (InstructionHandle tih : obj.getTargets()) {
                flowInfo.addTarget(tih, ControlFlowGraph.EdgeKind.SELECT_EDGE);
                this.targeted.add(tih);
            }
        }

        @Override
        public void visitJsrInstruction(JsrInstruction obj) {
            flowInfo.addTarget(obj.getTarget(), ControlFlowGraph.EdgeKind.JSR_EDGE);
            this.targeted.add(obj.getTarget());
            flowInfo.alwaysTaken = true;
        }

        /**
         * FIXME: Exceptions aren't supported yet, but to avoid
         * early bail out, we assume exceptions terminate execution (for now)
         */
        @Override
        public void visitATHROW(ATHROW obj) {
            flowInfo.exit = true;
            flowInfo.splitAfter = true;
        }

        // Not 100% necessary, but simplifies program analysis a lot
        @Override
        public void visitInvokeInstruction(InvokeInstruction obj) {
            if (!methodCode.getAppInfo().getProcessorModel().isSpecialInvoke(methodCode.getMethodInfo(), obj)) {
                flowInfo.splitBefore = true;
                flowInfo.splitAfter = true;
            }
        }
        
        // Not necessary, but nice for synchronized block analysis
        @Override
        public void visitMONITORENTER(MONITORENTER obj) {
            flowInfo.splitBefore = true;
        }
        
        @Override
        public void visitReturnInstruction(ReturnInstruction obj) {
            flowInfo.splitAfter = true;
            flowInfo.exit = true;
        }

        public FlowInfo getFlowInfo(InstructionHandle ih) {
            flowInfo = new FlowInfo();
            visitInstruction(ih);
            return flowInfo;
        }

    }

    /**
     * Create a vector of basic blocks, annotated with flow information
     *
     * @param methodCode The MethodCode of the method from which basic blocks should be extracted.
     * @return A vector of BasicBlocks, which instruction handles annotated with
     *         flow information.
     */
    static List<BasicBlock> buildBasicBlocks(MethodCode methodCode) {
        InstructionTargetVisitor itv = new InstructionTargetVisitor(methodCode);
        List<BasicBlock> basicBlocks = new LinkedList<BasicBlock>();
        // We do want to have the latest code, so we compile any existing, *attached* CFG first.
        // However, we do NOT want to remove this CFG, and we do *NOT* want to trigger the onBeforeCodeModify event,
        // else we might remove all CFGs for this method, which we want to avoid if we only modify the graph but do not
        // compile it (the event will be triggered when CFG#compile() is called).
        InstructionList il = methodCode.getInstructionList(true, false);
        il.setPositions(true);

        /* Step 1: compute flow info */
        for (InstructionHandle ih : il.getInstructionHandles()) {
            ih.addAttribute(InstrField.FLOW_INFO, itv.getFlowInfo(ih));
        }

        /* Step 2: create basic blocks */
        {
            BasicBlock bb = new BasicBlock(methodCode);
            InstructionHandle[] handles = il.getInstructionHandles();

            for (int i = 0; i < handles.length; i++) {
                InstructionHandle ih = handles[i];
                bb.addInstruction(ih);
                boolean doSplit = getFlowInfo(ih).doSplitAfter();
                if (i + 1 < handles.length) {
                    doSplit |= itv.isTarget(handles[i + 1]);
                    doSplit |= getFlowInfo(handles[i + 1]).doSplitBefore();
                }
                if (doSplit) {
                    bb.setExitFlowInfo(getFlowInfo(ih));
                    basicBlocks.add(bb);
                    bb = new BasicBlock(methodCode);
                }
                ih.removeAttribute(InstrField.FLOW_INFO);
            }

            if (!bb.getInstructions().isEmpty()) {
                // be nice to DFA stuff, and ignore NOPs
                for (int i = bb.getInstructions().size() - 1; i >= 0; --i) {
                    InstructionHandle x = bb.getInstructions().get(i);
                    if (x.getInstruction().getOpcode() != Constants.NOP) {
                        throw new AssertionError("[INTERNAL ERROR] Last instruction " + x +
                                " in code does not change control flow - this is impossible");
                    }
                }
            }
        }
        return basicBlocks;
    }

    /**
     * Append the instructions of this block to an instruction list.
     *
     * @see MethodCode#copyCustomValues(MethodInfo, InstructionHandle, InstructionHandle)
     * @param sourceInfo the method info containing the source instructions, used to copy custom values.
     * @param il the instruction list to append to.
     * @param attributes a list of attribute keys to copy in addition to those managed by MethodCode and BasicBlock.
     */
    public void appendTo(MethodInfo sourceInfo, InstructionList il, Object[] attributes) {
        List<InstructionHandle> old = new ArrayList<InstructionHandle>(instructions);
        instructions.clear();
        for (InstructionHandle ih : old) {
            InstructionHandle newIh;
            if (ih.getInstruction() instanceof BranchInstruction) {
                newIh = il.append((BranchInstruction)ih.getInstruction());
            } else {
                newIh = il.append(ih.getInstruction());
            }
            // link to new handles, find first and last handle
            instructions.add(newIh);
            // we need to copy all attributes. FlowInfo should not be needed.
            methodCode.copyCustomValues(sourceInfo, newIh, ih);
            for (Object key : attributes) {
                Object value = ih.getAttribute(key);
                if (value != null) newIh.addAttribute(key, value);
            }
            methodCode.retarget(ih, newIh);
        }
    }

    /*---------------------------------------------------------------------------
     *  Dump BasicBlock
     *---------------------------------------------------------------------------
     */

    /**
     * <p>Compact, human-readable String representation of the basic block.</p>
     * <p/>
     * <p>Mixed Stack notation, with at most one side-effect statement per line.</p>
     * Example:<br/>
     * {@code local_0 <- sipush[3] sipush[4] dup add add} <br/>
     * {@code local_1 <- load[local_0] load[local_0] mul}
     *
     * @return a compact string representation of this block
     */
    public String dump() {
        StringBuilder sb = new StringBuilder();
        Iterator<InstructionHandle> ihIter = this.instructions.iterator();
        InstructionPrettyPrinter ipp = new InstructionPrettyPrinter();
        while (ihIter.hasNext()) {
            InstructionHandle ih = ihIter.next();
            ipp.visitInstruction(ih);
        }
        sb.append(ipp.getBuffer());
        return sb.toString();
    }

    /* Prototyping */
    /* TODO: Refactor into some pretty printing class */

    private class InstructionPrettyPrinter extends EmptyVisitor {
        private StringBuilder sb;
        private StringBuilder lineBuffer;
        private boolean visited;
        private int startPos = -1, lastPos = -1;
        private int currentPos;
        private Integer address;


        public InstructionPrettyPrinter() {
            this.sb = new StringBuilder();
            this.lineBuffer = new StringBuilder();
        }

        public StringBuilder getBuffer() {
            nextLine();
            return sb;
        }

        public void visitInstruction(InstructionHandle ih) {
            this.visited = false;
            //this.address = cfg.getConstAddress(ih);
            currentPos = methodCode.getLineNumber(ih);
            ih.accept(this);
            if (!visited) {
                String s = ih.getInstruction().toString(cpg().getConstantPool());
                append(s);
            }
        }

        private void nextLine() {
            if (lineBuffer.length() > 0) {
                String start = startPos < 0 ? "?" : ("" + startPos);
                String end = lastPos < 0 ? "?" : ("" + lastPos);
                if (startPos != lastPos) lineBuffer.insert(0, "[" + start + "-" + end + "] ");
                else lineBuffer.insert(0, "[" + start + "]  ");
                sb.append(lineBuffer);
                sb.append("\n");
                lineBuffer = new StringBuilder();
                startPos = currentPos;
                lastPos = currentPos;
            }
        }

        @SuppressWarnings({"AssignmentToMethodParameter"})
        private void append(String stackOp) {
            if (lineBuffer.length() > 0) {
                if (lineBuffer.length() < 45) lineBuffer.append(" ");
                else lineBuffer.append("\n  \\ ");
            }
            if (address != null) stackOp = String.format("%s<%d>", stackOp, address);
            lineBuffer.append(stackOp);
            markVisited();
        }

        @SuppressWarnings({"AssignmentToMethodParameter"})
        private void assign(String lhs) {
            if (address != null) lhs = String.format("%s<%d>", lhs, address);
            lineBuffer.insert(0, lhs + "<-");
            nextLine();
            markVisited();
            visited = true;
        }

        private void markVisited() {
            visited = true;
            if (startPos < 0) startPos = currentPos;
            lastPos = currentPos;
        }

        @Override
        public void visitBranchInstruction(BranchInstruction obj) {
            nextLine();
        }

        @Override
        public void visitUnconditionalBranch(UnconditionalBranch obj) {
            nextLine();
        }

        @Override
        public void visitStoreInstruction(StoreInstruction obj) {
            assign("$" + obj.getIndex());
        }

        @Override
        public void visitPUTFIELD(PUTFIELD obj) {
            assign(obj.getFieldName(cpg()));
        }

        @Override
        public void visitPUTSTATIC(PUTSTATIC obj) {
            String fieldName = obj.getFieldName(cpg());
            assign(fieldName);
        }

        @Override
        public void visitInvokeInstruction(InvokeInstruction obj) {
            append(obj.toString());
        }

    }

}
