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

	/**
	 * What is the maximum value of our real-time clock?
	 * 
	 * On JOP it is actually way smaller at the moment.
	 */
	@Override
	public long getMaxValue() {
		return -1;
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
	public RelativeAbstractTime getEpochOffset() {
		return new RelativeTime(0L, 0);
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getResolution()
	 */
	@Override
	public RelativeAbstractTime getResolution() {
		return new RelativeTime(0L, 1000);
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getResolution(javax.realtime.RelativeTime)
	 */
	@Override
	public RelativeAbstractTime getResolution(RelativeAbstractTime dest) {
		((RelativeTime) dest).set(0L, 1000);
		return dest;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getTime()
	 */
	@Override
	public AbsoluteAbstractTime getTime() {
		AbsoluteTime t = new AbsoluteTime();
		t.set(System.currentTimeMillis());
		return t;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#getTime(javax.realtime.AbsoluteTime)
	 */
	@Override
	public AbsoluteAbstractTime getTime(AbsoluteAbstractTime dest) {
		((AbsoluteTime) dest).set(System.currentTimeMillis());
		return dest;
	}

	/* (non-Javadoc)
	 * @see javax.realtime.Clock#registerCallBack(javax.realtime.AbsoluteTime, javax.realtime.ClockCallBack)
	 */
	@Override
	public void registerCallBack(AbsoluteAbstractTime time, ClockCallBack clockEvent) {
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
