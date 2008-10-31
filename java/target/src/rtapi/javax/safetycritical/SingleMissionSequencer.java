/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package javax.safetycritical;

import javax.realtime.PriorityParameters;

/**
 * @author Martin Schoeberl
 *
 */
public class SingleMissionSequencer extends MissionSequencer {
	
	private MissionDescriptor mission;

	public SingleMissionSequencer(PriorityParameters priority, MissionDescriptor md) {
		super(priority);
		mission = md;
	}

	public MissionDescriptor getInitialMission() {
		return mission;
	}

	/**
	 * This is a single mission so we return null for the
	 * next mission.
	 */
	public MissionDescriptor getNextMission() {
		return null;
	}
}
