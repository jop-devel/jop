package com.jopdesign.timing.jop;

import java.util.Vector;

import com.jopdesign.timing.jop.MicrocodeAnalysis.MicrocodeVerificationException;
import com.jopdesign.tools.Instruction;
import com.jopdesign.tools.Jopa.Line;

/** Class representing microcode paths */
public class MicrocodePath {
	/** Path Entry, 'constant', i.e. you never modify any fields after construction */
	public class PathEntry {
		private PathEntry(Line l) { this(l,null); }
		private PathEntry(Line l, Integer tos) {
			this.line = l;
			this.tos  = tos;
		}
		private final Line line;
		/** TOS (op of stack), if available */
		private final Integer tos;    
		public Instruction getInstruction() {
			return line.getInstruction();
		}
		public String toString() {
			Instruction i = getInstruction();
			String stringRepr = i.name;
			if(i.hasOpd) { 
				if(line.getSymVal() != null) stringRepr += " "+line.getSymVal();
				else                         stringRepr += " "+line.getIntVal();
			}
			if(tos != null) { stringRepr += "{tos="+tos+"}"; }
			return stringRepr;
		}
		public Integer getTOS() {
			return tos;
		}
	}
	private String opName;
	private Vector<PathEntry> path;
	private boolean nullPtrCheck = false, 
	                arrayBoundCheck = false,
	                hasWait = false;
	private int minMultiplierDelay = Integer.MAX_VALUE;

	public MicrocodePath(String name) {
		this.opName = name;
		this.path = new Vector<PathEntry>();
	}
	
	public MicrocodePath clone() {
		MicrocodePath cloned = new MicrocodePath(opName);
		cloned.path = new Vector<PathEntry>(path);
		cloned.nullPtrCheck = nullPtrCheck;
		cloned.arrayBoundCheck = arrayBoundCheck;
		cloned.hasWait = hasWait;
		cloned.minMultiplierDelay = minMultiplierDelay;
		return cloned;
	}
	
	public void addInstr(Line microInstr, Integer tos) {
		this.path.add(new PathEntry(microInstr,tos));
	}
	
	/** check that the instruction before the current one did not modify the TOS value */
	public void checkStableTOS() throws MicrocodeVerificationException {
		/* Unknown last instr: MAYBE modified TOS */
		if (path.size() == 1) {
			throw new MicrocodeVerificationException("last instruction maybe modified TOS (empty path)");
		}
		Instruction lastInstr = path.get(path.size() - 2).getInstruction();
		/* Some statements do not use the stack */
		if(lastInstr.noStackUse()) {
			return;
		}
		/* DUP does not modify the TOS */
		if(lastInstr.opcode == MicrocodeConstants.DUP) {
			return;
		}
		/* If we have a DUP first, and the next instruction consumes one element without producing
		 * any, the TOS values isn't modified */
		if(path.size() >= 3 && 
		   path.get(path.size() - 3).getInstruction().opcode == MicrocodeConstants.DUP &&
		     lastInstr.isStackConsumer() &&
		   ! lastInstr.isStackProducer()) {
			return;
		}
		/* Unknown: TOS was MAYBE modified */
		System.err.println("[WARNING] "+opName+" : "+
				           "last instruction maybe modified TOS: "+lastInstr);
	}
	
	public String toString() {
		String s = path.toString();
		if(hasWait) s+= "[wait]";
		if(nullPtrCheck) s+= "[check-null-ptr]";
		if(arrayBoundCheck) s+= "[check-array-bound]";
		if(minMultiplierDelay != Integer.MAX_VALUE) s+= "[multiplier/delay "+minMultiplierDelay+"]";
		return s;
	}
	
	public void setNullPtrCheck() {
		this.nullPtrCheck = true;
	}
	
	public void setArrayBoundCheck() {
		this.arrayBoundCheck = true;
	}
	
	public void setNeedsMultiplier(int delay) {
		this.minMultiplierDelay = Math.min(minMultiplierDelay, delay);
	}
	
	public void setHasWait() {
		this.hasWait = true;
	}
	
	public Vector<PathEntry> getPath() {
		return this.path;
	}
}