/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package gcinc;

public class SimpleList {
	
	class Element {
		Object element;
		Element next;
	}
	
	Element first, last;

	static final int POOL_SIZE = 4; // limits capacity!
	Element[] pool;
	int poolIndex;

	public SimpleList() {
		pool = new Element[POOL_SIZE];
		for (int i = 0; i < POOL_SIZE; i++) {
			pool[i] = new Element();
		}
		poolIndex = 0;
	}

	public void append(Object o) {
		Element e = new Element();
		e.element = o;
		synchronized (this) {
			if (last!=null) {
				last.next = e;
			} else {
				first = e;
			}
			last = e;
		}
	}
	
	public void appendPooled(Object o) {
		Element e = null;
		synchronized(this) {
			e = pool[poolIndex];
			poolIndex++;
			if (poolIndex >= POOL_SIZE) {
				poolIndex = 0;
			}
		}
		e.element = o;
		e.next = null;
		synchronized (this) {
			if (last!=null) {
				last.next = e;
			} else {
				first = e;
			}
			last = e;
		}
	}
	
	public Object remove() {		
		Object o = null;
		synchronized (this) {
			if (first!=null) {
				Element e = first;
				o = e.element;
				first = e.next;
				if (first==null) {
					last = null;
				}
			}
		}
		return o;
	}
}
