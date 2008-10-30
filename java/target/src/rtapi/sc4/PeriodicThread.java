/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package sc4;

public abstract class PeriodicThread extends RealtimeThread {

	public PeriodicThread(int period, int deadline, int offset, int memSize) {
		super(period, deadline, offset, memSize);
	}
	
	public PeriodicThread(int period) {
		super(period, period, 0, 0);		
	}

	public PeriodicThread(javax.realtime.RelativeTime period) {
		super(0, 0, 0, 0);
		long ms = period.getMilliseconds();
		int ns = period.getNanoseconds();
		int us = ((int) ms)*1000 + ns/1000;
		this.period = us;
		this.deadline = us;
	}
}
