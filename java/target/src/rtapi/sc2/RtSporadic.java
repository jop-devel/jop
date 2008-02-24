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

package sc2;

public class RtSporadic extends RtTask {

	/**
	 * 
	 * @param event A HW interrupt
	 * @param logic 
	 * @param period
	 * @param deadline
	 * @param offset
	 */
	public RtSporadic(String event, RtLogic logic, int period, int deadline, int offset) {
		super(logic, period, deadline, offset);
	}

	public RtSporadic(String event, RtLogic logic, int period, int deadline) {
		super(logic, period, deadline);
	}

	public RtSporadic(String event, RtLogic logic, int period) {
		super(logic, period);
	}

	/**
	 * Trigger a software event
	 *
	 */
	public void fire() {
		// fire an event
	}
}
