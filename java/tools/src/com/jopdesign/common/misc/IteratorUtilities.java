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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperInvokeEdge;

/**
 * Purpose: Utilities to lift Collection functionality to Iterable s and Iterator s
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class IteratorUtilities {

	public static<T> int size(Iterable<T> nodes) {
		
		int i = 0;
		for(T _ : nodes) i+=1;
		return i;
	}

	public static<T, C extends Collection<T>> C addAll(C coll, Iterable<? extends T> addme) {
		
		for(T e : addme) { coll.add(e); }
		return coll;
	}

	public static<T> Iterable<T> singleton(T elem) {
		return Collections.singleton(elem);
	}
}
