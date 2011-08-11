/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.common.misc;

import java.util.Iterator;
import java.util.Set;

public abstract class Filter<T> {

	private abstract static class FilterIterator<T> implements Iterator<T> {

		private Iterator<? extends T> src;
		private T buf;

		public abstract boolean include(T e);

		public FilterIterator(Iterable<? extends T> source) {
			this(source.iterator());
		}
		
		public FilterIterator(Iterator<? extends T> source) {
			this.src = source;
			this.buf = null;
		}
		
		@Override
		public boolean hasNext() {
			if(buf == null) buf = next();
			return (buf != null);
		}

		@Override
		public T next() {
			T tmp = buf;
			if(tmp != null) {
				buf = null;
				return tmp;
			} 
			while(src.hasNext()) {
				tmp = src.next();
				if(include(tmp)) {
					return tmp;
				}
			}
			return null;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove() not supperted for EdgeFilter");
		}	
	}
	
	public <S extends T> Iterable<S> filter(final Iterable<? extends S> source) {
		
		return new Iterable<S>() {
			@Override
			public Iterator<S> iterator() {
				return new Filter.FilterIterator<S>(source) {
					@Override
					public boolean include(S e) {
						return Filter.this.include(e);
					}
				};
			}				
		};				
	}

 	protected abstract boolean include(T e);

	private static class SetFilter<T> extends Filter<T> {
		private Set<T> filterSet;
		public SetFilter(Set<T> filterSet) {
			this.filterSet = filterSet;
		}
		@Override
		protected boolean include(T e) {
			return filterSet.contains(e);
		}				
	}
	
	/**
	 * @param set the set all elements passing the filter need to be contained in
	 * @return
	 */
	public static <T> Filter<T> isContainedIn(Set<T> set) {

		return new SetFilter<T>(set);
	}
}