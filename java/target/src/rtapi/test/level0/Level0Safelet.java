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

package test.level0;

import javax.realtime.PriorityParameters;
import javax.safetycritical.CyclicExecutive;
import javax.safetycritical.LinearMissionSequencer;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.RepeatingMissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.Phase;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public class Level0Safelet implements Safelet<CyclicExecutive> {

	@Override
	public MissionSequencer<CyclicExecutive> getSequencer() {

		PriorityParameters seq_prio = new PriorityParameters(13);
		long[] sizes = { 1024 };

		StorageParameters seq_storage = new StorageParameters(2048, sizes, 0, 0);

		Level0Mission missions[] = new Level0Mission[2];
		for (int i = 0; i < missions.length; i++) {
			missions[i] = new Level0Mission(i);
		}

		// Choose between repeating or linear sequencers
//		return new LinearMissionSequencer<CyclicExecutive>(seq_prio, seq_storage,
//		 		missions, true);
		return new RepeatingMissionSequencer<CyclicExecutive>(seq_prio, seq_storage,
				missions);

	}

	@Override
	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public void initializeApplication() {
		// TODO Auto-generated method stub
		
	}

}
