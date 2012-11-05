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

/**
 * Construct a new object within the current memory area.
 * 
 * @parameter deadline is an offset from the release time by which the release
 *            should finish. A null deadline indicates that there is no
 *            deadline.
 * @parameter handler is the async event handler to be release if the associated
 *            schedulable object misses its deadline. A null parameter indicates
 *            that no handler should be release.
 */
@SCJAllowed(LEVEL_1)
public class AperiodicParameters extends ReleaseParameters {
	
	private RelativeTime deadline;
	private AsyncEventHandler missHandler;
	
	@SCJAllowed(LEVEL_1)
	public AperiodicParameters() {
		this.deadline = null;
		this.missHandler = null;
	}

	@SCJAllowed(LEVEL_1)
	public AperiodicParameters(RelativeTime deadline,
			AsyncEventHandler missHandler) {
		this.deadline = deadline;
		this.missHandler = missHandler;
	}
}
