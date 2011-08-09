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
 * @author martin
 *
 */
public class PassiveExtendedClock extends Clock {

	SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	public PassiveExtendedClock() {
		
	}
	/**
	 * Our passive clock uses a 32-bit counter
	 */
	@Override
	public long getMaxValue() {
		return 0xffffffff;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#drivesEvents()
	 */
	@Override
	protected boolean drivesEvents() {
		return false;
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
		return new RelativeUserTick(1, this);
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
		int val = sys.cntInt;
		return new AbsoluteUserTick(val, this);
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getTime(javax.realtime.AbsoluteTime)
	 */
	@Override
	public AbsoluteAbstractTime getTime(AbsoluteAbstractTime dest) {
		int val = sys.cntInt;
		dest.setTicks(val);
		return dest;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#registerCallBack(javax.realtime.AbsoluteTime, javax.realtime.ClockCallBack)
	 */
	@Override
	protected void registerCallBack(AbsoluteAbstractTime time, ClockCallBack clockEvent) {
		// TODO Auto-generated method stub

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

}
