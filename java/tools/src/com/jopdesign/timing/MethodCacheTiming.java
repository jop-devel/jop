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

package com.jopdesign.timing;

/**
 * Purpose: For processors with a method cache, provide timing information
 * on the method cache load times
 */
public interface MethodCacheTiming {

	/**
	 * @param words number of words to load
	 * @param loadOnInvoke whether this is an invoke or return instruction
	 * @return the maximum miss penalty for loading {@code words} words from method cache
	 */
	long getMethodCacheMissPenalty(int words, boolean loadOnInvoke);

	/**
	 * @param words number of words to load
	 * @param instructionOpcode opcode the invoke instruction
	 * @return the maximum miss penalty for loading {@code words} words from method cache during {@code instructionOpcode}
	 */
	long getMethodCacheMissPenalty(int words, short instructionOpcode);

}
