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
