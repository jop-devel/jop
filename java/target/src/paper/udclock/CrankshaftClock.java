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

import javax.realtime.AbsoluteAbstractTime;
import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.ClockCallBack;
import javax.realtime.RelativeAbstractTime;
import javax.realtime.RelativeTime;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

/**
 * The crankshaft clock, that also contains the interrupt handler.
 * @author martin
 *
 */
public class CrankshaftClock extends Clock implements Runnable {


	long now = 0;
	long nextTime = 0;
	ClockCallBack cback;
	
	public CrankshaftClock() {
		
	}
	
	/**
	 * We tick in 10 degrees
	 */
	synchronized void tick() {
		now += 10;
		if (nextTime-now < 0 && cback!=null) {
			cback.atTime(this);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public long getMaxValue() {
		return 0xffffffffffffffffL;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#drivesEvents()
	 */
	@Override
	protected boolean drivesEvents() {
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getEpochOffset()
	 */
	@Override
	public RelativeTime getEpochOffset() {
		return new RelativeTime(0L, 0, this);
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getResolution()
	 */
	@Override
	public RelativeAbstractTime getResolution() {
		return new RelativeRotationalTime(0, 10, this);
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getResolution(javax.realtime.RelativeTime)
	 */
	@Override
	public RelativeTime getResolution(RelativeAbstractTime dest) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getTime()
	 */
	@Override
	public AbsoluteAbstractTime getTime() {
		return getTime(new AbsoluteRotationalTime(this));
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getTime(javax.realtime.AbsoluteTime)
	 */
	@Override
	public AbsoluteAbstractTime getTime(AbsoluteAbstractTime dest) {
		dest.setTicks(now);
		return dest;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#registerCallBack(javax.realtime.AbsoluteTime, javax.realtime.ClockCallBack)
	 */
	@Override
	public void registerCallBack(AbsoluteAbstractTime time, ClockCallBack clockEvent) {
		
		cback = clockEvent;
		nextTime = time.getTicks();

	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#resetTargetTime(javax.realtime.AbsoluteTime)
	 */
	@Override
	protected boolean resetTargetTime(AbsoluteAbstractTime time) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#setResolution(javax.realtime.RelativeTime)
	 */
	@Override
	protected void setResolution(RelativeTime resolution) {
		// TODO Auto-generated method stub

	}
	/**
	 * On JOP an interrupt handler is just a simple Runnable.
	 */
	@Override
	public void run() {
		tick();
	}

}
