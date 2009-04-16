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
package com.jopdesign.wcet.frontend;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.ATHROW;
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
import com.jopdesign.wcet.frontend.ControlFlowGraph.ControlFlowError;
import com.jopdesign.wcet.frontend.ControlFlowGraph.EdgeKind;

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
	/**
	 * Flow annotations for instructions.
	 * Should only be used by {@link FlowGraph}
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
	/**
	 * Represents targets of a control flow instruction
	 */
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
	

	/** Keys for the custom {@link InstructionHandle} attributes */
	private enum InstrField { FLOW_INFO, LINE_NUMBER };
	
	/** Get FlowInfo associated with an {@link InstructionHandle} */
	static FlowInfo getFlowInfo(InstructionHandle ih) {
		return (FlowInfo)ih.getAttribute(InstrField.FLOW_INFO);
	}
	
	/** Get Line number associated with an {@link InstructionHandle} */
	static Integer getLineNumber(InstructionHandle ih) {
		return (Integer)ih.getAttribute(InstrField.LINE_NUMBER);
	}
	
	private LinkedList<InstructionHandle> instructions = new LinkedList<InstructionHandle>();
	private MethodInfo methodInfo;
	private WcetAppInfo appInfo;
	
	/** Create a basic block
	 * @param appInfo    The WCET context
	 * @param methodInfo The method the basic block belongs to
	 */
	public BasicBlock(WcetAppInfo appInfo, MethodInfo methodInfo) {
		this.appInfo = appInfo;
		this.methodInfo = methodInfo;
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
	/** Get the constant pool associated with the method of this basic block */
	public ConstantPoolGen cpg() {
		return this.getMethodInfo().getMethodGen().getConstantPool();
	}
	/** add an instruction to this basic block */
	public void addInstruction(InstructionHandle ih) {
		this.instructions.add(ih);
	}
	/** Get the first instruction of an basic block
	 *  (potential target of control flow instruction)
	 */
	public InstructionHandle getFirstInstruction() {
		return this.instructions.getFirst();
	}
	/** Get the last instruction of an basic block
	 *  (potential control flow instruction)
	 * @return
	 */
	public InstructionHandle getLastInstruction() {
		return this.instructions.getLast();
	}

	/**
	 * Get the invoke instruction of the basic block (which must be
	 * the only instruction in the basic block)
	 * @return the invoke instruction, or <code>null</code>, if the basic block doesn't
	 *         contain an invoke instruction.
	 * @throws FlowGraphError if there is more than one invoke instruction in the block.
	 */
	public InvokeInstruction getTheInvokeInstruction() {
		InvokeInstruction theInvInstr = null;
		for(InstructionHandle ih : this.instructions) {
			if(! (ih.getInstruction() instanceof InvokeInstruction)) continue;
			InvokeInstruction inv = (InvokeInstruction) ih.getInstruction();
			if(this.getAppInfo().getProcessorModel().isSpecialInvoke(this.methodInfo, inv)) {
				continue;
			}
			if(theInvInstr != null) {
				throw new ControlFlowError("More than one invoke instruction in a basic block");
			}
			theInvInstr = inv;
		}
		return theInvInstr;
	}
	/** return the BranchInstruction of the basic block, or {@code null} if there is none. */
	public BranchInstruction getBranchInstruction() {
		Instruction last = this.getLastInstruction().getInstruction();
		return ((last instanceof BranchInstruction) ? ((BranchInstruction) last) : null);
	}
	/** Get the list of {@link InstructionHandle}s, which make up this basic block */
	public List<InstructionHandle> getInstructions() {
		return this.instructions;
	}

	/** Get number of bytes in this basic block */
	public int getNumberOfBytes() {
		int len = 0;
		for(InstructionHandle ih : this.instructions) {
			len += appInfo.getProcessorModel().getNumberOfBytes(
					this.methodInfo, ih.getInstruction()					
			);
		}
		return len;
	}
	/*---------------------------------------------------------------------------
	 *  Control flow graph construction 
	 *---------------------------------------------------------------------------
	 */
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
			if(appInfo.getProcessorModel().isImplementedInJava(ih.getInstruction())) {
				flowInfo.splitBefore = true;
				flowInfo.splitAfter  = true;
			}
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

		/** FIXME: Exceptions aren't supported yet, but to avoid 
		 *  early bail out, we assume exceptions terminate execution (for now) */
		@Override
		public void visitATHROW(ATHROW obj) {
			flowInfo.exit = true;
			flowInfo.splitAfter = true;
		}
		
		// Not neccesarily, but nice for WCET analysis
		@Override public void visitInvokeInstruction(InvokeInstruction obj) {
			if(! appInfo.getProcessorModel().isSpecialInvoke(methodInfo, obj)) {
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

	/**
	 * Create a vector of basic blocks, annotated with flow information
	 * @param methodInfo The MethodInfo of the method where should extract basic blocks.
	 * @return A vector of BasicBlocks, which instruction handles annotated with
	 * flow information.
	 */
	static Vector<BasicBlock> buildBasicBlocks(WcetAppInfo ai, MethodInfo methodInfo) {
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
			if(! bb.instructions.isEmpty()) {
				// be nice to DFA stuff, and ignore NOPs
				for(int i = bb.instructions.size() - 1; i>=0; --i) {
					InstructionHandle x = bb.instructions.get(i);
					if(x.getInstruction().getOpcode() != org.apache.bcel.Constants.NOP) {
						throw new AssertionError("[INTERNAL ERROR] Last instruction "+x+
		                 " in code does not change control flow - this is impossible");
					}
				}
			}
		}		
		return basicBlocks;
	}
	
	
	/** <p>Compact, human-readable String representation of the basic block.</p>
	 *
	 *  <p>Mixed Stack notation, with at most one side-effect statement per line.</p> 
	 *  Example:<br/>
	 *  {@code local_0 <- sipush[3] sipush[4] dup add add} <br/>
	 *  {@code local_1 <- load[local_0] load[local_0] mul}
	 *  */
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
