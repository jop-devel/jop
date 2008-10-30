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

/**
 * Why do we need absolute time in SCJ?
 */
public class AbsoluteTime extends HighResolutionTime {

//	@BlockFree
//	@SCJAllowed
	public AbsoluteTime(AbsoluteTime time) {
		millis = time.millis;
		nanos = time.nanos;
	}

//	@BlockFree
//	@SCJAllowed
	public AbsoluteTime(long millis, int nanos) {
		this.millis = millis;
		this.nanos = nanos;
	}

//	@Allocate( { CURRENT })
//	@BlockFree
//	@SCJAllowed
	public AbsoluteTime add(long millis, int nanos) {
		return null; // dummy return
	}

//	@Allocate( { CURRENT })
//	@BlockFree
//	@SCJAllowed
	public AbsoluteTime add(RelativeTime time) {
		return null; // dummy return
	}


//	@Allocate( { CURRENT })
//	@BlockFree
//	@SCJAllowed
	public RelativeTime subtract(AbsoluteTime time) {
		return null; // dummy return
	}


//	@Allocate( { CURRENT })
//	@BlockFree
//	@SCJAllowed
	public AbsoluteTime subtract(RelativeTime time) {
		return null; // dummy return
	}

//	TBD
//	public RelativeTime relative(Clock clock) {
//		return relative(clock, new RelativeTime());
//	}

//	TBD
//	public RelativeTime relative(Clock clock, RelativeTime dest) {
//		return null; // dummy return
//	}

}
