package com.jopdesign.dfa.analyses;

public class HashedString {

	private String value;
	private int hash;
	
	public HashedString(String value) {
		this.value = value;
		this.hash = value.hashCode();
	}
	
	public int hashCode() {
		return hash;
	}
	
	public String toString() {
		return value;
	}
	
	public boolean equals(Object o) {
		return value.equals(o.toString());
	}
}
