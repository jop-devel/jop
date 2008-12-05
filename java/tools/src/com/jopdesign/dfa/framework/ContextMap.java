/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Wolfgang Puffitsch

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

package com.jopdesign.dfa.framework;

import java.util.HashMap;
import java.util.Map;

public class ContextMap<K, V> extends HashMap<K, V> {
	
	private static final long serialVersionUID = 1L;
	
	private Context context;
	
	public ContextMap(Context context, Map<K, V> map) {
		super(map);
		this.context = context;
	}

	public ContextMap(ContextMap<K, V> map) {
		super(map);
		this.context = map.context;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void add(Object elem) {
		put((K)elem, (V)elem);
	}
}
