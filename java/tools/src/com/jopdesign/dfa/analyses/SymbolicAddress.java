package com.jopdesign.dfa.analyses;

/**
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
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
	public SymbolicAddress accessArrayAny() {
		return new SymbolicAddress(impl + "[]");
	}
	public SymbolicAddress accessArray(int j) {
		return new SymbolicAddress(impl + "["+j+"]"); 
	}
	public static SymbolicAddress staticField(String fieldName) {
		return new SymbolicAddress(fieldName);
	}
	@Override
	public int hashCode() {
		return impl.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SymbolicAddress other = (SymbolicAddress) obj;
		return impl.equals(other.impl);
	}
}
