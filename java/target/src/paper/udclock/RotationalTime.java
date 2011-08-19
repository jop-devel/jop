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
package udclock;

import javax.realtime.AbstractTime;
import javax.realtime.Clock;

/**
 * Time type that represents rotations.
 * 
 * @author martin
 *
 */
public abstract class RotationalTime implements AbstractTime {

	Clock clock;
	/**
	 * Tick in degrees.
	 */
	long ticks;
	
	public RotationalTime(Clock clock) {
		this.clock = clock;
	}

	public RotationalTime(long rotations, int degrees, Clock clock) {
		this.clock = clock;
		set(rotations, degrees);
	}

	@Override
	public Clock getClock() {
		return clock;
	}

	@Override
	public long getTicks() {
		return ticks;
	}

	@Override
	public void setTicks(long l) {
		ticks = l;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	final long getRotations() {
		return ticks/360;
	}
	
	final long getDegrees() {
		return ticks%360;
	}
	
	void set(RotationalTime time) {
		ticks = time.ticks;
	}
	
	void set(long rotations) {
		ticks = rotations*360;
	}

	void set(long rotations, int degrees) {
		ticks = rotations*360 + degrees;
	}
	
	public int compareTo(RotationalTime time) {
		return 0;
	}
	public boolean equals(RotationalTime time) {
		return time.ticks==ticks;
	}
	
	
}
