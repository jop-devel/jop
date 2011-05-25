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

package javax.realtime;

import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;
import static javax.safetycritical.annotate.Level.LEVEL_1;
import javax.safetycritical.annotate.MemoryAreaEncloses;

@SCJAllowed
public class PeriodicParameters extends ReleaseParameters {

	RelativeTime start;
	RelativeTime period;

	/**
	 * TODO: is this a legal SCJ constructor? It is not part of the spec source.
	 * 
	 * @param start
	 * @param period
	 */
	public PeriodicParameters(RelativeTime start, RelativeTime period) {
		this.start = start;
		this.period = period;
	}

	/**
	 * Construct a new object within the current memory area.
	 * 
	 * @parameter start isrelative to the start of the mission. A null value
	 *            defaults to an offset of zero milliseconds.
	 * 
	 * @parameter period is the time between each release of the associated
	 *            schedulable object. The default deadline is the same value as
	 *            the period. The default handler is null.
	 * 
	 * @throws IllegalArgumentException
	 *             if period is null.
	 */
	@SCJAllowed
	@BlockFree
	public PeriodicParameters(HighResolutionTime start, RelativeTime period) {
		throw new Error("implement me");
	}

	/**
	 * Construct a new object within the current memory area.
	 * 
	 * @parameter start isrelative to the start of the mission. A null value
	 *            defaults to an offset of zero milliseconds.
	 * 
	 * @parameter period is the time between each release of the associated
	 *            schedulable object.
	 * 
	 * @parameter deadline is an offset from the release time by which the
	 *            release should finish. A null deadline indicates the same
	 *            value as the period.
	 * @parameter handler is the async event handler to be release if the
	 *            associated schedulable object misses its deadline. A null
	 *            parameter indicates that no handler should be release.
	 * 
	 * @throws IllegalArgumentException
	 *             if period is null.
	 */
	@SCJAllowed
	@BlockFree
	public PeriodicParameters(HighResolutionTime start, RelativeTime period,
			RelativeTime deadline, AsyncEventHandler handler) {
		throw new Error("implement me");
	}

	/**
	 * @return Returns the object originally passed in to the constructor, which
	 *         is known to reside in a memory area that encloses this.
	 */
	@BlockFree
	@SCJAllowed
	public HighResolutionTime getStart() {
		return null;
	}

	/**
	 * @return Returns the object originally passed in to the constructor, which
	 *         is known to reside in a memory area that encloses this.
	 */
	@BlockFree
	@SCJAllowed
	public RelativeTime getPeriod() {
		return period;
	}

	/**
	 * @return Returns the object originally passed in to the constructor, which
	 *         is known to reside in a memory area that encloses this.
	 */
	@BlockFree
	@SCJAllowed
	public RelativeTime getDeadline() {
		return null;
	}

	/**
	 * @return Returns the object originally passed in to the constructor, which
	 *         is known to reside in a memory area that encloses this.
	 */
	@BlockFree
	@SCJAllowed
	public AsyncEventHandler getHandler() {
		return null;
	}
}
