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
