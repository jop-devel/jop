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
import javax.safetycritical.annotate.SCJRestricted;

/**
 * What it the real usage of RealtimeThread in SCJ?
 * It is *just* the static method to get the current allocation context!!!
 * 
 * @author martin
 *
 */
@SCJAllowed(LEVEL_1)
public class RealtimeThread extends Thread implements Schedulable {


	// no instances allowed in level 0/1
	private RealtimeThread() {
		super(null);
	}

	@SCJAllowed(LEVEL_1)
	@SCJRestricted(maySelfSuspend = false)
	public static MemoryArea getCurrentMemoryArea() {
		throw new Error("implement me");
	}
}
