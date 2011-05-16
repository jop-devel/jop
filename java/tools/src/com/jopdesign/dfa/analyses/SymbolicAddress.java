/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber <benedikt.huber@gmail.com>
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

package com.jopdesign.dfa.analyses;

import com.jopdesign.common.misc.HashedString;
import com.jopdesign.dfa.framework.BoundedSetFactory;
import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class SymbolicAddress implements Serializable {

	private static final long serialVersionUID = 1L;

	enum Tag { ROOT, FIELD_ACCESS, ARRAY_ACCESS }

	private Tag tag;
	private int depth;
	private SymbolicAddressNode symname;
	
	
	private abstract static class SymbolicAddressNode {

		public abstract SymbolicAddress getParent();
		public abstract boolean equals_same(SymbolicAddressNode oth);

	}
	
	private static class RootObject extends SymbolicAddressNode implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private HashedString name;
		
		public RootObject(HashedString name) {
			this.name = name ;
		}
		public SymbolicAddress getParent() { 
			return null;
		}
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		public boolean equals_same(SymbolicAddressNode oth) {
			RootObject other = (RootObject) oth;
			return(name.equals(other.name));
		}
		@Override
		public String toString() {
			return name.toString();
		}
	}

	private static class FieldAccess extends SymbolicAddressNode implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private SymbolicAddress parent;
		private HashedString field;
		private int hashCode;
		private String stringReprCache = null;
		
		public FieldAccess(SymbolicAddress parent, String field) {
			this.parent = parent;
			this.field = new HashedString(field);
			hashCode = field.hashCode() * 31 + parent.hashCode();
		}
		public SymbolicAddress getParent() {
			return parent;
		}
		@Override
		public int hashCode() {
			return hashCode;
		}
		public boolean equals_same(SymbolicAddressNode oth) {
			FieldAccess other = (FieldAccess) oth;
			if(! field.equals(other.field))
				return false;
			return(parent.equals(other.parent));
		}
		@Override
		public String toString() {
			if(stringReprCache != null) return stringReprCache;
			StringBuffer sb = new StringBuffer();
			sb.append(parent.toString());
			sb.append(".");
			sb.append(field.toString());
			stringReprCache = sb.toString();
			return stringReprCache;
		}
	}

	private static class ArrayAccess extends SymbolicAddressNode implements Serializable {

		private static final long serialVersionUID = 1L;
		
		enum ArrayAccessKind { ANY, UNIQUE, ELEM }

		private SymbolicAddress parent;
		private ArrayAccessKind kind;
		private long index;
		private int hashCode;
		private String stringReprCache;
		
		public static ArrayAccess any(SymbolicAddress symbolicAddress) {
			ArrayAccess san = new ArrayAccess();
			san.parent = symbolicAddress;
			san.kind = ArrayAccessKind.ANY;
			san.hashCode = san.parent.hashCode() * 31 + 1;
			san.index = 0;
			return san;
		}
		public static ArrayAccess unique(SymbolicAddress symbolicAddress, long name) {
			ArrayAccess san = new ArrayAccess();
			san.parent = symbolicAddress;
			san.kind = ArrayAccessKind.UNIQUE;
			san.index = name;
			san.hashCode = san.parent.hashCode() * 31 + 2 + ((int)name)<<1;
			return san;
		}
		public static ArrayAccess element(SymbolicAddress symbolicAddress, int ix) {
			ArrayAccess san = new ArrayAccess();
			san.parent = symbolicAddress;
			san.kind = ArrayAccessKind.ELEM;
			san.index = ix;
			san.hashCode = san.parent.hashCode() * 31 + 3 + (ix)<<1;
			return san;
		}
		public SymbolicAddress getParent() { 
			return parent;
		}
		@Override
		public int hashCode() {
			return hashCode;
		}
		public boolean equals_same(SymbolicAddressNode oth) {
			ArrayAccess other = (ArrayAccess) oth;
			if(! kind.equals(other.kind))
				return false;
			if(index != other.index)
				return false;
			return(parent.equals(other.parent));
		}
		@Override
		public String toString() {
			if(stringReprCache != null) return stringReprCache;
			StringBuffer sb = new StringBuffer();
			sb.append(parent.toString());
			sb.append("[");
			switch(kind) {
			case ANY: sb.append("*"); break;
			case UNIQUE: sb.append("?"+index); break;
			case ELEM: sb.append(index); break;
			}
			sb.append("]");
			stringReprCache = sb.toString();
			return stringReprCache;
		}
	}

	// uh, bad coding style :( Sometimes I'm lazy too
	private static long globalGen = 0;
	private static HashMap<HashedString, RootObject> rootPool =
		new HashMap<HashedString, RootObject>();
	public static SymbolicAddress rootAddress(String root) {
		HashedString hstr = new HashedString(root);
		RootObject robj = rootPool.get(hstr);
		if(robj == null) {
			robj = new RootObject(hstr);
			rootPool.put(hstr, robj);
		}
		return new SymbolicAddress(Tag.ROOT, robj);
	}
	public static SymbolicAddress staticField(String fieldName) {
		return rootAddress(fieldName);
	}
	public static SymbolicAddress stringLiteral(String className, int cpIndex) {
		return rootAddress("@lit:"+className+":"+cpIndex);
	}
	public static SymbolicAddress newName() {
		long name = genName();
		return rootAddress("?x"+name);
	}
	public SymbolicAddress access(String fieldName) {
		return new SymbolicAddress(Tag.FIELD_ACCESS, new FieldAccess(this,fieldName));
	}
	public SymbolicAddress accessArrayAny() {
		return new SymbolicAddress(Tag.ARRAY_ACCESS, ArrayAccess.any(this));
	}
	public SymbolicAddress accessArrayUnique() {
		long name = genName();
		return new SymbolicAddress(Tag.ARRAY_ACCESS, ArrayAccess.unique(this, name));
	}
	public SymbolicAddress accessArray(int j) {
		return new SymbolicAddress(Tag.ARRAY_ACCESS, ArrayAccess.element(this, j));
	}
	public SymbolicAddress(Tag tag, SymbolicAddressNode san) {
		this.tag = tag;
		this.symname = san;
		this.depth = ( san.getParent() == null ) ? 0 : (san.getParent().depth + 1);
	}
	public String toString() {
		return symname.toString();
	}
	@Override
	public int hashCode() {
		return symname.hashCode();
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
		if (! tag.equals(other.tag))
			return false;
		if(depth != other.depth)
			return false;
		return symname.equals_same(other.symname);
	}
	
	public static BoundedSet<SymbolicAddress> fieldAccess(
			BoundedSetFactory<SymbolicAddress> bsFactory,
			BoundedSet<SymbolicAddress> objectMapping,
			String fieldName) {
		BoundedSet<SymbolicAddress> newMapping;
		if(objectMapping == null) {
			Logger.getLogger("Object Cache Analysis").error("Undefined mapping for "+fieldName);
			return bsFactory.top();
		}
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
	private static long genName() {
		return globalGen++;
	}

}
