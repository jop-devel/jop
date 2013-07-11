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

/**
 * @author martin
 *
 */
public class RealtimeClock extends Clock {

	static Clock single = new RealtimeClock();

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
		return new RelativeTime(0L, 0);
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getResolution()
	 */
	@Override
	public RelativeTime getResolution() {
		return new RelativeTime(0L, 1000);
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getResolution(javax.realtime.RelativeTime)
	 */
	@Override
	public RelativeTime getResolution(RelativeTime dest) {
		dest.set(0L, 1000);
		return dest;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getTime()
	 */
	@Override
	public AbsoluteTime getTime() {
		AbsoluteTime t = new AbsoluteTime();
		t.set(System.currentTimeMillis());
		return t;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getTime(javax.realtime.AbsoluteTime)
	 */
	@Override
	public AbsoluteTime getTime(AbsoluteTime dest) {
		dest.set(System.currentTimeMillis());
		return dest;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#registerCallBack(javax.realtime.AbsoluteTime, javax.realtime.ClockCallBack)
	 */
	@Override
	protected void registerCallBack(AbsoluteTime time, ClockCallBack clockEvent) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#resetTargetTime(javax.realtime.AbsoluteTime)
	 */
	@Override
	protected boolean resetTargetTime(AbsoluteTime time) {
		// TODO Auto-generated method stub
		return false;
	}


}
