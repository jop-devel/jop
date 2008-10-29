/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Wolfgang Puffitsch

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

package com.jopdesign.dfa.framework;

import org.apache.bcel.generic.InstructionHandle;

public class FlowEdge {

	public static final int NORMAL_EDGE = 0;
	public static final int TRUE_EDGE = 1;
	public static final int FALSE_EDGE = 2;	
	
	private InstructionHandle tail;
	private InstructionHandle head;
	private Context context;
	private int type;
	
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
	
	public boolean equals(Object o) {
		FlowEdge f = (FlowEdge)o;
		return tail.equals(f.tail)
				&& head.equals(f.head)
				&& context.equals(f.context)
				&& type == f.type;
	}
}
