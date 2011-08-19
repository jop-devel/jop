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
public class AbsoluteRotationalTime extends RotationalTime implements AbsoluteAbstractTime {
	
	public AbsoluteRotationalTime(Clock clock) {
		super(clock);
	}
	
	public AbsoluteRotationalTime(long rotations, int degrees, Clock clock) {
		super(rotations, degrees, clock);
		// TODO Auto-generated constructor stub
	}


	@Override
	public AbsoluteAbstractTime add(RelativeAbstractTime time) {
		AbsoluteRotationalTime ut = (AbsoluteRotationalTime) time;
		ticks += ut.ticks;
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
