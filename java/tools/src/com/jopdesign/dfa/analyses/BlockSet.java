package com.jopdesign.dfa.analyses;

import java.util.BitSet;

public class BlockSet extends BitSet {

	private static final long serialVersionUID = 1L;

	private boolean must;

	public BlockSet(int size) {
		super(size);
		must = true;
	}

	public boolean isMust() {
		return must;
	}

	public void setMust(boolean must) {
		this.must = must;
	}
	
	public boolean equals(Object o) {
		BlockSet b = (BlockSet)o;
		return (must == b.must) && super.equals(b);
	}
	
	public String toString() {
		return super.toString()+(must?"!":"?");
	}
}
