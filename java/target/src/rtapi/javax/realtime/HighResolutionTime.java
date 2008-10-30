/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>
  This subset of javax.realtime is provided for the JSR 302
  Safety Critical Specification for Java

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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

// @SCJAllowed
public abstract class HighResolutionTime implements java.lang.Comparable {

	long millis;
	int nanos;
	
//	@BlockFree
//	@SCJAllowed
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@BlockFree
//	@SCJAllowed
	public int compareTo(HighResolutionTime time) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@BlockFree
//	@SCJAllowed
	public boolean equals(HighResolutionTime time) {
		return (millis==time.millis && nanos==time.nanos);
	}

//	@BlockFree
//	@SCJAllowed
	public boolean equals(java.lang.Object object) {
		return false; // dummy return
	}

//	@BlockFree
//	@SCJAllowed
	public Clock getClock() {
		return Clock.getRealtimeClock();
	}

//	@BlockFree
//	@SCJAllowed
	public final long getMilliseconds() {
		return millis;
	}

//	@BlockFree
//	@SCJAllowed
	public final int getNanoseconds() {
		return nanos;
	}

//	@BlockFree
//	@SCJAllowed
	public int hashCode() {
		// TODO lookup the chapter how to best generate a hash code.
		return (int) millis + nanos;
	}

//	TBD
//	public abstract RelativeTime relative(Clock clock);
//	public abstract RelativeTime relative(Clock clock, RelativeTime time);
}
