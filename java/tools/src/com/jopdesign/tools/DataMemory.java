/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.tools;

import java.io.PrintStream;
import java.util.Collection;

/**
 * A Simulation Interface for standard data memory or cached data memory. The unit for addresses is
 * word (implying all accesses are word aligned).
 *
 */
public abstract class DataMemory  {
	public class AccessTypeUnsupported extends Error {
		public AccessTypeUnsupported(String msg) { super(msg); }
		private static final long serialVersionUID = -1904217311124106007L;		
	}
	
	public interface DataMemoryStats {
		public void reset();
		public void dump(PrintStream out);
		public DataMemoryStats addAverage(Collection<? extends DataMemoryStats> stats);
	}
	/**
	 * Classify memory access type for cache and TM experiments.
	 */
	public enum Access {
		/**
		 * General class info
		 */
		CLINFO,
		/**
		 * Method table
		 */
		MTAB,
		/**
		 * Constant pool
		 */
		CONST,
		/**
		 * Interface table
		 */
		IFTAB,
		/**
		 * Handle indirection
		 */
		HANDLE,
		/**
		 * Array length
		 */
		ALEN,
		/**
		 * Method vector base (= class reference)
		 */
		MVB,
		/**
		 * Field access
		 */
		FIELD,
		/**
		 * Static field access
		 */
		STATIC,
		/**
		 * Array access
		 */
		ARRAY,
		/**
		 * JVM internal access
		 */
		INTERN;
		
		public static int getMaxOrdinal() {
			return Access.values().length - 1;
		}
		public boolean isMutableData() {
			return this == Access.HANDLE || this == Access.STATIC || this == Access.FIELD || this == Access.ARRAY;
		}
	}

	public abstract int read(int addr, Access type);

	public abstract void write(int addr, int value, Access type);

	// Object cache instructions with default implementations
	public int readField(int handle, int offset, Access type) throws AccessTypeUnsupported {
		int address = read(handle, Access.HANDLE);
		return read(address + offset, type);
	}

	// Write object or array field
	public void writeField(int handle, int offset, int value, Access type) throws AccessTypeUnsupported {
		int address = read(handle, Access.HANDLE);
		write(address + offset, value, type);		
	}
	
	public abstract void invalidateData();
	public abstract void invalidateHandles();

	// Debugging / Analytics

	/** reset statistics */
	public abstract void resetStats();
	/** push statistics on a save stack (typically at the end of e.g. main or the bench() method) */
	public abstract void recordStats();
	
	public abstract String getName();
	public abstract void dump(PrintStream out);
}

