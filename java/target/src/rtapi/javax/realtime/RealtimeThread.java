/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>
  This subset of javax.realtime is provided for the JSR 302
  Safety Critical Specification for Java

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

import static javax.safetycritical.annotate.Level.LEVEL_1;
import static javax.safetycritical.annotate.Level.LEVEL_2;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * What it the real usage of RealtimeThread in SCJ?
 * 
 * @author martin
 *
 */
@SCJAllowed(LEVEL_1)
public class RealtimeThread extends Thread implements Schedulable {
	// public RealtimeThread(SchedulingParameters scheduling,
	// ReleaseParameters release,
	// MemoryParameters memory,
	// MemoryArea area,
	// ProcessingGroupParameters group,
	// java.lang.Runnable logic)
	// { // skeleton
	// }

	/**
	 * Should not be visible.
	 */
	@SCJProtected
	public RealtimeThread(SchedulingParameters schedule, MemoryArea area) {
		super(null);
	}

	@SCJProtected
	public RealtimeThread() {
		super(null);
	}

	public void deschedulePeriodic() {
	}

	/**
	 * Do we need this information in SCJ? We don't have the concept of threads
	 * in Level 1.
	 * 
	 */
	@SCJAllowed(LEVEL_2)
	@SCJRestricted(maySelfSuspend = false)
	public static RealtimeThread currentRealtimeThread() {
		throw new Error("implement me");
	}

	@SCJAllowed(LEVEL_1)
	@SCJRestricted(maySelfSuspend = false)
	public static MemoryArea getCurrentMemoryArea() {
		throw new Error("implement me");
	}

	@SCJAllowed(LEVEL_2)
	@SCJRestricted(maySelfSuspend = true)
	public static void sleep(AbstractTime time)
			throws InterruptedException {
	};

	/**
	 * Allocates no memory. Does not allow this to escape local variables. The
	 * returned object may reside in scoped memory, within a scope that encloses
	 * this.
	 */
	@SCJAllowed(LEVEL_2)
	@SCJRestricted(maySelfSuspend = false)
	public MemoryArea getMemoryArea() {
		throw new Error("implement me");
	}

	/**
	 * Not @SCJAllowed because ThreadConfigurationParameters releases
	 * MemoryParameters.
	 */
	public MemoryParameters getMemoryParameters() {
		return null; // dummy return
	}

	/**
	 * Allocates no memory. Does not allow this to escape local variables. The
	 * returned object may reside in scoped memory, within a scope that encloses
	 * this.
	 * <p>
	 * No allocation because ReleaseParameters are immutable.
	 */
	// @BlockFree
	// @SCJAllowed(LEVEL_2)
	public ReleaseParameters getReleaseParameters() {
		return null; // dummy return
	}

	/**
	 * Allocates no memory. Does not allow this to escape local variables. The
	 * returned object may reside in scoped memory, within a scope that encloses
	 * this.
	 * <p>
	 * No allocation because SchedulingParameters are immutable.
	 */
	// @BlockFree
	// @SCJAllowed(LEVEL_2)
	public SchedulingParameters getSchedulingParameters() {
		return null; // dummy return
	}

	/**
	 * Allocates no memory. Treats the implicit this argument as a variable
	 * residing in scoped memory.
	 */
	public void start() {
	}

	public void release() {
	}

	public static boolean waitForNextRelease() {
		return false; // dummy return
	}

	public static int getMemoryAreaStackDepth() {
		return -1;
	}

	public static MemoryArea getOuterMemoryArea(int delta) {
		return null;
	}

}
