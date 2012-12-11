/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

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

package javax.safetycritical;

import javax.safetycritical.annotate.SCJAllowed;

/**
 * StorageParameters
 * 
 * This class might get renamed to MemoryParameters (back again).
 * 
 */
@SCJAllowed
public class StorageParameters {

	private long totalBackingStore;

	@SCJAllowed
	public StorageParameters(long totalBackingStore, long[] sizes,
			int messageLength, int stackTraceLength)

	{
		this.totalBackingStore = totalBackingStore;
	}

	@SCJAllowed
	public StorageParameters(long totalBackingStore, long[] sizes) {
		this(totalBackingStore, sizes, 0, 0);
	}

	// Both of these methods are not in the spec:

	long getTotalBackingStoreSize() {
		return totalBackingStore;
	}
}
