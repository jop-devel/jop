package com.jopdesign.dfa.analyses;


public class SymbolicAddress {
	private String impl;
	public SymbolicAddress(String root) {
		this.impl = root;
	}
	public String toString() {
		return impl;
	}
	public SymbolicAddress access(String fieldName) {
		return new SymbolicAddress(impl + "." + fieldName); 
	}
	public static SymbolicAddress staticField(String fieldName) {
		return new SymbolicAddress(fieldName);
	}
}
