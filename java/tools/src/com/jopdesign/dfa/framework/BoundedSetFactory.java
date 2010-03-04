package com.jopdesign.dfa.framework;

import java.util.HashSet;
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
	
	public static interface BoundedSet<V>  {
		public void add(V el);
		public void addAll(BoundedSet<V> other);
		public BoundedSet<V> join(BoundedSet<V> other);
		public boolean isSaturated();
		/** precondition: not (isTop()) */
		public Set<V> getSet();
		public int getSize();
		public boolean isSubset(BoundedSet<V> otherEntry);
	}
	
	/** 
	 * Implementation for sets with a maximum size N.
	 * 
	 * @author Benedikt Huber <benedikt.huber@gmail.com>
	 */
	public class BoundedSetImpl implements BoundedSet<V> {
		private Set<V> setImpl;
		private boolean isSaturated;
		public BoundedSetImpl() {
			setImpl = new HashSet<V>();
		}
		private BoundedSetImpl(HashSet<V> set) {
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
			
			HashSet<V> joinedSet = new HashSet<V>();
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
			if(isSaturated) return limit+1;
			else return setImpl.size();
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
	}


}
