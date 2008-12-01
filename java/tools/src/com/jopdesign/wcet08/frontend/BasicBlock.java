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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.StoreInstruction;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.EdgeKind;

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
	private enum InstrField { FLOW_INFO, LINE_NUMBER };
	
	public static FlowInfo getFlowInfo(InstructionHandle ih) {
		return (FlowInfo)ih.getAttribute(InstrField.FLOW_INFO);
	}
	public static Integer getLineNumber(InstructionHandle ih) {
		return (Integer)ih.getAttribute(InstrField.LINE_NUMBER);
	}

	private LinkedList<InstructionHandle> instructions = new LinkedList<InstructionHandle>();
	private MethodInfo methodInfo;
	private WcetAppInfo appInfo;
	
	public BasicBlock(WcetAppInfo wcetAi, MethodInfo m) {
		this.appInfo = wcetAi;
		this.methodInfo = m;
	}
	public WcetAppInfo getAppInfo() {
		return appInfo;
	}
	public ClassInfo getClassInfo() {
		return methodInfo.getCli();
	}
	public MethodInfo getMethodInfo() {
		return methodInfo;
	}

	public ConstantPoolGen cpg() {
		return this.methodInfo.getMethodGen().getConstantPool();
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
	 * @param methodInfo The MethodInfo of the method where should extract basic blocks.
	 * @return A vector of BasicBlocks, which instruction handles annotated with
	 * flow information.
	 */
	public static Vector<BasicBlock> buildBasicBlocks(WcetAppInfo ai, MethodInfo methodInfo) {
		InstructionTargetVisitor itv = new InstructionTargetVisitor(ai,methodInfo);
		Vector<BasicBlock> basicBlocks = new Vector<BasicBlock>();
		InstructionList il = methodInfo.getMethodGen().getInstructionList();
		il.setPositions(true);
		LineNumberTable lineNumberTable = 
			methodInfo.getMethodGen().getLineNumberTable(methodInfo.getConstantPoolGen());
		/* Step 1: compute flow info */
		for(InstructionHandle ih : il.getInstructionHandles()) {
			ih.addAttribute(InstrField.FLOW_INFO, itv.getFlowInfo(ih));
			ih.addAttribute(InstrField.LINE_NUMBER, lineNumberTable.getSourceLine(ih.getPosition()));
		}
		/* Step 2: create basic blocks */
		{
			BasicBlock bb = new BasicBlock(ai, methodInfo);
			InstructionHandle[] handles = il.getInstructionHandles();
			for(int i = 0; i < handles.length; i++) {
				InstructionHandle ih = handles[i];
				bb.addInstruction(ih);
				boolean doSplit = getFlowInfo(ih).splitAfter;
				if(i+1 < handles.length) {
					doSplit |= itv.isTarget(handles[i+1]);
					doSplit |= getFlowInfo(handles[i+1]).splitBefore;
				}
				if(doSplit) {
					basicBlocks.add(bb);
					bb = new BasicBlock(ai,methodInfo);
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
		@Override public String toString() {
			return "FlowTarget<"+target.getPosition()+","+edgeKind+">";
		}
	}
	
	/**
	 * Override this class to get specific basic block partitioning
	 */
	public static class InstructionTargetVisitor extends EmptyVisitor {
		private FlowInfo flowInfo;
		private HashSet<InstructionHandle> targeted;
		public boolean isTarget(InstructionHandle ih) {
			return targeted.contains(ih);
		}
		private MethodInfo methodInfo;
		private WcetAppInfo appInfo;
		public void visitInstruction(InstructionHandle ih) {
			ih.accept(this);
		}
		@Override public void visitBranchInstruction(BranchInstruction obj) {
			flowInfo.splitAfter=true; /* details follow in goto/if/jsr/select */
		}
		@Override public void visitGotoInstruction(GotoInstruction obj) {
			flowInfo.addTarget(obj.getTarget(),EdgeKind.GOTO_EDGE);
			this.targeted.add(obj.getTarget());
			flowInfo.alwaysTaken = true;
		}
		@Override public void visitIfInstruction(IfInstruction obj) {
			flowInfo.addTarget(obj.getTarget(),EdgeKind.BRANCH_EDGE);
			this.targeted.add(obj.getTarget());
		}
		@Override public void visitSelect(Select obj) {
			super.visitSelect(obj);
			for(InstructionHandle tih : obj.getTargets()) {
				flowInfo.addTarget(tih,EdgeKind.SELECT_EDGE);
				this.targeted.add(tih);
			}
		}
		@Override public void visitJsrInstruction(JsrInstruction obj) {
			flowInfo.addTarget(obj.getTarget(),EdgeKind.JSR_EDGE);
			this.targeted.add(obj.getTarget());
			flowInfo.alwaysTaken = true;
		}

		// Not neccesarily, but nice for WCET analysis
		@Override public void visitInvokeInstruction(InvokeInstruction obj) {
			if(! appInfo.isSpecialInvoke(methodInfo, obj)) {
				flowInfo.splitBefore = true;
				flowInfo.splitAfter = true;
			}
		}
		@Override public void visitReturnInstruction(ReturnInstruction obj) {
			flowInfo.splitAfter = true;
			flowInfo.exit = true;
		}
		protected InstructionTargetVisitor(WcetAppInfo ai, MethodInfo m) {
			this.targeted = new HashSet<InstructionHandle>();
			this.methodInfo = m;
			this.appInfo = ai;
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
			int opCode = appInfo.getJOpCode(this.methodInfo.getCli(), ih.getInstruction());
			if(opCode >= 0) len += JopInstr.len(opCode);
		}
		return len;
	}
	/** fancy dumping */
	public String dump() {
		StringBuilder sb = new StringBuilder();
		LineNumberTable lnt = methodInfo.getMethod().getLineNumberTable();
		ConstantPoolGen cpg = methodInfo.getConstantPoolGen();
		Iterator<InstructionHandle> ihIter = this.instructions.iterator();
		StringBuilder lineBuilder = new StringBuilder();
		InstructionHandle first = null;
		while(ihIter.hasNext()) {
			InstructionHandle ih = ihIter.next();
			String line = null;
			if(first == null) {
				first = ih;
			} else {
				lineBuilder.append(" ");
			}
			Instruction ii = ih.getInstruction();
			// SIPUSH, RET, NOP, NEWARRAY, MONITORENTER, MONITOREXIT
			if(ii instanceof ReturnInstruction) {
				line = ii.getName() + ": " + lineBuilder.toString();
			} else if(ii instanceof StoreInstruction) {
				line = "$l"+((StoreInstruction)ii).getIndex()+" <- "+lineBuilder.toString();
			} else if(ii instanceof FieldInstruction && ii.getName().startsWith("put") ) {
				line = "$"+((FieldInstruction)ii).getFieldName(cpg)+" <- "+lineBuilder.toString();
		    } else {
				lineBuilder.append(ii.getName());
			}
			if(! ihIter.hasNext()) {
				line = lineBuilder.toString();
			}
			if(line != null) {
				int l1 = lnt.getSourceLine(first.getPosition());
				int l2 = lnt.getSourceLine(ih.getPosition());
				if(l1 != l2) sb.append("["+l1+"-"+l2+"] ");
				else         sb.append("["+l1+"]  ");
				sb.append(line+"\n");
				first = null;
				lineBuilder = new StringBuilder();
			}
		}
		return sb.toString();
	}
}
