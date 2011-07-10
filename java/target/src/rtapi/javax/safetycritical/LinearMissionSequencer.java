/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

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

package javax.safetycritical;

import javax.realtime.PriorityParameters;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Level.SUPPORT;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

/**
 * A utility class for simple mission sequences.
 * 
 * @param <SpecificMission>
 */
@SCJAllowed
public class LinearMissionSequencer<SpecificMission extends Mission> extends
		MissionSequencer<SpecificMission> {
	SpecificMission single;

	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
	public LinearMissionSequencer(PriorityParameters priority,
			StorageParameters storage, SpecificMission m) {
		super(priority, storage);
		single = m;
	}

	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
	public LinearMissionSequencer(PriorityParameters priority,
			StorageParameters storage, SpecificMission[] missions) {
		super(priority, storage);
		throw new Error("Implement me");
	}

	@SCJAllowed(SUPPORT)
	@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
	@Override
	protected SpecificMission getNextMission() {
		return single;
	}
}
