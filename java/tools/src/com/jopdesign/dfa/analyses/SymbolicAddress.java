package com.jopdesign.dfa.analyses;

import com.jopdesign.dfa.framework.BoundedSetFactory;
import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;

/**
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class SymbolicAddress {
	private String impl;
	// uh, bad coding style :( Sometimes I'm lazy too
	private static int globalGen = 0;
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
	public SymbolicAddress accessArrayUnique() {
		String name = genName();
		return new SymbolicAddress(impl + "["+name+"]");
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
	
	public static BoundedSet<SymbolicAddress> fieldAccess(
			BoundedSetFactory<SymbolicAddress> bsFactory,
			BoundedSet<SymbolicAddress> objectMapping,
			String fieldName) {
		BoundedSet<SymbolicAddress> newMapping;
		if(objectMapping.isSaturated()) {
			newMapping = bsFactory.top();
		} else {
			newMapping = bsFactory.empty();
			for(SymbolicAddress addr: objectMapping.getSet()) {
				newMapping.add(addr.access(fieldName));
			}
		}
		return newMapping;
	}
	public static SymbolicAddress newName() {
		return new SymbolicAddress(genName());
	}
	private static String genName() {
		return "?x"+(globalGen++);
	}
}
