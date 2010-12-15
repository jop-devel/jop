/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Benedikt Huber (benedikt.huber@gmail.com)

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

package com.jopdesign.timing.jop;

import java.io.Serializable;
import java.util.Vector;

import com.jopdesign.timing.jop.MicrocodeAnalysis.MicrocodeVerificationException;
import com.jopdesign.tools.Instruction;
import com.jopdesign.tools.Jopa.Line;

/**
 * A {@code MicrocodePath} represents a microcode instruction sequence.
 * Each path is a list of {@code PathEntry} objects, which correspond to
 * single microcode instructions, along with some static analysis information.
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class MicrocodePath implements Serializable {
	private static final long serialVersionUID = 1L;

	/** Path Entry, 'constant', i.e. you never modify any fields after construction */
	public class PathEntry implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Line line;
		private final Integer tos; /** TOS (op of stack), if available */

		private PathEntry(Line l) { this(l,null); }
		private PathEntry(Line l, Integer tos) {
			this.line = l;
			this.tos  = tos;
		}
		/** Get the microcode instruction
		 * @return
		 */
		public Instruction getInstruction() {
			return line.getInstruction();
		}
		/** Return the top of stack value, if it is known at this instruction
		 *
		 * @return the TOS, or {@code null} if the TOS isn't known at this point
		 */
		public Integer getTOS() {
			return tos;
		}
		@Override public String toString() {
			Instruction i = getInstruction();
			String stringRepr = i.name;
			if(i.opdSize!=0) {
				if(line.getSymVal() != null) stringRepr += " "+line.getSymVal();
				else                         stringRepr += " "+line.getIntVal();
			}
			if(tos != null) { stringRepr += "{tos="+tos+"}"; }
			return stringRepr;
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
	/** Get the micro code instruction sequence aggregated by this {@code MicrocodePath}
	 *
	 * @return
	 */
	public Vector<PathEntry> getPath() {
		return this.path;
	}

	/**
	 * Query whether there is a byte code load instruction on the path
	 * @return
	 */
	public boolean hasBytecodeLoad() {
		for(PathEntry instr : path) {
			if(instr.getInstruction().opcode == MicrocodeConstants.STBCRD) return true;
		}
		return false;
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

	@Override public String toString() {
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

}
