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

import java.util.Map;

import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.build.MethodInfo;

public interface Analysis<K, V> {

	public ContextMap<K, V>	bottom();
	public ContextMap<K, V>	initial(InstructionHandle stmt);

	/**
	 * Initialize the analysis
	 * @param entry The entry method (main)
	 * @param context The initial context
	 */
	public void				initialize(MethodInfo entry, Context context);
	
	public ContextMap<K, V> transfer(InstructionHandle stmt,
									FlowEdge edge,
									ContextMap<K, V> input,
									Interpreter<K, V> interpreter,
									Map<InstructionHandle, ContextMap<K, V>> state);
	/**
	 * {@code compare(s1,s2)} returns {@code true} if and only if both s1 and s2 have the same context
	 * and s1 \subseteq s2 (s1 `join` s2 = s2)
	 * 
	 */
	public boolean		 	compare(ContextMap<K, V> s1, ContextMap<K, V> s2);
	public ContextMap<K, V> join(ContextMap<K, V> s1, ContextMap<K, V> s2);
	
	public Map			 	getResult();
	public void				printResult(DFAAppInfo program);
}
