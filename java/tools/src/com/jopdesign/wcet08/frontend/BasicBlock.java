/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.jopdesign.wcet08.frontend;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.bcel.classfile.Code;
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
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet08.frontend.FlowGraph.EdgeKind;

/**
 * A lightweight basic block class, based on linked lists.<br/>
 * 
 * @see A more elaborated attempt to add BasicBlocks to BCEL
 *      http://findbugs.sourceforge.net/api/edu/umd/cs/findbugs/ba/BasicBlock.html
 * 
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class BasicBlock  {
	private static final long serialVersionUID = 1L;
	public enum InstrField { FLOW_INFO, LINE_NUMBER };
	private LinkedList<InstructionHandle> instructions = new LinkedList<InstructionHandle>();
	private MethodInfo methodInfo;
	private ConstantPoolGen cpg;
	
	public BasicBlock(MethodInfo m) {
		this.methodInfo = m;
		this.cpg = new ConstantPoolGen(m.getCli().clazz.getConstantPool());
	}
	public JOPAppInfo getAppInfo() {
		return (JOPAppInfo) methodInfo.getCli().appInfo;
	}
	public ClassInfo getClassInfo() {
		return methodInfo.getCli();
	}
	public MethodInfo getMethodInfo() {
		return methodInfo;
	}

	public ConstantPoolGen cpg() {
		return this.cpg;
	}
	public void addInstruction(InstructionHandle ih) {
		this.instructions.add(ih);
	}
	public InstructionHandle getFirstInstruction() {
		return this.instructions.getFirst();
	}	
	public InstructionHandle getLastInstruction() {
		return this.instructions.getLast();
	}

	/**
	 * Get the invoke instruction of the basic block (which should be
	 * the only instruction in the basic block)
	 * @return the invoke instruction, or <code>null</code>, if the basic block doesn't
	 *         contain an invoke instruction.
	 * @throws AssertionError if there is more than one invoke instruction in the block.
	 */
	public InvokeInstruction getTheInvokeInstruction() {
		InvokeInstruction theInvInstr = null;
		for(InstructionHandle ih : this.instructions) {
			if(! (ih.getInstruction() instanceof InvokeInstruction)) continue;
			InvokeInstruction inv = (InvokeInstruction) ih.getInstruction();
			if(this.getAppInfo().isSpecialInvoke(this.methodInfo, inv)) continue;
			if(theInvInstr != null) {
				throw new AssertionError("More than one invoke instruction in a basic block");
			}
			theInvInstr = inv;
		}
		return theInvInstr;
	}
	public BranchInstruction getBranchInstruction() {
		Instruction last = this.getLastInstruction().getInstruction();
		return ((last instanceof BranchInstruction) ? ((BranchInstruction) last) : null);
	}
	public List<InstructionHandle> getInstructions() {
		return this.instructions;
	}
	/**
	 * Create a vector of basic blocks, annotated with flow information
	 * @param m The MethodInfo of the method where should extract basic blocks.
	 * @return A vector of BasicBlocks, which instruction handles annotated with
	 * flow information.
	 */
	public static Vector<BasicBlock> buildBasicBlocks(MethodInfo m) {
		InstructionTargetVisitor itv = new InstructionTargetVisitor(m);
		Vector<BasicBlock> basicBlocks = new Vector<BasicBlock>();
		Code code = m.getMethod().getCode();
		InstructionList il = new InstructionList(code.getCode());		
		il.setPositions(true);
		/* Step 1: compute flow info */
		for(InstructionHandle ih : il.getInstructionHandles()) {
			ih.addAttribute(InstrField.FLOW_INFO, itv.getFlowInfo(ih));
			ih.addAttribute(InstrField.LINE_NUMBER, code.getLineNumberTable().getSourceLine(ih.getPosition()));
		}
		/* Step 2: create basic blocks */
		{
			BasicBlock bb = new BasicBlock(m);
			InstructionHandle[] handles = il.getInstructionHandles();
			for(int i = 0; i < handles.length; i++) {
				InstructionHandle ih = handles[i];
				bb.addInstruction(ih);
				boolean doSplit = ((FlowInfo)ih.getAttribute(InstrField.FLOW_INFO)).splitAfter;
				if(i+1 < handles.length) {
					doSplit |= ((FlowInfo)handles[i+1].getAttribute(InstrField.FLOW_INFO)).splitBefore;
				}
				if(doSplit) {
					basicBlocks.add(bb);
					bb = new BasicBlock(m);
				}
			}
		}
		return basicBlocks;
	}
	
	/**
	 * Flow annotations for basic blocks (package visibility for FlowGraph)
	 */
	static class FlowInfo {
		boolean alwaysTaken = false;
		boolean splitBefore = false;
		boolean splitAfter = false;
		boolean exit = false;
		List<FlowTarget> targets = new Vector<FlowTarget>();
		void addTarget(InstructionHandle ih, EdgeKind kind) {
			targets.add(new FlowTarget(ih,kind));
		}
	}
	static class FlowTarget {
		InstructionHandle target;
		EdgeKind edgeKind;
		FlowTarget(InstructionHandle target, EdgeKind edgeKind) { 
			this.target = target; this.edgeKind = edgeKind; 
		}
	}
	
	/**
	 * Override this class to get specific basic block partitioning
	 */
	public static class InstructionTargetVisitor extends EmptyVisitor {
		private FlowInfo flowInfo;
		private MethodInfo methodInfo;
		private JOPAppInfo appInfo;
		public void visitInstruction(InstructionHandle ih) {
			if(ih.getTargeters() != null) {
				flowInfo.splitBefore = true;
			}
			ih.accept(this);			
		}
		@Override public void visitBranchInstruction(BranchInstruction obj) {
			flowInfo.splitAfter=true;
		}
		@Override public void visitGotoInstruction(GotoInstruction obj) {
			flowInfo.addTarget(obj.getTarget(),EdgeKind.GOTO_EDGE);
			flowInfo.alwaysTaken = true;
		}
		@Override public void visitIfInstruction(IfInstruction obj) {
			flowInfo.addTarget(obj.getTarget(),EdgeKind.BRANCH_EDGE);
		}
		// Not neccesarily, but nice for WCET analysis
		@Override public void visitInvokeInstruction(InvokeInstruction obj) {
			if(! appInfo.isSpecialInvoke(methodInfo, obj)) {
				flowInfo.splitBefore = true;
				flowInfo.splitAfter = true;
			}
		}
		@Override public void visitJsrInstruction(JsrInstruction obj) {
			flowInfo.addTarget(obj.getTarget(),EdgeKind.JSR_EDGE);
			flowInfo.alwaysTaken = true;
		}
		@Override public void visitReturnInstruction(ReturnInstruction obj) {
			flowInfo.splitAfter = true;
			flowInfo.exit = true;
		}
		@Override public void visitSelect(Select obj) {
			super.visitSelect(obj);
			for(InstructionHandle tih : obj.getTargets()) {
				flowInfo.addTarget(tih,EdgeKind.SELECT_EDGE);
			}
		}
		protected InstructionTargetVisitor(MethodInfo m) { 
			this.methodInfo = m;
			this.appInfo = (JOPAppInfo) m.getCli().appInfo;
		}
		
		public FlowInfo getFlowInfo(InstructionHandle ih) {
			flowInfo = new FlowInfo();
			visitInstruction(ih);
			return flowInfo;
		}
	}
	public int getNumberOfBytes() {
		int len = 0;
		for(InstructionHandle ih : this.instructions) {
			JOPAppInfo appInfo = (JOPAppInfo) this.methodInfo.getCli().appInfo;
			int opCode = appInfo.getJOpCode(this.methodInfo.getCli(), ih.getInstruction());
			if(opCode >= 0) len += JopInstr.len(opCode);
		}
		return len;
	}
	public String dump() {
		StringBuilder sb = new StringBuilder();
		for(InstructionHandle ih : this.instructions) {
			sb.append(ih.getPosition()+": "+ih.getInstruction()+";\n");
		}
		return sb.toString();
	}
}
