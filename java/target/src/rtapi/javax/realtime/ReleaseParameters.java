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

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;
import javax.safetycritical.annotate.SCJRestricted;

@SCJAllowed
public abstract class ReleaseParameters { // implements java.lang.Cloneable {

	// not in the spec
	// @SCJProtected
	// protected ReleaseParameters(RelativeTime cost, RelativeTime deadline,
	// AsyncEventHandler overrunHandler,
	// AsyncEventHandler missHandler)
	// { }

	/**
	 * Ooh, all those empty constructors...
	 */
	@SCJAllowed
	protected ReleaseParameters() {
	}

	@SCJAllowed(LEVEL_1)
	protected ReleaseParameters(RelativeTime deadline,
			AsyncEventHandler missHandler) {
	}

	/**
	 * I don't like clone....
	 */
	@SCJAllowed(LEVEL_1)
	public Object clone() {
		return null;
	}

	/**
	 * That's not currently in the spec.
	 * 
	 * @return
	 */
	@SCJAllowed(LEVEL_1)
	public AsyncEventHandler getDeadlineMissHandler() {
		return null;
	}

	/**
	 * That's not currently in the spec.
	 * 
	 * @return
	 */
	@SCJRestricted(maySelfSuspend = false)
	@SCJAllowed(LEVEL_1)
	public RelativeTime getDeadline() {
		return null;
	}
}
