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

/**
 * Purpose: A functional map for Iterables
 * 
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public abstract class MappedIterable<T1, T2> implements Iterable<T2> {

	private Iterable<? extends T1> source;

	/**
	 * @param source input generator
	 */
	public MappedIterable(Iterable<? extends T1> source) {
		this.source = source;
	}

	protected abstract T2 map(T1 in);

	private class MapIterator implements Iterator<T2> {
		private Iterator<? extends T1> sourceIterator;
		public MapIterator() {
			this.sourceIterator = source.iterator();
		}
		@Override
		public boolean hasNext() {
			return sourceIterator.hasNext();
		}
		@Override
		public T2 next() {
			T1 in = sourceIterator.next();
			if(in == null) return null;
			return map(in);
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove() not supported by MapIterator");
		}
	}
	@Override
	public Iterator<T2> iterator() {
		return new MapIterator();
	}
}
