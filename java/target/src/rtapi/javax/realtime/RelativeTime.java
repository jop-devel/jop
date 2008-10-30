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

//import javax.safetycritical.annotate.Allocate;
//import static javax.safetycritical.annotate.Allocate.Area.CURRENT;

//@SCJAllowed
public class RelativeTime extends HighResolutionTime {



	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	/**
	 * Create and normalize the time.
	 * How are negative values normalized?
	 * 
	 * TODO: see in the OVM source if GPLed versions exist.
	 * @param ms
	 * @param ns
	 */
//	@BlockFree
//	@SCJAllowed
	public RelativeTime(long ms, int ns) {
		ms += ns/1000;
		ns %= 1000;
		if (ns<0) {
			ns += 1000;
			ms--;
		}
		millis = ms;
		nanos = ns;
	}

	/**
	 * Why do we need this constructor when RelativeTime is in
	 * fact immutable?
	 * @param time
	 */
//	@BlockFree
//	@SCJAllowed
	public RelativeTime(RelativeTime time) {
		millis = time.millis;
		nanos = time.nanos;
	}

	/**
	 * TBD: is AbsoluteTime mutable?
	 */
	public AbsoluteTime absolute(Clock clock, AbsoluteTime destination) {
		return null; // dummy return
	}

	/**
	 * TBD: it is not "safe" to automatically convert from one clock basis
	 * to another. Do we want to support this?
	 */
	public AbsoluteTime absolute(Clock clock) {
		return absolute(clock, null);
	}


	public RelativeTime relative(Clock clock) {
		return null; // dummy return
	}
	public RelativeTime relative(Clock clock, RelativeTime destination) {
		return null; // dummy return
	}

//	@Allocate({CURRENT})
//	@BlockFree
//	@SCJAllowed
	public RelativeTime add(long millis, int nanos) {
		return new RelativeTime(this.millis + millis, this.nanos + nanos);
	}

//	@Allocate({CURRENT})
//	@BlockFree
//	@SCJAllowed
	public RelativeTime add(RelativeTime time) {
		return add(time.millis, time.nanos);
	}

//	@Allocate({CURRENT})
//	@BlockFree
//	@SCJAllowed
	public RelativeTime subtract(RelativeTime time) {
		return new RelativeTime(millis - time.millis, nanos - time.nanos);
	}
}

