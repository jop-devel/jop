/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Wolfgang Puffitsch
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

package com.jopdesign.dfa.framework;

import org.apache.bcel.generic.InstructionHandle;

public class FlowEdge {

	public static final int NORMAL_EDGE = 0;
	public static final int TRUE_EDGE = 1;
	public static final int FALSE_EDGE = 2;	
	
	private final InstructionHandle tail;
	private final InstructionHandle head;
	private final Context context;
	private final int type;
	
	public FlowEdge(InstructionHandle tail, InstructionHandle head, int type) {
		this.tail = tail;
		this.head = head;
		this.context = null;
		this.type = type;
	}

	public FlowEdge(FlowEdge f, Context c) {
		this.tail = f.tail;
		this.head = f.head;
		this.context = c;
		this.type = f.type;
	}

	public InstructionHandle getHead() {
		return head;
	}

	public InstructionHandle getTail() {
		return tail;
	}

	public Context getContext() {
		return context;
	}

	public int getType() {
		return type;
	}
	
	public String toString() {
		return tail.toString(false)+" -> "+head.toString(false);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((context == null) ? 0 : context.hashCode());
		result = PRIME * result + ((head == null) ? 0 : head.getInstruction().hashCode());
		result = PRIME * result + ((head == null) ? 0 : head.getPosition());
		result = PRIME * result + ((tail == null) ? 0 : tail.getInstruction().hashCode());
		result = PRIME * result + ((tail == null) ? 0 : tail.getPosition());
		result = PRIME * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FlowEdge other = (FlowEdge) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (head == null) {
			if (other.head != null)
				return false;
		} else if (!head.equals(other.head))
			return false;
		if (tail == null) {
			if (other.tail != null)
				return false;
		} else if (!tail.equals(other.tail))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
