package com.jopdesign.dfa.analyses;

public class Pair<E> {

	private E first;
	private E second;
	
	public Pair(E first, E second) {
		this.first = first;
		this.second = second;
	}
	
	public Pair() {	}

	public E getFirst() {
		return first;
	}

	public void setFirst(E first) {
		this.first = first;
	}

	public E getSecond() {
		return second;
	}

	public void setSecond(E second) {
		this.second = second;
	}
	
	public String toString() {
		return "<"+first+","+second+">";
	}
}
