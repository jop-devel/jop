/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Martin Schoeberl (martin@jopdesign.com)

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

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * @author martin
 *
 */
public abstract class AbstractTime implements Comparable {

	/**
	 * the clock associated with this time.
	 * This is only interesting when user-defined clocks are
	 * used, which are a Level 2 feature.
	 */
	protected Clock clock;

	/**
	 * At the moment just return the single real-time clock.
	 * 
	 * @return A reference to the clock associated with this.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public Clock getClock() {
		return clock;
	}
	
	public int compareTo(AbstractTime time) {
	       if (time == null)
	            throw new IllegalArgumentException("null parameter");
	        // We are missing reflection in JOP - Tórur 3/6/2011
	        /*if (getClass() != time.getClass())
	            throw new ClassCastException();*/
	        if (clock != time.clock)
	            throw new IllegalArgumentException("different clocks");
	        // as we're using longs this cannot be simplified
	        // to a simple subtraction :-(
	        if (getTicks() > time.getTicks())
	            return 1;
	        else if (getTicks() < time.getTicks())
	            return -1;
	        else
	            return 0;
	}
	
	public boolean equals(AbstractTime t) {
		return (t.getTicks() == getTicks());
	}
	
	public abstract long getTicks();
	
	public abstract void setTicks(long l);
}