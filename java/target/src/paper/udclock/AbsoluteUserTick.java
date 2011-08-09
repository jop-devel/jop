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

import javax.realtime.Clock;
import javax.realtime.AbsoluteAbstractTime;
import javax.realtime.RelativeAbstractTime;

/**
 * A user defined time to represent ticks for a
 * user defined clock.
 * 
 * @author martin
 *
 */
public class AbsoluteUserTick implements AbsoluteAbstractTime {
	
	int tick;
	Clock clock;
	
	public AbsoluteUserTick(int tick, Clock clock) {
		this.tick = tick;
		this.clock = clock;
	}

	@Override
	public Clock getClock() {
		return clock;
	}

	@Override
	public long getTicks() {
		return (int) tick;
	}

	@Override
	public void setTicks(long l) {
		tick = (int) l;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public AbsoluteAbstractTime add(RelativeAbstractTime time) {
		AbsoluteUserTick ut = (AbsoluteUserTick) time;
		tick += ut.tick;
		return this;
	}

	@Override
	public AbsoluteAbstractTime add(RelativeAbstractTime time,
			AbsoluteAbstractTime dest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RelativeAbstractTime subtract(AbsoluteAbstractTime time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RelativeAbstractTime subtract(AbsoluteAbstractTime time,
			RelativeAbstractTime dest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbsoluteAbstractTime subtract(RelativeAbstractTime time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbsoluteAbstractTime subtract(RelativeAbstractTime time,
			AbsoluteAbstractTime dest) {
		// TODO Auto-generated method stub
		return null;
	}

}
