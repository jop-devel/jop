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

package com.jopdesign.dfa.framework;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/** 
 * Factory for sets with a maximum size N.
 * For every bound N, there is one factory, and one top element.
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */

public class BoundedSetFactory<V> {
	private int limit;
	private BoundedSet<V> top;

	public BoundedSetFactory(int limit) {
		this.limit = limit;
		BoundedSetImpl topset = new BoundedSetImpl();
		topset.isSaturated = true;
		this.top = topset;
	}

	public BoundedSet<V> empty() {
		return new BoundedSetImpl();
	}
	public BoundedSet<V> singleton(V el) {
		BoundedSetImpl bs = new BoundedSetImpl();
		bs.add(el);
		return bs;
	}
	public BoundedSet<V> top() {
		return top;
	}
	
	public interface BoundedSet<V>  {
		BoundedSet<V> newBoundedSet();
		
		void add(V el);
		void addAll(BoundedSet<V> other);
		BoundedSet<V> join(BoundedSet<V> other);
		boolean isSaturated();
		/** precondition: not (isTop()) */
                Set<V> getSet();
		int getSize();
		boolean isSubset(BoundedSet<V> otherEntry);

                int getLimit();
	}
	
	/** 
	 * Implementation for sets with a maximum size N.
	 * 
	 * @author Benedikt Huber <benedikt.huber@gmail.com>
	 */
	public class BoundedSetImpl implements BoundedSet<V>, Serializable {

		private static final long serialVersionUID = 1L;
		
		private Set<V> setImpl;
		private boolean isSaturated;
		
		public BoundedSetImpl() {
			setImpl = new LinkedHashSet<V>();
		}
		
		private BoundedSetImpl(LinkedHashSet<V> set) {
			if(set.size() > limit) {
				this.isSaturated = true;
			} else {
				setImpl = set;
			}
		}
		
		public void add(V el) {
			if(this.isSaturated()) return; 
			setImpl.add(el);
			if(setImpl.size()  > limit) { setTop(); }
		}
		public void addAll(BoundedSet<V> other) {
			if(this.isSaturated()) return; 
			if(other.isSaturated()) { setTop(); return; }
			setImpl.addAll(other.getSet());
			if(setImpl.size()  > limit) { setTop(); }
		}
		public BoundedSet<V> join(BoundedSet<V> other) {
			if(this.isSaturated()) return this;
			else if(other != null && other.isSaturated()) return other;
			
			LinkedHashSet<V> joinedSet = new LinkedHashSet<V>();
			joinedSet.addAll(this.getSet());
			if(other!=null) joinedSet.addAll(other.getSet());
			BoundedSetImpl r = new BoundedSetImpl(joinedSet);
			return r;
		}


		public Set<V> getSet() {
			return setImpl;
		}
		public boolean isSaturated() {
			return this.isSaturated;
		}
		private void setTop() {
			this.isSaturated = true;
			this.setImpl = null;
		}
		public int getSize() {
			if(setImpl.size() > limit) {
				throw new AssertionError("Bounded Set exceeded size: "+setImpl.size());
			}
			if(isSaturated) return limit+1;
			else return setImpl.size();
		}
		public int getLimit() {
			return limit;
		}

		@Override
		public int hashCode() {
			if(isSaturated) return 1;
			else            return 2 + setImpl.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			BoundedSetImpl other = (BoundedSetImpl) obj;
			if(this.isSaturated) return other.isSaturated;
			if(this.getSize() != other.getSize()) return false;
			else                 return setImpl.equals(other.setImpl);
		}

		public boolean isSubset(BoundedSet<V> otherEntry) {
			if(otherEntry.isSaturated()) return true;
			else if(this.isSaturated())  return false;
			return otherEntry.getSet().containsAll(this.getSet());
		}
		@Override
		public String toString() {
			if(this.isSaturated) return "BoundedSet.TOP";
			return this.setImpl.toString();
		}
		
		public BoundedSet<V> newBoundedSet() {
			return new BoundedSetImpl(new LinkedHashSet<V>());
		}
	}


}
